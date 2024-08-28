package org.example.mycalendarbackend.notifications

import jakarta.persistence.criteria.JoinType
import org.example.mycalendarbackend.domain.entity.CalendarEvent
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

@Component
class NotificationSpecification {

    fun hasPendingStatus(): Specification<ScheduledNotification> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.equal(root.get<ScheduledNotificationStatus>("status"), ScheduledNotificationStatus.PENDING)
        }
    }

    fun scheduledTimeBetween(start: ZonedDateTime, end: ZonedDateTime): Specification<ScheduledNotification> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.between(root.get("scheduledTime"), start, end)
        }
    }

    fun joinFetchEvent(): Specification<ScheduledNotification> {
        return Specification { root, query, criteriaBuilder ->
            root.fetch<CalendarEvent, ScheduledNotification>("event", JoinType.INNER)
            query.distinct(true) // Ensure the results are distinct
            criteriaBuilder.conjunction() // No additional condition, just perform the join
        }
    }


}