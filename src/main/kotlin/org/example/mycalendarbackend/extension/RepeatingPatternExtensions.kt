package org.example.mycalendarbackend.extension

import org.example.mycalendarbackend.domain.dto.RepeatingPatternDto
import org.example.mycalendarbackend.domain.entity.RepeatingPattern

fun RepeatingPattern.toDto(): RepeatingPatternDto = RepeatingPatternDto(
    id, frequency, weekDays, setPos, interval, occurrenceCount, rruleText, rruleString, start, until
)

fun RepeatingPatternDto.toEntity(): RepeatingPattern = RepeatingPattern(
    frequency, weekDays, setPos, interval, occurrenceCount, rruleText, rruleString, start, until
).also { it.id = id }
