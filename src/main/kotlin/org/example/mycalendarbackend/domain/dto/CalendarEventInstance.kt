package org.example.mycalendarbackend.domain.dto

import java.time.ZonedDateTime

data class CalendarEventInstance(
    val date: ZonedDateTime,
    val duration: Int
)
