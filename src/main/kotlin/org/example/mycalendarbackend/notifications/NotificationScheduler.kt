package org.example.mycalendarbackend.notifications

import org.example.mycalendarbackend.domain.dto.DateTime
import org.example.mycalendarbackend.domain.entity.CalendarEvent
import org.example.mycalendarbackend.extension.toRRuleRequest
import org.example.mycalendarbackend.extension.tomorrowMidnight
import org.example.mycalendarbackend.service.CalendarEventService
import org.example.mycalendarbackend.service.UserSequenceService
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.time.ZonedDateTime

@Component
internal class NotificationScheduler(
    private val notificationService: NotificationService,
    private val eventService: CalendarEventService,
    private val userSequenceService: UserSequenceService,
    private val restClient: RestClient
) {

    @Scheduled(cron = "0 0 0 * * *")
    fun scheduleNotifications() {
        val now = ZonedDateTime.now()
        // here we fetch the events that are already started or are going to start today
        val events = eventService.findAllByStartDateLessThan(now.tomorrowMidnight())
        // now we need to eliminate the past events (i.e. the events that are finished)
        // here we have two different scenarios: the event has end date or the event has occurrence count
        val filteredEvents = events.filter { event ->
            if (event.repeatingPattern?.until != null) {
                !event.repeatingPattern.until.isBefore(now)
            } else if (event.repeatingPattern?.occurrenceCount != null) {
                true
            } else { // should not happen
                false
            }
        }
        scheduleNotificationsForEvents(filteredEvents, now)
    }

    fun scheduleNotificationsForEventManually(event: CalendarEvent) {
        scheduleNotificationsForEvents(listOf(event), ZonedDateTime.now())
    }

    // one improvement I can think of is to further filter the events that have an instance on 'onDate' before we send them to this method
    private fun scheduleNotificationsForEvents(events: List<CalendarEvent>, onDate: ZonedDateTime) {
        events.forEach { event ->
            val userSequences = userSequenceService.findAllBySequenceId(event.sequenceId)
                .filter { it.notifyMinutesBefore != null }
            val date = restClient.post()
                .uri("/get-instance-for-event-on-day")
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    mapOf(
                        "rruleRequest" to event.toRRuleRequest(),
                        "date" to DateTime(
                            year = onDate.year,
                            month = onDate.monthValue,
                            day = onDate.dayOfMonth
                        )
                    )
                ).retrieve()
                .body(ZonedDateTime::class.java)
            if (date != null) {
                userSequences.forEach {
                    notificationService.save(
                        scheduledTime = date.minusMinutes(it.notifyMinutesBefore!!.toLong()),
                        event = event,
                        receiverId = it.userId
                    )
                }
            }
        }
    }

//    @EventListener(ApplicationReadyEvent::class)
//    @Async
//    fun execute() {
//        Thread.sleep(5000)
//        scheduleNotifications()
//    }

}
