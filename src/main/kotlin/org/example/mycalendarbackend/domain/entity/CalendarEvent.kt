package org.example.mycalendarbackend.domain.entity

import jakarta.persistence.*
import org.example.mycalendarbackend.domain.base.BaseEntity
import org.hibernate.annotations.Formula
import java.time.ZonedDateTime

@Entity
@Table(schema = "calendar", name = "calendar_events")
data class CalendarEvent(

    @Column(name = "title")
    val title: String?,

    @Column(name = "description")
    val description: String?,

    @Column(name = "start_date")
    val startDate: ZonedDateTime,

    @Column(name = "end_date")
    val endDate: ZonedDateTime?,

    @Column(name = "duration")
    val duration: Int,

    @JoinColumn(name = "repeating_pattern_id")
    @ManyToOne(fetch = FetchType.LAZY)
    val repeatingPattern: RepeatingPattern,

    @JoinColumn(name = "parent_id")
    @ManyToOne(fetch = FetchType.LAZY)
    val parent: CalendarEvent?

) : BaseEntity<Long>() {

    @Formula("select parent_id from calendar.calendar_events e where e.id = id")
    val parentId: Long? = null

}
