package org.example.mycalendarbackend.api.request

import org.example.mycalendarbackend.domain.enums.Frequency

data class RepeatingPatternCreationRequest(
    val id: Long?,

    val frequency: Frequency,

    val weekDays: Array<Int>?,

    val setPos: Int?,

    val interval: Int?,

    val occurrenceCount: Int?,

    val rruleText: String?,

    val rruleString: String?,

    val endDate: String?
)
