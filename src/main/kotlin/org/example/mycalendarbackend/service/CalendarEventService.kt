package org.example.mycalendarbackend.service

import jakarta.transaction.Transactional
import org.example.mycalendarbackend.domain.dto.*
import org.example.mycalendarbackend.domain.enums.DeletionType
import org.example.mycalendarbackend.extension.*
import org.example.mycalendarbackend.repository.CalendarEventRepository
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.data.repository.findByIdOrNull
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

    fun getCalendarEventWithChildren(eventId: Long): List<CalendarEventDto> =
        repository.findAllByIdOrParentId(eventId, eventId).map { it.toDto() }

    fun generateCalendarEventInstances(eventId: Long): Result<CalendarEventInstancesContainer2> {
        val event = repository.findByIdOrNull(eventId) ?: return Result.failure(NotFoundException())
        if (event.isNonRepeating) {
            return Result.success(
                CalendarEventInstancesContainer2(event.duration, listOf(event.startDate))
            )
        }
        val request = with(event) {
            RRuleRequest(
                start = startDate.toDateTime(),
                end = repeatingPattern!!.until?.toDateTime(),
                freq = repeatingPattern.frequency,
                count = repeatingPattern.occurrenceCount,
                byWeekDay = repeatingPattern.weekDays,
                bySetPos = repeatingPattern.setPos,
                interval = repeatingPattern.interval
            )
        }
        val dates = restClient.post()
            .uri("/generate-event-instances")
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(object : ParameterizedTypeReference<List<ZonedDateTime>>() {})  // response type

        return dates?.let {
            Result.success(
                CalendarEventInstancesContainer2(event.duration, dates)
            )
        } ?: Result.failure(IllegalStateException("Calendar event instances could not be retrieved."))
    }

    fun generateInstancesForEvents(from: ZonedDateTime): Result<List<CalendarEventInstancesContainer>> {
        val events = repository.findAllByStartDateGreaterThanEqual(from)
        val requests = events.map { it.toRRuleRequest() }
        val response = restClient.post()
            .uri("/generate-event-instances")
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(requests)
            .retrieve()
            .body(object : ParameterizedTypeReference<List<List<ZonedDateTime>>>() {})  // response type

        return response?.let {
            Result.success(
                events.zip(it) { event, dates ->
                    CalendarEventInstancesContainer(
                        eventId = event.id!!,
                        duration = event.duration,
                        calendarEventInstances = dates
                    )
                }
            )
        } ?: Result.failure(IllegalStateException("Calendar event instances could not be retrieved."))
    }

    fun generateInstanceForEvents2(from: ZonedDateTime): Map<String, List<CalendarEventInstanceInfo>> {
        val events = repository.findAllByStartDateGreaterThanEqual(from)
        val requests = events.map { it.toRRuleRequest() }
        val response = restClient.post()
            .uri("/generate-event-instances")
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(requests)
            .retrieve()
            .body(object : ParameterizedTypeReference<List<List<ZonedDateTime>>>() {})!!  // response type

//        val result = events.zip(response) { event, dates ->
//            dates.groupBy(
//                { it.toLocalDate().toString() },
//                { CalendarEventInstanceInfo(event.id!!, it, event.duration) })
//        }.flatMap { it.entries }
//            .groupBy({ it.key }, { it.value })
//            .mapValues { it.value.flatten() }
//        return result

        // Process the response and map to CalendarEventInstanceInfo
        val eventInstances = events.zip(response) { event, dates ->
            dates.map { CalendarEventInstanceInfo(event.id!!, it, event.duration, event.toDto()) }
        }

        // Group by date string
        return eventInstances.flatten()
            .groupBy { it.date.toLocalDate().toString() }
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
    fun delete(id: Long, fromDate: ZonedDateTime, deletionType: DeletionType) = when (deletionType) {
        DeletionType.THIS_EVENT -> deleteThisInstance(id, fromDate)
        DeletionType.THIS_AND_ALL_FOLLOWING_EVENTS -> deleteThisAndAllFollowintInstances(id, fromDate)
        DeletionType.ALL_EVENTS -> deleteAllInstances(id, fromDate)
    }

    private fun deleteThisInstance(id: Long, fromDate: ZonedDateTime) {
        val event = repository.findById(id).orElseThrow()
        if (event.isNonRepeating) {
            repository.delete(event)
            return
        }
        val newRepeatingPatternForExistingEvent = event.repeatingPattern!!.copy(until = fromDate.endOfPreviousDay())
        val repeatingPatternForNewEvent = event.repeatingPattern.copy(start = nextExecution)
        val newEvent = event.copy(startDate = nextExecution, repeatingPattern = repeatingPatternForNewEvent)
        save(event.copy(repeatingPattern = newRepeatingPatternForExistingEvent).withBase(event).toDto()) // save existing event
        save(newEvent.toDto())
    }

    private fun deleteThisAndAllFollowintInstances(id: Long, fromDate: ZonedDateTime) {

    }

    private fun deleteAllInstances(id: Long, fromDate: ZonedDateTime) {

    }

}

data class CalendarEventInstanceInfo(
    val eventId: Long,
    val date: ZonedDateTime,
    val duration: Int,
    val event: CalendarEventDto
)

data class RRuleTextAndString(
    val rruleText: String,
    val rruleString: String
)