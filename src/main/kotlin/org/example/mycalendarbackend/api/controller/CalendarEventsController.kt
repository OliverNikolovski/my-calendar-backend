package org.example.mycalendarbackend.api.controller

import org.example.mycalendarbackend.domain.dto.CalendarEventDto
import org.example.mycalendarbackend.domain.enums.DeletionType
import org.example.mycalendarbackend.service.CalendarEventInstanceInfo
import org.example.mycalendarbackend.service.CalendarEventService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.ZonedDateTime

@RestController
@RequestMapping("/api/calendar-events")
class CalendarEventsController(
    private val service: CalendarEventService
) {

    @GetMapping("/generate-instances-for-events")
    fun generateInstancesForEvents(@RequestParam
                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                   from: ZonedDateTime): Map<String, List<CalendarEventInstanceInfo>> =
        service.generateInstanceForEvents(from)

    @GetMapping("/generate-instances-for-event-id")
    fun generateInstancesForEventId(@RequestParam eventId: Long): Map<String, List<CalendarEventInstanceInfo>> =
        service.generateInstancesForEvent(eventId)

    @PostMapping
    fun create(@RequestBody calendarEventDto: CalendarEventDto) = service.save(calendarEventDto)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long,
               @RequestParam deletionType: DeletionType,
               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) fromDate: ZonedDateTime,
               @RequestParam order: Int) =
        service.delete(id, fromDate, deletionType, order)

}