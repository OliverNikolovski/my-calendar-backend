package org.example.mycalendarbackend.events

import org.example.mycalendarbackend.domain.entity.CalendarEvent

data class EmailNotificationAddedOrUpdated(
    val event: CalendarEvent,
    val minutes: Int
)
