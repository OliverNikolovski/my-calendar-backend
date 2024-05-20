package org.example.mycalendarbackend.extension

import org.example.mycalendarbackend.domain.dto.CalendarEventDto
import org.example.mycalendarbackend.domain.entity.CalendarEvent

fun CalendarEvent.toDto(): CalendarEventDto = CalendarEventDto(
    id, title, description, startDate, endDate, duration, repeatingPattern?.toDto(), parentId
)

fun CalendarEventDto.toEntity(parent: CalendarEvent?): CalendarEvent = CalendarEvent(
    title, description, startDate, endDate, duration, repeatingPattern?.toEntity(), parent
).also { it.id = id }

