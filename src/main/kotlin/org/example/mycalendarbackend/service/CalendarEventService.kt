package org.example.mycalendarbackend.service

import jakarta.transaction.Transactional
import org.example.mycalendarbackend.domain.dto.*
import org.example.mycalendarbackend.domain.enums.DeletionType
import org.example.mycalendarbackend.extension.*
import org.example.mycalendarbackend.repository.CalendarEventRepository
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.time.ZonedDateTime

@Service
class CalendarEventService internal constructor(
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
    fun save(calendarEventDto: CalendarEventDto): Long {
        val parent = calendarEventDto.parentId?.let {
            repository.getReferenceById(it)
        }
        var dto = calendarEventDto
        if (calendarEventDto.isRepeating) {
            val result = restClient.post()
                .uri("/get-rrule-text-and-string")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(calendarEventDto.toRRuleRequest())
                .retrieve()
                .body(RRuleTextAndString::class.java)!!
            dto = calendarEventDto.copy(
                repeatingPattern = calendarEventDto.repeatingPattern!!.copy(
                    rruleText = result.rruleText,
                    rruleString = result.rruleString
                )
            )
        }
        return repository.save(dto.toEntity(parent)).id!!
    }

    @Transactional
    fun delete(id: Long, fromDate: ZonedDateTime, deletionType: DeletionType, order: Int): Map<String, List<CalendarEventInstanceInfo>> = when (deletionType) {
        DeletionType.THIS_EVENT -> deleteThisInstance(id, fromDate, order)
        DeletionType.THIS_AND_ALL_FOLLOWING_EVENTS -> deleteThisAndAllFollowintInstances(id, fromDate)
        DeletionType.ALL_EVENTS -> deleteAllInstances(id, fromDate)
    }

    private fun deleteThisInstance(id: Long, fromDate: ZonedDateTime, order: Int): Map<String, List<CalendarEventInstanceInfo>> {
        val event = repository.findById(id).orElseThrow()
        if (event.isNonRepeating) {
            repository.delete(event)
        }
        val (previousOccurrence, nextOccurrence) = restClient.post()
            .uri("/calculate-previous-next-execution")
            .contentType(APPLICATION_JSON)
            .body(
                mapOf(
                    "rruleRequest" to event.toRRuleRequest(),
                    "date" to fromDate.toString()
                )
            ).retrieve()
            .body(PreviousAndNextOccurrence::class.java)!!
        val newRepeatingPatternForExistingEvent =
            event.repeatingPattern!!.copy(until = previousOccurrence?.plusMinutes(event.duration.toLong()))
        repeatingPatternService.save(newRepeatingPatternForExistingEvent)
        nextOccurrence?.let { it ->
            val occurrenceCount = event.repeatingPattern.occurrenceCount?.let { it - order - 1 }
            val repeatingPatternForNewEvent = event.repeatingPattern.copy(
                start = it,
                occurrenceCount = occurrenceCount
            )
            val newEvent = event.copy(
                startDate = it,
                repeatingPattern = repeatingPatternForNewEvent,
                parent = event,
            )
            save(newEvent.toDto()) // save new event
        }
        val oldRepeatingPattern = event.repeatingPattern
        save(
            event.copy(repeatingPattern = newRepeatingPatternForExistingEvent).withBase(event).toDto()
        ) // save existing event
        repeatingPatternService.delete(oldRepeatingPattern)
    }

    private fun deleteThisAndAllFollowintInstances(id: Long, fromDate: ZonedDateTime): Map<String, List<CalendarEventInstanceInfo>> {
        //TODO
    }

    private fun deleteAllInstances(id: Long, fromDate: ZonedDateTime): Map<String, List<CalendarEventInstanceInfo>> {
        //TODO
    }

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
    val rruleText: String,
    val rruleString: String
)