package org.example.mycalendarbackend.extension

import org.example.mycalendarbackend.domain.dto.DateTime
import java.time.ZonedDateTime

fun ZonedDateTime.toDateTime(): DateTime = DateTime(
    year = year,
    month = monthValue,
    day = dayOfMonth,
    hour = hour,
    minute = minute,
    second = second
)
