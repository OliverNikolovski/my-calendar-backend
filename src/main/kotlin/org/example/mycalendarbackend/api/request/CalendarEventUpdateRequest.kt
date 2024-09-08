package org.example.mycalendarbackend.api.request

import org.example.mycalendarbackend.domain.enums.ActionType
import java.time.ZonedDateTime

data class CalendarEventUpdateRequest(
    val eventId: Long,
    val fromDate: ZonedDateTime,
    val actionType: ActionType,
    val newStartDate: ZonedDateTime,
    val newDuration: Int,
    val order: Int
)
