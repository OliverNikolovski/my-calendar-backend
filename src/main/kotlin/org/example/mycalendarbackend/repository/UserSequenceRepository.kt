package org.example.mycalendarbackend.repository

import org.example.mycalendarbackend.domain.entity.UserSequence
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface UserSequenceRepository : JpaRepository<UserSequence, Long>, JpaSpecificationExecutor<UserSequence> {

    fun <T> findAllByUserId(userId: Long, type: Class<T>): List<T>

    fun findAllBySequenceId(sequenceId: String): List<UserSequence>

    fun findByUserIdAndSequenceId(userId: Long, sequenceId: String): UserSequence?

    fun findAllByUserIdAndIsPublic(userId: Long, public: Boolean): List<UserSequence>

}
