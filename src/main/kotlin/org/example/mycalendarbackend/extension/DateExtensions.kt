package org.example.mycalendarbackend.extension

import org.example.mycalendarbackend.domain.dto.DateTime
import java.time.LocalTime
import java.time.ZonedDateTime

fun ZonedDateTime.atStartOfDay(): ZonedDateTime =
    withHour(0).withMinute(0).withSecond(0).withNano(0)

fun ZonedDateTime.endOfPreviousDay(): ZonedDateTime =
    atStartOfDay().minusSeconds(1)

fun ZonedDateTime.tomorrowMidnight(): ZonedDateTime = plusOneDay().atStartOfDay()

fun ZonedDateTime.toDateTime(): DateTime = DateTime(
    year = year,
    month = monthValue,
    day = dayOfMonth,
    hour = hour,
    minute = minute,
    second = second
)

fun ZonedDateTime.plusOneDay(): ZonedDateTime = plusDays(1)

fun ZonedDateTime.withTimeFrom(other: ZonedDateTime): ZonedDateTime =
    withHour(other.hour).withMinute(other.minute).withSecond(other.second).withNano(other.nano)

fun ZonedDateTime.isEqualIgnoreSeconds(other: ZonedDateTime): Boolean =
    year == other.year && month == other.month && dayOfMonth == other.dayOfMonth && hour == other.hour && minute == other.minute

fun ZonedDateTime.isAfterOrEqualIgnoreSeconds(other: ZonedDateTime): Boolean {
    val thisDateTransformed = this.withSecond(0).withNano(0)
    val otherDateTransformed = other.withSecond(0).withNano(0)
    return thisDateTransformed.isEqual(otherDateTransformed) || thisDateTransformed.isAfter(otherDateTransformed)
}

fun ZonedDateTime.isBeforeOrEqualIgnoreSeconds(other: ZonedDateTime): Boolean {
    val thisDateTransformed = this.withSecond(0).withNano(0)
    val otherDateTransformed = other.withSecond(0).withNano(0)
    return thisDateTransformed.isEqual(otherDateTransformed) || thisDateTransformed.isBefore(otherDateTransformed)
}

fun ZonedDateTime.isInPastComparingTimeOnly(): Boolean {
    val currentTime = LocalTime.now(this.zone)
    val givenTime = this.toLocalTime()
    return givenTime.isBefore(currentTime)
}

fun ZonedDateTime.withYearMonthAndDayFrom(other: ZonedDateTime) =
    withYear(other.year).withMonth(other.monthValue).withDayOfMonth(other.dayOfMonth)

fun ZonedDateTime.withCurrentYearMonthAndDay() = withYearMonthAndDayFrom(ZonedDateTime.now())

