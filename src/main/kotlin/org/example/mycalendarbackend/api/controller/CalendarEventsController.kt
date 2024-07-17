package org.example.mycalendarbackend.api.controller

import org.example.mycalendarbackend.api.request.DateRange
import org.example.mycalendarbackend.domain.dto.CalendarEventDto
import org.example.mycalendarbackend.domain.dto.CalendarEventInstancesContainer
import org.example.mycalendarbackend.domain.dto.CalendarEventInstancesContainer2
import org.example.mycalendarbackend.domain.enums.DeletionType
import org.example.mycalendarbackend.service.CalendarEventInstanceInfo
import org.example.mycalendarbackend.service.CalendarEventService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.ZonedDateTime

@RestController
@RequestMapping("/api/calendar-events")
class CalendarEventsController(
    private val service: CalendarEventService
) {

    @GetMapping
    fun test(): ResponseEntity<String> = ResponseEntity.ok("Hello, world!")

    @GetMapping("/{id}")
    fun getEventAndChildren(@PathVariable("id") eventId: Long) = service.getCalendarEventWithChildren(eventId)

    @GetMapping("/generate-event-instances/{id}")
    fun getEventInstances(@PathVariable("id") eventId: Long): ResponseEntity<CalendarEventInstancesContainer2> {
        val result = service.generateCalendarEventInstances(eventId)
        return if (result.isSuccess) {
            ResponseEntity.ok(result.getOrNull()!!)
        } else {
            ResponseEntity.badRequest().build()
        }
    }


    // ovoj go koristam
    @GetMapping("/generate-instances-for-events")
    fun generateInstancesForEvents(@RequestParam
                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                   from: ZonedDateTime): Map<String, List<CalendarEventInstanceInfo>> =
        service.generateInstanceForEvents2(from)

    @GetMapping("/generate-event-instances")
    fun getEventInstancesInRange(dateRange: DateRange) {

    }

    @PostMapping
    fun create(@RequestBody calendarEventDto: CalendarEventDto) = service.save(calendarEventDto)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long,
               @RequestParam deletionType: DeletionType,
               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) fromDate: ZonedDateTime) =
        service.delete(id, fromDate, deletionType)

}