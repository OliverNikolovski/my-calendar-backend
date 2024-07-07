package org.example.mycalendarbackend.extension

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

fun RepeatingPatternDto.toEntity(startDate: ZonedDateTime): RepeatingPattern = RepeatingPattern(
    frequency = frequency,
    weekDays = weekDays,
    setPos = setPos,
    interval = interval,
    occurrenceCount = occurrenceCount,
    rruleText = rruleText,
    rruleString = rruleString,
    start = startDate,
    until = endDate
).also { it.id = id }
