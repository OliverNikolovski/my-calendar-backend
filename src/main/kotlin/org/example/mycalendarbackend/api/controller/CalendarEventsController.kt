package org.example.mycalendarbackend.api.controller

import org.example.mycalendarbackend.api.request.CalendarEventCreationRequest
import org.example.mycalendarbackend.api.request.CalendarEventUpdateRequest
import org.example.mycalendarbackend.api.request.ShareEventRequest
import org.example.mycalendarbackend.domain.enums.ActionType
import org.example.mycalendarbackend.service.CalendarEventInstanceInfo
import org.example.mycalendarbackend.service.CalendarEventService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.ZonedDateTime

@RestController
@RequestMapping("/api/calendar-events")
class CalendarEventsController internal constructor(
    private val service: CalendarEventService
) {

    @GetMapping("/test")
    fun test(): String {
        return "test"
    }

    @GetMapping("/generate-instances-for-events")
    fun generateInstancesForEvents(@RequestParam
                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                   from: ZonedDateTime): Map<String, List<CalendarEventInstanceInfo>> =
        service.generateInstanceForEvents(from)

    @GetMapping("/generate-calendar-event-instances-for-user")
    fun generateEventInstancesForAuthenticatedUser(): Map<String, List<CalendarEventInstanceInfo>> =
        service.generateEventInstancesForAuthenticatedUser()

    @GetMapping("/generate-instances-for-event-id")
    fun generateInstancesForEventId(@RequestParam eventId: Long): Map<String, List<CalendarEventInstanceInfo>> =
        service.generateInstancesForEvent(eventId)

    @GetMapping("/event-instances-for-sequence/{sequenceId}")
    fun getEventInstancesForSequence(@PathVariable sequenceId: String): Map<String, List<CalendarEventInstanceInfo>> =
        service.generateInstancesForSequence(sequenceId)

    @PostMapping
    fun create(@RequestBody request: CalendarEventCreationRequest) = service.save(request)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long,
               @RequestParam actionType: ActionType,
               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) fromDate: ZonedDateTime,
               @RequestParam order: Int) =
        service.delete(id, fromDate, actionType, order)

    @PatchMapping()
    fun update(@RequestBody updateRequest: CalendarEventUpdateRequest) = service.update(updateRequest)

    @PostMapping("/share")
    fun shareEventSequenceWithUser(@RequestBody request: ShareEventRequest) = service.shareEventSequenceWithUser(request)

}