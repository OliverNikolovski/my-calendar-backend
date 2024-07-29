package org.example.mycalendarbackend.annotation

import kotlin.annotation.AnnotationRetention.*
import kotlin.annotation.AnnotationTarget.*

@Target(VALUE_PARAMETER)
@Retention(RUNTIME)
annotation class TimeFormatter(val format: Format) {
    enum class Format {
        TWELVE_HOUR,
        TWENTY_FOUR_HOUR
    }
}
