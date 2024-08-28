package org.example.mycalendarbackend.notifications

import org.example.mycalendarbackend.domain.entity.CalendarEvent
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
internal class NotificationService(
    private val repository: NotificationRepository,
    private val specificationBuilder: NotificationSpecificationBuilder
) {

    fun fetchNotificationsForProcessing(from: ZonedDateTime, to: ZonedDateTime): List<ScheduledNotification> =
        repository.findAll(specificationBuilder.pendingNotificationsInDateRangeJoinFetchEvent(from, to))

    fun save(scheduledTime: ZonedDateTime, event: CalendarEvent) = repository.save(
        ScheduledNotification(
            scheduledTime = scheduledTime,
            event = event,
            status = ScheduledNotificationStatus.PENDING
        )
    )

}