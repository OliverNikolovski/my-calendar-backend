package org.example.mycalendarbackend.notifications

import jakarta.persistence.*
import org.example.mycalendarbackend.domain.base.BaseEntity
import org.example.mycalendarbackend.domain.entity.CalendarEvent
import org.hibernate.annotations.JdbcType
import org.hibernate.dialect.PostgreSQLEnumJdbcType
import java.time.ZonedDateTime

enum class ScheduledNotificationStatus {
    PENDING, PROCESSED
}

@Entity
@Table(schema = "calendar", name = "scheduled_notifications")
data class ScheduledNotification(

    @Column(name = "scheduled_time", nullable = false)
    val scheduledTime: ZonedDateTime,

    @Column(name = "event_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    val event: CalendarEvent,

    @Enumerated
    @JdbcType(PostgreSQLEnumJdbcType::class)
    val status: ScheduledNotificationStatus

) : BaseEntity<Long>()
