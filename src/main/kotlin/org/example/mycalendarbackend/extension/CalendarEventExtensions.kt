package org.example.mycalendarbackend.extension

import org.example.mycalendarbackend.api.request.CalendarEventCreationRequest
import org.example.mycalendarbackend.domain.dto.CalendarEventDto
import org.example.mycalendarbackend.domain.dto.RRuleRequest
import org.example.mycalendarbackend.domain.entity.CalendarEvent

fun CalendarEvent.toDto(isPublic: Boolean? = null): CalendarEventDto = CalendarEventDto(
    id = id,
    title = title,
    description = description,
    isRepeating = isRepeating,
    startDate = startDate,
    duration = duration,
    repeatingPattern = repeatingPattern?.toDto(),
    sequenceId = sequenceId,
    isPublic = isPublic
)

fun CalendarEventDto.toEntity(): CalendarEvent = CalendarEvent(
    title = title,
    description = description,
    startDate = startDate,
    duration = duration,
    repeatingPattern = repeatingPattern?.toEntity(),
    sequenceId = sequenceId
).also { it.id = id }

fun CalendarEventCreationRequest.toEntity(sequenceId: String): CalendarEvent = CalendarEvent(
    title = title,
    description = description,
    startDate = startDate,
    duration = duration,
    repeatingPattern = repeatingPattern?.toEntity(),
    sequenceId = sequenceId
).also { it.id = id }

fun CalendarEvent.toRRuleRequest(): RRuleRequest =
    if (isNonRepeating) {
        RRuleRequest(
            start = startDate.toDateTime(),
            count = 1
        )
    } else {
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

fun CalendarEventDto.toRRuleRequest(): RRuleRequest =
    if (repeatingPattern == null) {
        RRuleRequest(
            start = startDate.toDateTime(),
            count = 1
        )
    } else {
        RRuleRequest(
            start = startDate.toDateTime(),
            end = repeatingPattern.endDate?.toDateTime(),
            freq = repeatingPattern.frequency,
            count = repeatingPattern.occurrenceCount,
            byWeekDay = repeatingPattern.weekDays,
            bySetPos = repeatingPattern.setPos,
            interval = repeatingPattern.interval
        )
    }

