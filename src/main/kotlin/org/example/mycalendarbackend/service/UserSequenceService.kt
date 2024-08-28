package org.example.mycalendarbackend.service

import org.example.mycalendarbackend.domain.entity.UserSequence
import org.example.mycalendarbackend.extension.withBase
import org.example.mycalendarbackend.repository.UserSequenceRepository
import org.springframework.stereotype.Service
import kotlin.math.min

@Service
class UserSequenceService(
    private val repository: UserSequenceRepository
) {

    fun findAllSequencesByUserId(userId: Long): List<String> =
        repository.findAllByUserId(userId).map { it.sequenceId }

    fun findAllPublicSequencesByUserId(userId: Long): List<String> =
        repository.findAllByUserIdAndIsPublic(userId, true).map { it.sequenceId }


    fun findByUserIdAndSequenceId(userId: Long, sequenceId: String): UserSequence? =
        repository.findByUserIdAndSequenceId(userId, sequenceId)

    fun save(userId: Long, sequenceId: String) = repository.save(
        UserSequence(
            userId = userId,
            sequenceId = sequenceId
        )
    )

    fun save(userSequence: UserSequence) = repository.save(userSequence)

    fun saveNotificationConfiguration(userSequence: UserSequence, minutes: Int) =
        repository.save(
            userSequence.copy(notifyMinutesBefore = minutes).withBase(userSequence)
        )

}