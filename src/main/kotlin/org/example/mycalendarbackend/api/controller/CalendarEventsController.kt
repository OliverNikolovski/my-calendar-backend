package org.example.mycalendarbackend.api.controller

import org.example.mycalendarbackend.api.request.CalendarEventCreationRequest
import org.example.mycalendarbackend.api.request.CalendarEventUpdateRequest
import org.example.mycalendarbackend.api.request.EventSequenceVisibilityUpdateRequest
import org.example.mycalendarbackend.api.request.ShareEventRequest
import org.example.mycalendarbackend.domain.enums.ActionType
import org.example.mycalendarbackend.domain.result.CalendarImportResult
import org.example.mycalendarbackend.extension.toDto
import org.example.mycalendarbackend.service.CalendarEventInstanceInfo
import org.example.mycalendarbackend.service.CalendarEventService
import org.example.mycalendarbackend.service.CalendarParserService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.web.bind.annotation.*
import java.time.ZonedDateTime
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/calendar-events")
class CalendarEventsController internal constructor(
    private val service: CalendarEventService,
    private val calendarParserService: CalendarParserService
) {

    @GetMapping("/test")
    fun test(): String {
        return "test"
    }

    @GetMapping("/generate-calendar-event-instances-for-authenticated-user")
    fun generateEventInstancesForAuthenticatedUser(): Map<String, List<CalendarEventInstanceInfo>> =
        service.generateEventInstancesForAuthenticatedUser()

    @GetMapping("/generate-calendar-event-instances-for-user/{userId}")
    fun generateEventInstancesForUser(@PathVariable userId: Long): Map<String, List<CalendarEventInstanceInfo>> =
        service.generateEventInstancesForUser(userId)

    @GetMapping("/generate-instances-for-event-id")
    fun generateInstancesForEventId(@RequestParam eventId: Long): Map<String, List<CalendarEventInstanceInfo>> =
        service.generateInstancesForEvent(eventId)

    @GetMapping("/event-instances-for-sequence/{sequenceId}")
    fun getEventInstancesForSequence(@PathVariable sequenceId: String): Map<String, List<CalendarEventInstanceInfo>> =
        service.generateInstancesForSequence(sequenceId)

    @PostMapping
    fun create(@RequestBody request: CalendarEventCreationRequest) = service.save(request.toDto())

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

    @PatchMapping("/update-event-visibility")
    fun updateEventSequenceVisibility(@RequestBody request: EventSequenceVisibilityUpdateRequest) =
        service.updateEventSequenceVisibility(request.sequenceId, request.isPublic)

    @PatchMapping("/update-calendar-visibility")
    fun updateCalendarVisibility(@RequestParam isPublic: Boolean) = service.updateVisibilityForAllAuthenticatedUserSequences(isPublic)

    @GetMapping("/export")
    @ResponseBody
    fun exportCalendar(): ResponseEntity<ByteArray> {
        val icsData = service.createIcsFileForAuthenticatedUser()

        val headers = HttpHeaders()
        headers.contentType = MediaType("text", "calendar")
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"calendar.ics\"")

        return ResponseEntity.ok()
            .headers(headers)
            .body(icsData.toByteArray())
    }

    @PostMapping("/import", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun importCalendar(@RequestParam file: MultipartFile): ResponseEntity<String> {
        val result = calendarParserService.importIcsFile(file)
        return ResponseEntity.status(result.httpStatus).body(result.message)
    }

    @PatchMapping("/add-or-update-email-notification-config")
    fun addOrUpdateEmailNotificationForEvent(
        @RequestParam eventId: Long,
        @RequestParam minutes: Int
    ) = service.addOrUpdateEmailNotificationForEvent(eventId, minutes)

}