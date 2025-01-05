package org.example.mycalendarbackend.domain.dto

data class CalendarEventDto(
    val id: Long?,

    val title: String?,

    val description: String?,

    val isRepeating: Boolean,

    val startDate: String,

    val duration: Int,

    val repeatingPattern: RepeatingPatternDto?,

    val sequenceId: String,

    val isPublic: Boolean?,

    val minutes: Int?
)
