package org.example.mycalendarbackend.service

import com.fasterxml.jackson.databind.type.CollectionType
import jakarta.transaction.NotSupportedException
import jakarta.transaction.Transactional
import org.example.mycalendarbackend.domain.dto.CalendarEventDto
import org.example.mycalendarbackend.domain.dto.CalendarEventInstance
import org.example.mycalendarbackend.domain.dto.RRuleRequest
import org.example.mycalendarbackend.extension.toDto
import org.example.mycalendarbackend.extension.toEntity
import org.example.mycalendarbackend.repository.CalendarEventRepository
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.http.MediaType
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.lang.reflect.Type
import java.time.ZonedDateTime

@Service
class CalendarEventService internal constructor(
    private val repository: CalendarEventRepository,
    private val restClient: RestClient
) {

    fun getCalendarEventWithChildren(eventId: Long): List<CalendarEventDto> =
        repository.findAllByIdOrParentId(eventId, eventId).map { it.toDto() }

    fun generateCalendarEventInstances(eventId: Long): List<ZonedDateTime> {
        val event = repository.findById(eventId).orElseThrow(::NotFoundException)
        if (event.repeatingPattern == null) {
            throw NotSupportedException()
        }
        val request = with (event) {
            RRuleRequest(
                start = startDate,
                end = endDate,
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
        return dates ?: emptyList()
    }

    @Transactional
    fun save(calendarEventDto: CalendarEventDto): Long {
        val parent = calendarEventDto.parentId?.let {
            repository.getReferenceById(it)
        }
        return repository.save(calendarEventDto.toEntity(parent)).id!!
    }

    fun delete(id: Long) = repository.deleteById(id)

}