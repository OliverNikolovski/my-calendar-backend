package org.example.mycalendarbackend.extension

import org.example.mycalendarbackend.api.request.CalendarEventCreationRequest
import org.example.mycalendarbackend.domain.dto.CalendarEventCreationDto
import org.example.mycalendarbackend.domain.dto.CalendarEventDto
import org.example.mycalendarbackend.domain.dto.RRuleRequest
import org.example.mycalendarbackend.domain.entity.CalendarEvent
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun CalendarEvent.toDto(isPublic: Boolean? = null): CalendarEventDto = CalendarEventDto(
    id = id,
    title = title,
    description = description,
    isRepeating = isRepeating,
    startDate = startDate.withOffsetSameInstant(offsetInSeconds).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")),
    duration = duration,
    repeatingPattern = repeatingPattern?.toDto(offsetInSeconds),
    sequenceId = sequenceId,
    isPublic = isPublic
)

fun CalendarEventCreationDto.toEntity(sequenceId: String, offsetInSeconds: Int): CalendarEvent = CalendarEvent(
    title = title,
    description = description,
    startDate = startDate,
    duration = duration,
    repeatingPattern = repeatingPattern?.toEntity(),
    sequenceId = sequenceId,
    offsetInSeconds = offsetInSeconds
).also { it.id = id }

fun CalendarEvent.toRRuleRequest(): RRuleRequest =
    if (isNonRepeating) {
        RRuleRequest(
            start = startDate.withOffsetSameInstant(offsetInSeconds).toDateTime(),
            count = 1
        )
    } else {
        RRuleRequest(
            start = startDate.withOffsetSameInstant(offsetInSeconds).toDateTime(),
            end = repeatingPattern!!.until?.withOffsetSameInstant(offsetInSeconds)?.toDateTime(),
            freq = repeatingPattern.frequency,
            count = repeatingPattern.occurrenceCount,
            byWeekDay = repeatingPattern.weekDays,
            bySetPos = repeatingPattern.setPos,
            interval = repeatingPattern.interval
        )
    }

//fun CalendarEventDto.toRRuleRequest(): RRuleRequest =
//    if (repeatingPattern == null) {
//        RRuleRequest(
//            start = startDate.toDateTime(),
//            count = 1
//        )
//    } else {
//        RRuleRequest(
//            start = startDate.toDateTime(),
//            end = repeatingPattern.endDate?.toDateTime(),
//            freq = repeatingPattern.frequency,
//            count = repeatingPattern.occurrenceCount,
//            byWeekDay = repeatingPattern.weekDays,
//            bySetPos = repeatingPattern.setPos,
//            interval = repeatingPattern.interval
//        )
//    }

fun CalendarEventCreationRequest.toDto(): CalendarEventCreationDto =
    CalendarEventCreationDto(
        id = id,
        title = title,
        description = description,
        isRepeating = isRepeating,
        startDate = ZonedDateTime.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")),
        duration = duration,
        repeatingPattern = repeatingPattern?.toDto()
    )
