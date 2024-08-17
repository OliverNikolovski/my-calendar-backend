package org.example.mycalendarbackend.repository

import org.example.mycalendarbackend.domain.entity.SequenceOwner
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface UserSequenceRepository : JpaRepository<SequenceOwner, Long>, JpaSpecificationExecutor<SequenceOwner> {

    fun findAllById_UserId(userId: Long): List<SequenceOwner>

}
