package org.example.mycalendarbackend.service

import org.example.mycalendarbackend.domain.entity.UserSequence
import org.example.mycalendarbackend.repository.UserSequenceRepository
import org.springframework.stereotype.Service

@Service
class UserSequenceService(
    private val repository: UserSequenceRepository
) {

    fun findAllSequencesByUserId(userId: Long): List<String> =
        repository.findAllByUserId(userId).map { it.sequenceId }

    fun save(userId: Long, sequenceId: String) = repository.save(
        UserSequence(
            userId = userId,
            sequenceId = sequenceId
        )
    )

}