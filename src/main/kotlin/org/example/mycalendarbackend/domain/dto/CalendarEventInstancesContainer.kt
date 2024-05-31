package org.example.mycalendarbackend.domain.dto

import java.time.ZonedDateTime

data class CalendarEventInstancesContainer(
    val eventId: Long,
    val duration: Int,
    val calendarEventInstances: List<ZonedDateTime>
)

data class CalendarEventInstancesContainer2(
    val duration: Int,
    val calendarEventInstances: List<ZonedDateTime>
)
