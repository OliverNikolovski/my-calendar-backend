package org.example.mycalendarbackend.extension

import org.example.mycalendarbackend.domain.dto.RRuleRequest
import org.example.mycalendarbackend.domain.dto.RepeatingPatternDto
import org.example.mycalendarbackend.domain.entity.RepeatingPattern
import java.time.ZonedDateTime

fun RepeatingPattern.toDto(): RepeatingPatternDto = RepeatingPatternDto(
    id = id,
    frequency = frequency,
    weekDays = weekDays,
    setPos = setPos,
    interval = interval,
    occurrenceCount = occurrenceCount,
    rruleText = rruleText,
    rruleString = rruleString,
    endDate = until
)

fun RepeatingPatternDto.toEntity(): RepeatingPattern = RepeatingPattern(
    frequency = frequency,
    weekDays = weekDays,
    setPos = setPos,
    interval = interval,
    occurrenceCount = occurrenceCount,
    rruleText = rruleText,
    rruleString = rruleString,
    until = endDate
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
