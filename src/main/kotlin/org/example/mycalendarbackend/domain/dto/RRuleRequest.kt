package org.example.mycalendarbackend.domain.dto

import org.example.mycalendarbackend.domain.enums.Frequency
import java.time.ZonedDateTime

data class RRuleRequest(
    val start: DateTime,
    val end: DateTime? = null,
    val freq: Frequency? = Frequency.DAILY,
    val count: Int? = null,
    val byWeekDay: Array<Int>? = null,
    val bySetPos: Int? = null,
    val interval: Int? = null
)
