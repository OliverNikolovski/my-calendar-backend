package org.example.mycalendarbackend.notifications

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Lock

internal interface NotificationRepository : JpaRepository<ScheduledNotification, Long>, JpaSpecificationExecutor<ScheduledNotification> {

    // TODO: SKIP LOCKED should be incorporated for more correct solution, maybe implement view that returns the notiications with FOR UPDATE SKIP LOCKED
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    override fun findAll(specification: Specification<ScheduledNotification>): List<ScheduledNotification>

}
