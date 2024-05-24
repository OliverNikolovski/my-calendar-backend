package org.example.mycalendarbackend.domain.dto

import org.example.mycalendarbackend.domain.enums.Frequency
import java.time.ZonedDateTime

data class RepeatingPatternDto(
    val id: Long?,

    val frequency: Frequency,

    val weekDays: Array<Int>?,

    val setPos: Int?,

    val interval: Int?,

    val occurrenceCount: Int?,

    val rruleText: String?,

    val rruleString: String?,

    val start: ZonedDateTime?,

    val until: ZonedDateTime?
)
