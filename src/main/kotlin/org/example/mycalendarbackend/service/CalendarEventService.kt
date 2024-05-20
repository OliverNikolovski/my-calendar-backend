package org.example.mycalendarbackend.service

import jakarta.transaction.Transactional
import org.example.mycalendarbackend.domain.dto.CalendarEventDto
import org.example.mycalendarbackend.extension.toDto
import org.example.mycalendarbackend.extension.toEntity
import org.example.mycalendarbackend.repository.CalendarEventRepository
import org.springframework.stereotype.Service

@Service
class CalendarEventService internal constructor(
    private val repository: CalendarEventRepository
) {

    fun getCalendarEventWithChildren(eventId: Long): List<CalendarEventDto> =
        repository.findAllByIdOrParentId(eventId, eventId).map { it.toDto() }

    @Transactional
    fun save(calendarEventDto: CalendarEventDto): Long {
        val parent = calendarEventDto.parentId?.let {
            repository.getReferenceById(it)
        }
        return repository.save(calendarEventDto.toEntity(parent)).id!!
    }

    fun delete(id: Long) = repository.deleteById(id)

}