package org.example.mycalendarbackend.extension

import org.example.mycalendarbackend.domain.dto.RepeatingPatternDto
import org.example.mycalendarbackend.domain.entity.RepeatingPattern

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
