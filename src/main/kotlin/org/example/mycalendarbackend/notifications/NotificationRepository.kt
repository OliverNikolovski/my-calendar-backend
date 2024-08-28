package org.example.mycalendarbackend.notifications

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

internal interface NotificationRepository : JpaRepository<ScheduledNotification, Long>, JpaSpecificationExecutor<ScheduledNotification>
