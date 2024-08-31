package org.example.mycalendarbackend.notifications

import org.example.mycalendarbackend.domain.entity.CalendarEvent
import org.example.mycalendarbackend.domain.entity.User
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
internal class NotificationService(
    private val repository: NotificationRepository,
    private val specificationBuilder: NotificationSpecificationBuilder
) {

    fun fetchNotificationsForProcessing(from: ZonedDateTime, to: ZonedDateTime): List<ScheduledNotification> =
        repository.findAll(specificationBuilder.pendingNotificationsInDateRangeJoinFetchEvent(from, to))

    fun save(scheduledTime: ZonedDateTime, event: CalendarEvent, receiverId: Long) = repository.save(
        ScheduledNotification(
            scheduledTime = scheduledTime,
            event = event,
            receiverId = receiverId,
            status = ScheduledNotificationStatus.PENDING
        )
    )

}