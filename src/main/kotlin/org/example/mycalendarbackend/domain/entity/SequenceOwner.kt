package org.example.mycalendarbackend.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.io.Serializable

@Embeddable
data class SequenceOwnerPk(
    @Column(name = "owner_id", nullable = false)
    val ownerId: Long,

    @Column(name = "sequence_id", nullable = false)
    val sequenceId: String
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}

@Entity
@Table(schema = "calendar", name = "owner_sequences")
data class SequenceOwner(

    @EmbeddedId
    val id: SequenceOwnerPk

//    @ManyToOne(fetch = FetchType.LAZY)
//    @MapsId("userId")  // Maps the userId in UserSequenceKey to this association
//    @JoinColumn(name = "user_id", nullable = false)
//    val user: User

)
