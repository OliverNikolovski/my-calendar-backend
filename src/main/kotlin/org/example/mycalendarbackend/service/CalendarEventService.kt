package org.example.mycalendarbackend.service

import jakarta.transaction.Transactional
import org.example.mycalendarbackend.api.request.CalendarEventCreationRequest
import org.example.mycalendarbackend.api.request.CalendarEventUpdateRequest
import org.example.mycalendarbackend.domain.dto.*
import org.example.mycalendarbackend.domain.entity.CalendarEvent
import org.example.mycalendarbackend.domain.enums.ActionType
import org.example.mycalendarbackend.extension.*
import org.example.mycalendarbackend.repository.CalendarEventRepository
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.time.ZonedDateTime
import java.util.*

@Service
internal class CalendarEventService(
    private val repository: CalendarEventRepository,
    private val restClient: RestClient,
    private val repeatingPatternService: RepeatingPatternService
) {

    fun generateInstanceForEvents(from: ZonedDateTime): Map<String, List<CalendarEventInstanceInfo>> {
        val events = repository.findAllByStartDateGreaterThanEqual(from)
        return createEventInstancesMapFromEvents(events)
    }

    fun generateInstancesForSequence(sequenceId: String): Map<String, List<CalendarEventInstanceInfo>> {
        val events = repository.findAllBySequenceId(sequenceId)
        return createEventInstancesMapFromEvents(events)
    }

    private fun createEventInstancesMapFromEvents(events: List<CalendarEvent>): Map<String, List<CalendarEventInstanceInfo>> {
        val requests = events.map { it.toRRuleRequest() }
        val response = generateEventInstances(requests)

        // Process the response and map to CalendarEventInstanceInfo
        val eventInstances = events.zip(response) { event, dates ->
            dates.mapIndexed { index, date ->
                CalendarEventInstanceInfo(event.id!!, date, event.duration, event.toDto(), index)
            }
        }

        // Group by date string
        return eventInstances.flatten()
            .groupBy { it.date.toLocalDate().toString() }
    }

    // this is used only after event creation, to get the instances of the created event
    fun generateInstancesForEvent(eventId: Long): Map<String, List<CalendarEventInstanceInfo>> {
        val event = repository.findById(eventId).orElseThrow()
        val request = event.toRRuleRequest()
        val instances = restClient.post()
            .uri("/generate-event-instances-for-event")
            .contentType(APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(object : ParameterizedTypeReference<List<ZonedDateTime>>() {})!!
        val result = instances.withIndex().associate { instance ->
            instance.value.toLocalDate().toString() to listOf(
                CalendarEventInstanceInfo(
                    eventId = eventId,
                    date = instance.value,
                    duration = event.duration,
                    event = event.toDto(),
                    order = instance.index
                )
            )
        }
        return result
    }

    fun save(request: CalendarEventCreationRequest): Long = save(request.toEntity(generateSequenceId()))

    fun save(calendarEvent: CalendarEvent): Long {
        val (rruleText, rruleString) = if (calendarEvent.isRepeating) {
            restClient.post()
                .uri("/get-rrule-text-and-string")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(calendarEvent.toRRuleRequest())
                .retrieve()
                .body(RRuleTextAndString::class.java)!!
        } else {
            RRuleTextAndString(null, null)
        }
        val repeatingPattern = calendarEvent.repeatingPattern?.let {
            it.copy(rruleText = rruleText, rruleString = rruleString).withBase(it)
        }
        return repository.save(calendarEvent.copy(repeatingPattern = repeatingPattern).withBase(calendarEvent)).id!!
    }

    @Transactional
    fun delete(id: Long, fromDate: ZonedDateTime, actionType: ActionType, order: Int) {
        val event = repository.findById(id).orElseThrow()
        when (actionType) {
            ActionType.THIS_EVENT -> deleteThisInstance(event, fromDate, order)
            ActionType.THIS_AND_ALL_FOLLOWING_EVENTS -> deleteThisAndAllFollowingInstances(event, fromDate)
            ActionType.ALL_EVENTS -> deleteAllInstances(event)
        }
    }

    @Transactional
    fun update(updateRequest: CalendarEventUpdateRequest) {
        val event = repository.findById(updateRequest.eventId).orElseThrow()
        when (updateRequest.actionType) {
            ActionType.THIS_EVENT -> updateSingleInstance(
                event = event,
                newFromDate = updateRequest.newStartDate,
                oldFromDate = updateRequest.fromDate,
                newDuration = updateRequest.newDuration
            )
            ActionType.THIS_AND_ALL_FOLLOWING_EVENTS -> updateThisAndAllFollowingInstances(
                event = event,
                newFromDate = updateRequest.newStartDate,
                oldFromDate = updateRequest.fromDate,
                newDuration = updateRequest.newDuration
            )
            ActionType.ALL_EVENTS -> updateAllInstances(
                event = event,
                newFromDate = updateRequest.newStartDate,
                newDuration = updateRequest.newDuration
            )
        }
    }

    private fun deleteThisInstance(event: CalendarEvent, fromDate: ZonedDateTime, order: Int) {
        if (event.isNonRepeating) {
            repository.delete(event)
            return
        }
        val (previousOccurrence, nextOccurrence) = getPreviousAndNextOccurrence(event, fromDate)
        nextOccurrence?.let { it ->
            val occurrenceCount = event.repeatingPattern!!.occurrenceCount?.let { it - order - 1 }
            val repeatingPatternForNewEvent = event.repeatingPattern.copy(
                start = it,
                occurrenceCount = occurrenceCount
            )
            val newEvent = event.copy(
                startDate = it,
                repeatingPattern = repeatingPatternForNewEvent
            )
            save(newEvent) // save new event
        }
        if (previousOccurrence == null) {
            repository.delete(event)
        } else {
            val updatedRepeatingPattern =
                event.repeatingPattern!!.copy(until = previousOccurrence.plusMinutes(event.duration.toLong())).withBase(event.repeatingPattern)
            repeatingPatternService.save(updatedRepeatingPattern)
        }
    }

    private fun deleteThisAndAllFollowingInstances(event: CalendarEvent, fromDate: ZonedDateTime) {
        val otherEventsInSequence = repository.findAllBySequenceIdAndStartDateGreaterThan(event.sequenceId, fromDate)
        repository.deleteAll(otherEventsInSequence)
        if (event.isRepeating) {
            val newRepeatingPattern = event.repeatingPattern!!.copy(until = fromDate.atStartOfDay()).withBase(event.repeatingPattern)
            save(
                event.copy(repeatingPattern = newRepeatingPattern).withBase(event)
            )
        } else {
            repository.delete(event)
        }
    }

    private fun deleteAllInstances(event: CalendarEvent) = repository.deleteAllBySequenceId(event.sequenceId)

    private fun updateSingleInstance(event: CalendarEvent, oldFromDate: ZonedDateTime, newFromDate: ZonedDateTime, newDuration: Int) {
        if (event.isNonRepeating) {
            save(
                event.copy(
                    startDate = newFromDate,
                    duration = newDuration
                ).withBase(event)
            )
            return
        }
        // create new non-repeating event on the date the update is applied
        val newNonRepeatingEvent = event.copy(
            startDate = newFromDate,
            duration = newDuration,
            repeatingPattern = null
        )
        save(newNonRepeatingEvent)
        // create new repeating event from the next occurrence onwards
        // HERE IS THE BUG. previousExecution is returned wrong
        val (previousOccurrence, nextOccurrence) = getPreviousAndNextOccurrence(event, oldFromDate)
        nextOccurrence?.let {
            val repeatingPatternForNewEvent = event.repeatingPattern!!.copy(start = nextOccurrence)
            val newEvent = event.copy(
                startDate = nextOccurrence,
                repeatingPattern = repeatingPatternForNewEvent,
            )
            save(newEvent)
        }
        // update the repeating pattern for the original event or delete the original event if the first instance is modified
        if (previousOccurrence == null) {
            repository.delete(event)
        } else {
            val updatedRepeatingPattern = event.repeatingPattern!!.copy(
                until = previousOccurrence.plusMinutes(event.duration.toLong())
            ).withBase(event.repeatingPattern)
            repeatingPatternService.save(updatedRepeatingPattern)
        }
    }

    private fun updateThisAndAllFollowingInstances(
        event: CalendarEvent, oldFromDate: ZonedDateTime,
        newFromDate: ZonedDateTime, newDuration: Int
    ) {
        //update all other following events in sequence
        val events = repository.findAllBySequenceIdAndStartDateGreaterThan(event.sequenceId, oldFromDate)
        // TODO: Refactor this to not have save in a loop
        events.map {
            it.copy(
                startDate = it.startDate.withTimeFrom(newFromDate),
                duration = newDuration
            ).withBase(it)
        }.forEach { save(it) }

        if (event.isRepeating) {
            // create new event with the new time and duration and new repeating pattern for it
            val repeatingPatternForNewEvent = event.repeatingPattern!!.copy(start = newFromDate)
            val newEvent =
                event.copy(startDate = newFromDate, duration = newDuration, repeatingPattern = repeatingPatternForNewEvent)
            save(newEvent)

            // modify repeating pattern for old event
            val updatedRepeatingPattern = event.repeatingPattern.copy(until = oldFromDate.plusMinutes(event.duration.toLong()))
            repeatingPatternService.save(updatedRepeatingPattern)
        }
    }

    private fun updateAllInstances(event: CalendarEvent, newFromDate: ZonedDateTime, newDuration: Int) {
        val events = repository.findAllBySequenceId(event.sequenceId)
        // TODO: Refactor to not use save in a loop
        events.map {
            it.copy(
                startDate = it.startDate.withTimeFrom(newFromDate),
                duration = newDuration
            ).withBase(it)
        }.forEach { save(it) }
    }

    private fun getPreviousAndNextOccurrence(event: CalendarEvent, fromDate: ZonedDateTime): PreviousAndNextOccurrence =
        restClient.post()
            .uri("/calculate-previous-next-execution")
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(
                mapOf(
                    "rruleRequest" to event.toRRuleRequest(),
                    "date" to fromDate.toString()
                )
            ).retrieve()
            .body(PreviousAndNextOccurrence::class.java)!!

    private fun generateSequenceId(): String {
        return UUID.randomUUID().toString()
    }

    private fun generateEventInstances(rruleRequests: List<RRuleRequest>) =
        restClient.post()
            .uri("/generate-event-instances")
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(rruleRequests)
            .retrieve()
            .body(object : ParameterizedTypeReference<List<List<ZonedDateTime>>>() {})!!

    private data class PreviousAndNextOccurrence(
        val previousOccurrence: ZonedDateTime?,
        val nextOccurrence: ZonedDateTime?
    )
}

data class CalendarEventInstanceInfo(
    val eventId: Long,
    val date: ZonedDateTime,
    val duration: Int,
    val event: CalendarEventDto,
    val order: Int
)

data class RRuleTextAndString(
    val rruleText: String?,
    val rruleString: String?
)