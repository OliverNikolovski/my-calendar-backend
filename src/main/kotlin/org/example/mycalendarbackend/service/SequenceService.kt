package org.example.mycalendarbackend.service

import org.example.mycalendarbackend.domain.entity.CalendarEvent
import org.example.mycalendarbackend.domain.entity.UserSequence
import org.example.mycalendarbackend.domain.projection.UserSequenceMinutesProjection
import org.example.mycalendarbackend.domain.projection.UserSequenceVisibilityProjection
import org.example.mycalendarbackend.exception.CalendarEntityNotFoundException
import org.example.mycalendarbackend.extension.withBase
import org.springframework.stereotype.Service

@Service
internal class SequenceService(
    private val userSequenceService: UserSequenceService,
    private val sequenceOwnerService: SequenceOwnerService,
    private val userService: UserService
) {

    fun findAllSequencesForUser(userId: Long) = userSequenceService.findAllSequencesByUserId(userId)

    fun getUserSequencesVisibilityMap(userId: Long): Map<String, Boolean> =
        userSequenceService
            .findAllSequencesByUserId(userId, UserSequenceVisibilityProjection::class.java)
            .associate { it.sequenceId to it.isPublic }

    fun getUserSequencesMinutesMap(userId: Long): Map<String, Int?> =
        userSequenceService
            .findAllSequencesByUserId(userId, UserSequenceMinutesProjection::class.java)
            .associate { it.sequenceId to it.notifyMinutesBefore }

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

    fun generateSequenceId(): String = SequenceGenerator.generateId()

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

    fun saveNotificationConfigForEvent(event: CalendarEvent, minutes: Int?) {
        val userSequence = userSequenceService.findByUserIdAndSequenceId(
            userId = userService.getAuthenticatedUserId(),
            sequenceId = event.sequenceId
        ) ?: throw CalendarEntityNotFoundException("Event sequence does not exist for authenticated user")
        userSequenceService.saveNotificationConfiguration(userSequence, minutes)
    }

}