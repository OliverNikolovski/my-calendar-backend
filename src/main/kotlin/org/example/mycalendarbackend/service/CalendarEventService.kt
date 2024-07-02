package org.example.mycalendarbackend.service

import jakarta.transaction.Transactional
import org.example.mycalendarbackend.domain.dto.*
import org.example.mycalendarbackend.extension.toDateTime
import org.example.mycalendarbackend.extension.toDto
import org.example.mycalendarbackend.extension.toEntity
import org.example.mycalendarbackend.extension.toRRuleRequest
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
    private val restClient: RestClient
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
                end = endDate?.toDateTime(),
                freq = repeatingPattern!!.frequency,
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
        var dto: CalendarEventDto? = null
        if (calendarEventDto.repeatingPattern != null) {
            val result = restClient.post()
                .uri("/get-rrule-text-and-string")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(calendarEventDto.repeatingPattern)
                .retrieve()
                .body(RRuleTextAndString::class.java)!!
            dto = calendarEventDto.copy()
        }
        return repository.save(calendarEventDto.toEntity(parent)).id!!
    }

    fun delete(id: Long) = repository.deleteById(id)

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