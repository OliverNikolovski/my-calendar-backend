package org.example.mycalendarbackend.api

import org.example.mycalendarbackend.domain.dto.CalendarEventDto
import org.example.mycalendarbackend.service.CalendarEventService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/calendar-events")
class CalendarEventsController(
    private val service: CalendarEventService
) {

    @GetMapping
    fun test(): ResponseEntity<String> = ResponseEntity.ok("Hello, world!")

    @GetMapping("/{id}")
    fun getEventAndChildren(@PathVariable("id") eventId: Long) = service.getCalendarEventWithChildren(eventId)

    @PostMapping
    fun create(@RequestBody calendarEventDto: CalendarEventDto) = service.save(calendarEventDto)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)

}