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
import java.time.LocalTime
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
        val requests = events.map { it.toRRuleRequest() }
        val response = restClient.post()
            .uri("/generate-event-instances")
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(requests)
            .retrieve()
            .body(object : ParameterizedTypeReference<List<List<ZonedDateTime>>>() {})!!  // response type

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

    @Transactional
    fun save(request: CalendarEventCreationRequest): Long = save(request.toEntity(UUID.randomUUID().toString()))

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
        if (event.isNonRepeating) {
            repository.delete(event)
            return
        }
        when (actionType) {
            ActionType.THIS_EVENT -> deleteThisInstance(event, fromDate, order)
            ActionType.THIS_AND_ALL_FOLLOWING_EVENTS -> deleteThisAndAllFollowingInstances(event, fromDate)
            ActionType.ALL_EVENTS -> deleteAllInstances(event)
        }
    }

    fun update(updateRequest: CalendarEventUpdateRequest) {
        val event = repository.findById(updateRequest.eventId).orElseThrow()
        if (event.isNonRepeating) {
            save(
                event.copy(
                    startDate = event.startDate.withTime(updateRequest.newStartTime),
                    duration = updateRequest.newDuration
                ).withBase(event)
            )
            return
        }
        when (updateRequest.actionType) {
            ActionType.THIS_EVENT -> updateSingleInstance(
                event = event,
                date = updateRequest.fromDate,
                newStartTime = updateRequest.newStartTime,
                newDuration = updateRequest.newDuration
            )
            ActionType.THIS_AND_ALL_FOLLOWING_EVENTS -> updateThisAndAllFollowingInstances()
            ActionType.ALL_EVENTS -> updateAllInstances()
        }
    }

    private fun deleteThisInstance(event: CalendarEvent, fromDate: ZonedDateTime, order: Int) {
        val (previousOccurrence, nextOccurrence) = getPreviousAndNextOccurrence(event, fromDate)
//        repeatingPatternService.save(newRepeatingPatternForExistingEvent)
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
            //TODO: Check if this is needed
            save(
                event.copy(repeatingPattern = updatedRepeatingPattern).withBase(event)
            ) // save existing event
        }
    }

    private fun deleteThisAndAllFollowingInstances(event: CalendarEvent, fromDate: ZonedDateTime) {
        val otherEventsInSequence = repository.findAllBySequenceIdAndStartDateGreaterThanEqual(event.sequenceId, fromDate)
            .filter { it.id != event.id }
        repository.deleteAll(otherEventsInSequence)
        val newRepeatingPattern = event.repeatingPattern!!.copy(until = fromDate.atStartOfDay()).withBase(event.repeatingPattern)
        save(
            event.copy(repeatingPattern = newRepeatingPattern).withBase(event)
        )
    }

    private fun deleteAllInstances(event: CalendarEvent) = repository.deleteAllBySequenceId(event.sequenceId)

    private fun updateSingleInstance(event: CalendarEvent, date: ZonedDateTime, newStartTime: LocalTime, newDuration: Int) {
        // create new non-repeating event on the date the update is applied
        val newNonRepeatingEvent = event.copy(
            startDate = date.withTime(newStartTime),
            duration = newDuration,
            repeatingPattern = null
        )
        save(newNonRepeatingEvent)
        // create new repeating event from the next occurrence onwards
        val (previousOccurrence, nextOccurrence) = getPreviousAndNextOccurrence(event, date)
        nextOccurrence?.let {
            val repeatingPatternForNewEvent = event.repeatingPattern!!.copy(start = nextOccurrence)
            val newEvent = event.copy(
                startDate = nextOccurrence,
                repeatingPattern = repeatingPatternForNewEvent
            )
            save(newEvent)
        }
        // update the repeating pattern for the original event or delete the original event if the first instance is modified
        if (previousOccurrence == null) {
            repository.delete(event)
        } else {
            val updatedRepeatingPattern = event.repeatingPattern!!.copy(
                until = previousOccurrence.plusMinutes(newDuration.toLong())
            ).withBase(event.repeatingPattern)
            // TODO: Check if this is needed, maybe save just the repeating pattern from its repository?
            save(
                event.copy(repeatingPattern = updatedRepeatingPattern).withBase(event)
            )
        }
    }

    private fun updateThisAndAllFollowingInstances() {
        // TODO
    }

    private fun updateAllInstances() {
        // TODO
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