package org.example.mycalendarbackend.domain.dto

import java.time.ZonedDateTime

data class CalendarEventCreationDto(
    val id: Long?,

    val title: String?,

    val description: String?,

    val isRepeating: Boolean,

    val startDate: ZonedDateTime,

    val duration: Int,

    val repeatingPattern: RepeatingPatternDto?,

    val minutes: Int?
)