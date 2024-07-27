package org.example.mycalendarbackend.repository

import org.example.mycalendarbackend.domain.entity.CalendarEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime

@Repository
internal interface CalendarEventRepository : JpaRepository<CalendarEvent, Long>, JpaSpecificationExecutor<CalendarEvent> {

    fun findAllByStartDateGreaterThanEqual(from: ZonedDateTime): List<CalendarEvent>

    fun findAllBySequenceIdAndStartDateGreaterThanEqual(sequenceId: String, fromDate: ZonedDateTime): List<CalendarEvent>

    fun deleteAllBySequenceId(sequenceId: String)

}