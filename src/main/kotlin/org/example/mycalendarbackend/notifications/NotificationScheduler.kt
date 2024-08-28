package org.example.mycalendarbackend.notifications

import org.example.mycalendarbackend.domain.dto.DateTime
import org.example.mycalendarbackend.extension.atStartOfDay
import org.example.mycalendarbackend.extension.toRRuleRequest
import org.example.mycalendarbackend.extension.tomorrowMidnight
import org.example.mycalendarbackend.service.CalendarEventService
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.time.ZonedDateTime

@Component
internal class NotificationScheduler(
    private val notificationService: NotificationService,
    private val eventService: CalendarEventService,
    private val restClient: RestClient
) {

    @Scheduled(cron = "0 0 0 * * *")
    fun scheduleNotifications() {
        val now = ZonedDateTime.now()
        // here we fetch the events that are already started or are going to start today
        val events = eventService.findAllByStartDateLessThan(now.tomorrowMidnight())
        // now we need to eliminate the past events (i.e. the events that are finished)
        // here we have to different scenarios: the event has end date or the event has occurrence count
        val filteredEvents = events.filter { event ->
            if (event.repeatingPattern?.until != null) {
                !event.repeatingPattern.until.isBefore(now)
            } else if (event.repeatingPattern?.occurrenceCount != null) {
                true
            } else { // should not happen
                false
            }
        }
        filteredEvents.forEach { event ->
            val date = restClient.post()
                .uri("/calculate-previous-execution")
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    mapOf(
                        "rruleRequest" to event.toRRuleRequest(),
                        "date" to DateTime(
                            year = now.year,
                            month = now.monthValue,
                            day = now.dayOfMonth
                        )
                    )
                ).retrieve()
                .body(ZonedDateTime::class.java)
            date?.run {
                notificationService.save(
                    scheduledTime = this,
                    event = event
                )
            }
        }

    }

}