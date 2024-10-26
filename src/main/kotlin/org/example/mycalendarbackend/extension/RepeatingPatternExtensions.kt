package org.example.mycalendarbackend.extension

import org.example.mycalendarbackend.api.request.RepeatingPatternCreationRequest
import org.example.mycalendarbackend.domain.dto.RRuleRequest
import org.example.mycalendarbackend.domain.dto.RepeatingPatternDto
import org.example.mycalendarbackend.domain.entity.RepeatingPattern
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun RepeatingPattern.toDto(offsetInSeconds: Int): RepeatingPatternDto = RepeatingPatternDto(
    id = id,
    frequency = frequency,
    weekDays = weekDays,
    setPos = setPos,
    interval = interval,
    occurrenceCount = occurrenceCount,
    rruleText = rruleText,
    rruleString = rruleString,
    endDate = until?.withOffsetSameInstant(offsetInSeconds)?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"))
)

fun RepeatingPatternDto.toEntity(): RepeatingPattern = RepeatingPattern(
    frequency = frequency,
    weekDays = weekDays,
    setPos = setPos,
    interval = interval,
    occurrenceCount = occurrenceCount,
    rruleText = rruleText,
    rruleString = rruleString,
    until = ZonedDateTime.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"))
).also { it.id = id }

fun RepeatingPattern.toRRuleRequest(startDate: ZonedDateTime): RRuleRequest = RRuleRequest(
    start = startDate.toDateTime(),
    end = until?.toDateTime(),
    freq = frequency,
    count = occurrenceCount,
    byWeekDay = weekDays,
    bySetPos = setPos,
    interval = interval
)

fun RepeatingPatternCreationRequest.toDto(): RepeatingPatternDto =
    RepeatingPatternDto(
        id = id,
        frequency = frequency,
        weekDays = weekDays,
        setPos = setPos,
        interval = interval,
        occurrenceCount = occurrenceCount,
        rruleText = rruleText,
        rruleString = rruleString,
        endDate = endDate
    )
