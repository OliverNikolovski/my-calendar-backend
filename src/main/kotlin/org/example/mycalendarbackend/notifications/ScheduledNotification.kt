package org.example.mycalendarbackend.notifications

import jakarta.persistence.*
import org.example.mycalendarbackend.domain.base.BaseEntity
import org.example.mycalendarbackend.domain.entity.CalendarEvent
import org.hibernate.annotations.JdbcType
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.dialect.PostgreSQLEnumJdbcType
import org.hibernate.type.SqlTypes
import java.time.ZonedDateTime

enum class ScheduledNotificationStatus {
    PENDING, PROCESSED
}

@Entity
@Table(schema = "calendar", name = "scheduled_notifications")
data class ScheduledNotification(

    @Column(name = "scheduled_time", nullable = false)
    val scheduledTime: ZonedDateTime,

    @JoinColumn(name = "event_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    val event: CalendarEvent,

//    @ManyToOne(fetch = FetchType.EAGER, optional = false)
//    @JoinColumn(name = "receiver_id", nullable = false)
//    val receiver: User,

    @Column(name = "receiver_id", nullable = false)
    val receiverId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "\"calendar\".\"scheduled_notification_status\"")
    @JdbcType(PostgreSQLEnumJdbcType::class)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    val status: ScheduledNotificationStatus

) : BaseEntity<Long>()
