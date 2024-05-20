package org.example.mycalendarbackend.domain.dto

import java.time.ZonedDateTime

data class CalendarEventDto(
    val id: Long?,

    val title: String?,

    val description: String?,

    val startDate: ZonedDateTime,

    val endDate: ZonedDateTime?,

    val duration: Int,

    val repeatingPattern: RepeatingPatternDto?,

    val parentId: Long?
)
