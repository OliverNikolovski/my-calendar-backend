package org.example.mycalendarbackend.notifications

import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

@Component
class NotificationSpecificationBuilder(
    private val specification: NotificationSpecification
) {

    fun pendingNotificationsInDateRangeJoinFetchEvent(from: ZonedDateTime, to: ZonedDateTime): Specification<ScheduledNotification> =
        Specification.where(specification.hasPendingStatus())
            .and(specification.scheduledTimeBetween(from, to))
            .and(specification.joinFetchEvent())

    fun pendingNotificationsJoinFetchEvent(): Specification<ScheduledNotification> =
        Specification.where(specification.hasPendingStatus()).and(specification.joinFetchEvent())

}