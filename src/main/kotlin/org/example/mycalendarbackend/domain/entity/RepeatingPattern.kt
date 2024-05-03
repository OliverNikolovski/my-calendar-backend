package org.example.mycalendarbackend.domain.entity

import io.hypersistence.utils.hibernate.type.array.IntArrayType
import jakarta.persistence.*
import org.example.mycalendarbackend.domain.base.BaseEntity
import org.hibernate.annotations.Type
import java.time.ZonedDateTime

enum class Frequency {
    YEARLY,
    MONTHLY,
    WEEKLY,
    DAILY
}

@Entity
@Table(schema = "calendar", name = "repeating_patterns")
data class RepeatingPattern(

    @Column(name = "frequency")
    @Enumerated(EnumType.ORDINAL)
    val frequency: Frequency,

    @Column(name = "week_days", columnDefinition = "_int4")
    @Type(IntArrayType::class)
    val weekDays: Array<Int>?,

    @Column(name = "set_pos")
    val setPos: Int?,

    @Column(name = "interval")
    val interval: Int?,

    @Column(name = "occurrence_count")
    val occurrenceCount: Int?,

    @Column(name = "rrule_text")
    val rruleText: String?,

    @Column(name = "rrule_string")
    val rruleString: String?,

    @Column(name = "start")
    val start: ZonedDateTime?,

    @Column(name = "until")
    val until: ZonedDateTime?

) : BaseEntity<Long>() {

    override fun equals(other: Any?): Boolean = super.equals(other)

    override fun hashCode(): Int = super.hashCode()

}
