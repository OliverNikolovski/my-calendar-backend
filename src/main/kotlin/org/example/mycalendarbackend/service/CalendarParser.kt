package org.example.mycalendarbackend.service

import jakarta.annotation.PostConstruct
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Recur
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.*
import org.example.mycalendarbackend.domain.entity.CalendarEvent
import org.example.mycalendarbackend.domain.entity.RepeatingPattern
import org.example.mycalendarbackend.domain.enums.Frequency
import org.example.mycalendarbackend.extension.withTimeFrom
import org.springframework.stereotype.Service
import java.io.StringReader
import java.nio.file.Files
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.io.path.Path
import kotlin.jvm.optionals.getOrNull

@Service
internal class CalendarParser(
    private val eventService: CalendarEventService
) {

    @PostConstruct
    fun saveImportedCalendarEvents() {
        val path = Path("C:\\Users\\OLIVER-PC\\Desktop\\test_calendar.ics")
        val content = Files.readString(path)
        val events = parseIcsContent(content)
        eventService.saveAll(events)
    }

    fun parseIcsContent(icsContent: String): List<CalendarEvent> {
        //System.setProperty("net.fortuna.ical4j.timezone.cache.impl", MapTimeZoneCache::class.java.name)
        val builder = CalendarBuilder()
        val calendar = builder.build(StringReader(icsContent)) // TODO: maybe handle exceptions
        val xWrTimezone = calendar.getProperty<XProperty>("X-WR-TIMEZONE").getOrNull()?.value
        val iCalEvents = calendar.getComponents<VEvent>("VEVENT")
        val mappedEvents = mutableListOf<CalendarEvent>()
        iCalEvents.forEach { vEvent ->
            val (eventsToCreate, eventToRemove) = mapVEventToCalendarEvent(vEvent, mappedEvents, xWrTimezone)
            if (eventToRemove != null) mappedEvents.removeIf { it === eventToRemove }
            mappedEvents.addAll(eventsToCreate)
        }
        return mappedEvents
    }

    private fun mapVEventToCalendarEvent(target: VEvent, events: List<CalendarEvent>, xWrTimezone: String? = null): EventMappingResult {
        val zoneId = ZoneId.of(xWrTimezone ?: target.tzId)
        val startDate = target.dtStart.withZoneSameInstant(zoneId)
        val endDate = target.dtEnd?.withZoneSameInstant(zoneId)
        val sequenceId = target.uidValue ?: SequenceGenerator.generateId()
        val repeatingPattern = target.recur?.let {
            val interval = it.interval.nullIfNegative()
            val frequency = Frequency.valueOf(it.frequency.name)
            val count = it.count.nullIfNegative()
            val until = it.until.toInstant().atZone(zoneId) // zavrshuva na kraj na den (sekunda pred polnok, vo UTC zona - Z)
            val weekDays = it.weekNoList.toTypedArray().ifEmpty { null }
            val setPos = it.setPosList?.firstOrNull() // should have been list of integers instead
            RepeatingPattern(
                frequency = frequency,
                weekDays = weekDays,
                setPos = setPos,
                interval = interval,
                occurrenceCount = count,
                until = until,
                rruleText = null,
                rruleString = null
            )
        }
        val calendarEvent = CalendarEvent(
            title = target.summaryValue,
            description = target.descriptionValue,
            startDate = target.dtStart.withZoneSameInstant(zoneId),
            duration = target.durationInMinutes ?: ChronoUnit.MINUTES.between(startDate, endDate).toInt(),
            sequenceId = sequenceId,
            repeatingPattern = repeatingPattern
        )
        if (calendarEvent.isNonRepeating) { // if it is non-repeating, we know it does not have RECURRENCE-ID
            return EventMappingResult(eventsToCreate = listOf(calendarEvent))
        }

        val recurrenceId = target.getProperty<RecurrenceId<ZonedDateTime>>("RECURRENCE-ID").getOrNull()?.date
            ?.withZoneSameInstant(zoneId)
        return recurrenceId?.let {
            val originalEvent = events.find { it.sequenceId == target.uidValue }!!
            val (previousOccurrence, nextOccurrence) = eventService.getPreviousAndNextOccurrencesPublic(
                event = originalEvent,
                referenceDate = calendarEvent.startDate.withTimeFrom(originalEvent.startDate)
            )
            val eventBeforeTarget = previousOccurrence?.let {
                originalEvent.copy(
                    repeatingPattern = repeatingPattern!!.copy(
                        until = previousOccurrence
                    )
                )
            }
            val eventAfterTarget = nextOccurrence?.let {
                originalEvent.copy(
                    startDate = nextOccurrence
                )
            }
            EventMappingResult(
                eventsToCreate = listOfNotNull(eventBeforeTarget, calendarEvent, eventAfterTarget),
                eventToRemove = originalEvent
            )
        } ?: EventMappingResult(eventsToCreate = listOf(calendarEvent))
    }

    private data class EventMappingResult(
        val eventsToCreate: List<CalendarEvent>,
        val eventToRemove: CalendarEvent? = null
    )

}

val VEvent.uidValue: String?
    get() = getProperty<Uid>("Uid").getOrNull()?.value

val VEvent.dtStartProperty: DtStart<ZonedDateTime>
    get() = getProperty<DtStart<ZonedDateTime>>("DTSTART").get()

val VEvent.dtStart: ZonedDateTime
    get() = dtStartProperty.date

val VEvent.tzId: String?
    get() = dtStartProperty.getParameters("TZID")?.firstOrNull()?.value

val VEvent.summaryValue: String?
    get() = summary.getOrNull()?.value

val VEvent.descriptionValue: String?
    get() = description.getOrNull()?.value

val VEvent.dtEnd: ZonedDateTime?
    get() = getProperty<DtEnd<ZonedDateTime>>("DTEND").getOrNull()?.date

val VEvent.durationInMinutes: Int?
    get() = getProperty<Duration>("DURATION").getOrNull()
        ?.duration
        ?.units
        ?.map { it.duration }
        ?.sumOf { it.toMinutes() }?.toInt()

val VEvent.recur: Recur<OffsetDateTime>?
    get() = getProperty<RRule<OffsetDateTime>>("RRULE").getOrNull()?.recur

fun Int.nullIfNegative(): Int? = if (this >= 0) this else null