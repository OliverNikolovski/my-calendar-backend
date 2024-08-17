package org.example.mycalendarbackend.domain.entity

import jakarta.persistence.*

@Entity
@Table(schema = "calendar", name = "user_sequences")
data class UserSequence(

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    val id: Long,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "sequence_id", nullable = false)
    val sequenceId: String

)
