package org.example.mycalendarbackend.service

import org.example.mycalendarbackend.domain.entity.UserSequence
import org.example.mycalendarbackend.extension.withBase
import org.springframework.stereotype.Service
import java.util.*

@Service
internal class CalendarEventSequenceService(
    private val userSequenceService: UserSequenceService,
    private val sequenceOwnerService: SequenceOwnerService,
    private val userService: UserService
) {

    fun findAllSequencesForUser(userId: Long) = userSequenceService.findAllSequencesByUserId(userId)

    fun findAllPublicSequencesForUser(userId: Long) = userSequenceService.findAllPublicSequencesByUserId(userId)

    fun findAllSequencesForAuthenticatedUser() = findAllSequencesForUser(
        userService.getAuthenticatedUserId()
    )

    fun findAllSequencesOwnedBy(ownerId: Long): List<String> =
        sequenceOwnerService.findAllSequencesByOwner(ownerId)

    fun findAllOwnersForSequence(sequenceId: String): List<Long> =
        sequenceOwnerService.findAllOwnersForSequence(sequenceId)

    fun saveSequenceForUser(userId: Long, sequenceId: String) = userSequenceService.save(userId, sequenceId)

    fun saveSequenceOwner(ownerId: Long, sequenceId: String) = sequenceOwnerService.save(ownerId, sequenceId)

    fun saveSequenceForUserAndOwner(userId: Long, sequenceId: String) {
        saveSequenceForUser(userId, sequenceId)
        saveSequenceOwner(userId, sequenceId)
    }

    fun saveSequenceForUserAndOwnerOnAuthenticatedUser(sequenceId: String) {
        val userId = userService.getAuthenticatedUserId()
        saveSequenceForUserAndOwner(userId, sequenceId)
    }

    fun isAuthenticatedUserOwnerOfSequence(sequenceId: String): Boolean =
        sequenceOwnerService.isUserOwnerOfSequence(
            userId = userService.getAuthenticatedUserId(),
            sequenceId = sequenceId
        )

    fun generateSequenceId(): String = UUID.randomUUID().toString()

    fun findUserSequenceForAuthenticatedUser(sequenceId: String): UserSequence = checkNotNull(
        userSequenceService.findByUserIdAndSequenceId(
            userId = userService.getAuthenticatedUserId(),
            sequenceId = sequenceId
        )
    ) { "Event sequence does not exist" }

    fun updateEventSequenceVisibility(sequenceId: String, isPublic: Boolean) {
        val userSequence = findUserSequenceForAuthenticatedUser(sequenceId)
        userSequenceService.save(
            userSequence.copy(isPublic = isPublic).withBase(userSequence)
        )
    }

    fun isSequenceForAuthenticatedUserPublic(sequenceId: String): Boolean =
        findUserSequenceForAuthenticatedUser(sequenceId).isPublic

}