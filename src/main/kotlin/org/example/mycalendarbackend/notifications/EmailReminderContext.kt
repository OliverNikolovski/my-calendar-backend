package org.example.mycalendarbackend.notifications

import java.time.ZonedDateTime

data class EmailReminderContext(
    val userFirstName: String,
    val userEmail: String,
    val eventTitle: String,
    val eventDescription: String,
    val minutesBefore: Long,
    val eventDate: String,
    val eventTime: String
)
