package org.example.mycalendarbackend.service

import org.example.mycalendarbackend.domain.entity.SequenceOwner
import org.example.mycalendarbackend.domain.entity.SequenceOwnerPk
import org.example.mycalendarbackend.repository.UserSequenceRepository
import org.springframework.stereotype.Service

@Service
class UserSequenceService(
    private val repository: UserSequenceRepository
) {

    fun findAllSequencesByUserId(userId: Long): List<String> =
        repository.findAllById_UserId(userId).map { it.id.sequenceId }

    fun save(userId: Long, sequenceId: String) = repository.save(
        SequenceOwner(
            SequenceOwnerPk(userId, sequenceId)
        )
    )

}