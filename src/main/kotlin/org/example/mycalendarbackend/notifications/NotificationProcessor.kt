package org.example.mycalendarbackend.notifications

import org.example.mycalendarbackend.domain.entity.CalendarEvent
import org.example.mycalendarbackend.domain.projection.UserProjection
import org.example.mycalendarbackend.service.EmailService
import org.example.mycalendarbackend.service.HandlebarsService
import org.example.mycalendarbackend.service.UserService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.ZonedDateTime

@Component
internal class NotificationProcessor(
    private val notificationService: NotificationService,
    private val emailService: EmailService,
    private val userService: UserService,
    private val handlebarsService: HandlebarsService
) {

    @Transactional
    @Scheduled(cron = "0 * * * * *")
    fun processScheduledNotifications() {
        val notifications = notificationService.fetchNotificationsForProcessing()
        notifications.forEach {
            val userProjection = userService.getUserProjectionByUserId(it.receiverId)
            emailService.sendEmail(
                to = userProjection.usernameField,
                subject = getSubject(it.event.title ?: "", it.event.startDate, it.scheduledTime),
                text = handlebarsService.getEmailTemplate(
                    buildContextObject(userProjection, it.event)
                )
            )
            notificationService.markNotificationAsProcessed(it)
        }
    }

    private fun getSubject(eventTitle: String, eventStart: ZonedDateTime, notificationScheduleTime: ZonedDateTime): String {
        val minutes = Duration.between(notificationScheduleTime, eventStart).toMinutes()
        return "MyCalendar Reminder: Your Event $eventTitle Starts in $minutes Minutes"
    }

    private fun buildContextObject(userProjection: UserProjection, event: CalendarEvent): EmailReminderContext =
        EmailReminderContext(
            userEmail = userProjection.usernameField,
            userFirstName = userProjection.firstName,
            eventTitle = event.title ?: "",
            eventDescription = event.description ?: "",
            eventStart = event.startDate
        )

}