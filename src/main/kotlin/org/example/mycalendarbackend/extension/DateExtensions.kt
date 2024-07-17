package org.example.mycalendarbackend.extension

import org.example.mycalendarbackend.domain.dto.DateTime
import java.time.ZonedDateTime

fun ZonedDateTime.atStartOfDay(): ZonedDateTime =
    withHour(0).withMinute(0).withSecond(0).withNano(0)

fun ZonedDateTime.endOfPreviousDay(): ZonedDateTime =
    atStartOfDay().minusSeconds(1)

fun ZonedDateTime.toDateTime(): DateTime = DateTime(
    year = year,
    month = monthValue,
    day = dayOfMonth,
    hour = hour,
    minute = minute,
    second = second
)

fun ZonedDateTime.plusOneDay(): ZonedDateTime = plusDays(1)