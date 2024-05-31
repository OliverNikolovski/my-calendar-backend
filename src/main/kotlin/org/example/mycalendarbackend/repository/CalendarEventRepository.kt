package org.example.mycalendarbackend.repository

import org.example.mycalendarbackend.domain.entity.CalendarEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime

@Repository
internal interface CalendarEventRepository : JpaRepository<CalendarEvent, Long>, JpaSpecificationExecutor<CalendarEvent> {

    fun findAllByParentId(parentId: Long): List<CalendarEvent>

    fun findAllByIdOrParentId(id: Long, parentId: Long): List<CalendarEvent>

    fun findAllByStartDateGreaterThanEqualOrEndDateLessThanEqual(from: ZonedDateTime, to: ZonedDateTime): List<CalendarEvent>

    fun findAllByStartDateGreaterThanEqual(from: ZonedDateTime): List<CalendarEvent>

}