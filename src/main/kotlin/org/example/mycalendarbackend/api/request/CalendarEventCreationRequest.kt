package org.example.mycalendarbackend.api.request

data class CalendarEventCreationRequest(
    val id: Long?,

    val title: String?,

    val description: String?,

    val isRepeating: Boolean,

    val startDate: String,

    val duration: Int,

    val repeatingPattern: RepeatingPatternCreationRequest?,

    val minutes: Int?
)


