package org.example.mycalendarbackend.domain.entity

import jakarta.persistence.*
import org.example.mycalendarbackend.domain.base.BaseEntity

@Entity
@Table(schema = "calendar", name = "user_sequences")
data class UserSequence(

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "sequence_id", nullable = false)
    val sequenceId: String,

    @Column(name = "is_public", nullable = false)
    val isPublic: Boolean = false

) : BaseEntity<Long>()
