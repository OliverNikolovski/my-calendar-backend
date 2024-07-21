package org.example.mycalendarbackend.domain.dto

import java.time.ZonedDateTime

data class CalendarEventInstancesContainer(
    val eventId: Long,
    val duration: Int,
    val calendarEventInstances: List<ZonedDateTime>
)
