package org.example.mycalendarbackend.api

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/calendar-events")
class CalendarEventsController {

    @GetMapping
    fun test(): ResponseEntity<String> = ResponseEntity.ok("Hello, world!")

}