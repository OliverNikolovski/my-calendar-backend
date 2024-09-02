package org.example.mycalendarbackend.notifications

import org.example.mycalendarbackend.domain.entity.CalendarEvent
import org.example.mycalendarbackend.domain.projection.UserProjection
import org.example.mycalendarbackend.extension.isAfterOrEqualIgnoreSeconds
import org.example.mycalendarbackend.extension.isBeforeOrEqualIgnoreSeconds
import org.example.mycalendarbackend.extension.withCurrentYearMonthAndDay
import org.example.mycalendarbackend.extension.withYearMonthAndDayFrom
import org.example.mycalendarbackend.service.EmailService
import org.example.mycalendarbackend.service.HandlebarsService
import org.example.mycalendarbackend.service.UserService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@Component
internal class NotificationProcessor(
    private val notificationService: NotificationService,
    private val emailService: EmailService,
    private val userService: UserService,
    private val handlebarsService: HandlebarsService
) {

    private val eventDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val eventTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    @Transactional
    //@Scheduled(cron = "0 * * * * *")
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    fun processScheduledNotifications() {
        val notifications = notificationService.fetchNotificationsForProcessing()
        notifications.filter {
          val now = ZonedDateTime.now()
          now.isAfterOrEqualIgnoreSeconds(it.scheduledTime) &&
                  now.isBeforeOrEqualIgnoreSeconds(it.event.startDate.withCurrentYearMonthAndDay())
        }.forEach {
            val userProjection = userService.getUserProjectionByUserId(it.receiverId)
            val eventInstanceStart = it.event.startDate.withCurrentYearMonthAndDay()
            emailService.sendEmail(
                to = userProjection.usernameField,
                subject = getSubject(it.event.title ?: "", eventInstanceStart),
                text = handlebarsService.getEmailTemplate(
                    buildContextObject(userProjection, it.event, eventInstanceStart)
                )
            )
            notificationService.markNotificationAsProcessed(it)
        }
    }

    private fun getSubject(eventTitle: String, eventInstanceStart: ZonedDateTime): String {
        val minutes = Duration.between(ZonedDateTime.now(), eventInstanceStart).toMinutes()
        return "MyCalendar Reminder: Your Event $eventTitle Starts in $minutes Minutes"
    }

    private fun buildContextObject(userProjection: UserProjection,
                                   event: CalendarEvent,
                                   eventInstanceStart: ZonedDateTime): EmailReminderContext =
        EmailReminderContext(
            userEmail = userProjection.usernameField, // not used, can be removed
            userFirstName = userProjection.name,
            eventTitle = event.title ?: "",
            eventDescription = event.description ?: "",
            eventDate = ZonedDateTime.now().withZoneSameInstant(ZoneId.systemDefault()).format(eventDateFormatter),
            eventTime = event.startDate.withZoneSameInstant(ZoneId.systemDefault()).format(eventTimeFormatter),
            minutesBefore = Duration.between(ZonedDateTime.now(), eventInstanceStart).toMinutes()
        )

}