package org.example.mycalendarbackend.domain.entity

import jakarta.persistence.*
import org.example.mycalendarbackend.domain.base.BaseEntity
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

    @Column(name = "duration")
    val duration: Int,

    @JoinColumn(name = "repeating_pattern_id", nullable = true)
    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    val repeatingPattern: RepeatingPattern?,

    @JoinColumn(name = "sequence_id")
    val sequenceId: String

) : BaseEntity<Long>() {

    val isRepeating: Boolean
        get() = repeatingPattern != null

    val isNonRepeating: Boolean
        get() = !isRepeating

}
