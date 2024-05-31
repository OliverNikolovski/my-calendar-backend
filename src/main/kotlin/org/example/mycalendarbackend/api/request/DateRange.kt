package org.example.mycalendarbackend.api.request

import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.RequestParam
import java.time.ZonedDateTime

data class DateRange(

    @RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val from: ZonedDateTime,

    @RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val end: ZonedDateTime

)
