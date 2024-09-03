package org.example.mycalendarbackend.listeners

import org.example.mycalendarbackend.events.EmailNotificationAddedOrUpdated
import org.example.mycalendarbackend.notifications.NotificationScheduler
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
internal class CalendarListener(
    private val notificationScheduler: NotificationScheduler
) {

    @EventListener(EmailNotificationAddedOrUpdated::class)
    fun emailNotificationListener(event: EmailNotificationAddedOrUpdated) {
        notificationScheduler.scheduleNotificationsForEventManually(event.event)
    }

}