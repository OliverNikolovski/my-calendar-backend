package org.example.mycalendarbackend.api.request

import org.example.mycalendarbackend.domain.enums.ActionType
import java.time.LocalTime
import java.time.ZonedDateTime

data class CalendarEventUpdateRequest(
    val eventId: Long,
    val fromDate: ZonedDateTime,
    val actionType: ActionType,
    val newStartTime: LocalTime,
    val newDuration: Int
)
