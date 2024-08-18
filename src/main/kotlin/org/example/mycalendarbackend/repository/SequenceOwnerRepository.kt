package org.example.mycalendarbackend.repository

import org.example.mycalendarbackend.domain.entity.SequenceOwner
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface SequenceOwnerRepository : JpaRepository<SequenceOwner, Long>, JpaSpecificationExecutor<SequenceOwner> {

    fun findAllById_OwnerId(ownerId: Long): List<SequenceOwner>

    fun findAllById_SequenceId(sequenceId: String): List<SequenceOwner>

    fun existsById_OwnerIdAndId_SequenceId(ownerId: Long, sequenceId: String): Boolean

}