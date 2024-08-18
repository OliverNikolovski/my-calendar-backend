package org.example.mycalendarbackend.service

import org.springframework.stereotype.Service
import java.util.*

@Service
internal class CalendarEventSequenceService(
    private val userSequenceService: UserSequenceService,
    private val sequenceOwnerService: SequenceOwnerService,
    private val userService: UserService
) {

    fun findAllSequencesForUser(userId: Long) = userSequenceService.findAllSequencesByUserId(userId)

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

}