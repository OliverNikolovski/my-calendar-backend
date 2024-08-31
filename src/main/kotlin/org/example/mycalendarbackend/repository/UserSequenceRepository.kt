package org.example.mycalendarbackend.repository

import org.example.mycalendarbackend.domain.entity.UserSequence
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface UserSequenceRepository : JpaRepository<UserSequence, Long>, JpaSpecificationExecutor<UserSequence> {

    fun findAllByUserId(userId: Long): List<UserSequence>

    fun findAllBySequenceId(sequenceId: String): List<UserSequence>

    fun findByUserIdAndSequenceId(userId: Long, sequenceId: String): UserSequence?

    fun findAllByUserIdAndIsPublic(userId: Long, public: Boolean): List<UserSequence>

}
