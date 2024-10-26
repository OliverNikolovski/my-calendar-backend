package org.example.mycalendarbackend.api.request

import org.example.mycalendarbackend.domain.dto.RepeatingPatternDto
import java.time.ZonedDateTime

data class CalendarEventCreationRequest(
    val id: Long?,

    val title: String?,

    val description: String?,

    val isRepeating: Boolean,

    val startDate: String,

    val duration: Int,

    val repeatingPattern: RepeatingPatternCreationRequest?
)


