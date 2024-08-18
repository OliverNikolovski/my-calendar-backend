package org.example.mycalendarbackend.service

import org.example.mycalendarbackend.domain.entity.SequenceOwner
import org.example.mycalendarbackend.domain.entity.SequenceOwnerPk
import org.example.mycalendarbackend.repository.SequenceOwnerRepository
import org.springframework.stereotype.Service

@Service
class SequenceOwnerService(
    private val repository: SequenceOwnerRepository
) {

    fun save(ownerId: Long, sequenceId: String) = repository.save(
        SequenceOwner(
            SequenceOwnerPk(
                ownerId, sequenceId
            )
        )
    )

    fun findAllSequencesByOwner(ownerId: Long): List<String> =
        repository.findAllById_OwnerId(ownerId).map { it.id.sequenceId }

    fun findAllOwnersForSequence(sequenceId: String): List<Long> =
        repository.findAllById_SequenceId(sequenceId).map { it.id.ownerId }

    fun isUserOwnerOfSequence(userId: Long, sequenceId: String): Boolean =
        repository.existsById_OwnerIdAndId_SequenceId(userId, sequenceId)

}