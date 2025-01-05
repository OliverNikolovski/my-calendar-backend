package org.example.mycalendarbackend.api.request

import org.example.mycalendarbackend.domain.enums.ActionType
import java.time.ZonedDateTime

data class CalendarEventUpdateRequest(
    val eventId: Long,
    val fromDate: ZonedDateTime,
    val actionType: ActionType,
    val startTime: ZonedDateTime,
    val duration: Int,
    val order: Int,
    val minutes: Int?
)
