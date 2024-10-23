package org.example.mycalendarbackend.service

import org.example.mycalendarbackend.api.request.CalendarEventCreationRequest
import org.example.mycalendarbackend.api.request.CalendarEventUpdateRequest
import org.example.mycalendarbackend.api.request.ShareEventRequest
import org.example.mycalendarbackend.domain.dto.*
import org.example.mycalendarbackend.domain.entity.CalendarEvent
import org.example.mycalendarbackend.domain.entity.RepeatingPattern
import org.example.mycalendarbackend.domain.enums.ActionType
import org.example.mycalendarbackend.events.EmailNotificationAddedOrUpdated
import org.example.mycalendarbackend.exception.CalendarEntityNotFoundException
import org.example.mycalendarbackend.exception.NotAuthorizedException
import org.example.mycalendarbackend.extension.*
import org.example.mycalendarbackend.repository.CalendarEventRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestClient
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
internal class CalendarEventService(
    private val repository: CalendarEventRepository,
    private val restClient: RestClient,
    private val repeatingPatternService: RepeatingPatternService,
    private val sequenceService: SequenceService,
    private val userService: UserService,
    private val eventPublisher: ApplicationEventPublisher
) {

    fun findAllByStartDateLessThan(targetDate: ZonedDateTime) = repository.findAllByStartDateLessThan(targetDate)

    fun findAllByStartDateGreaterThanEqual(from: ZonedDateTime) = repository.findAllByStartDateGreaterThanEqual(from)

    fun generateInstanceForEvents(from: ZonedDateTime): Map<String, List<CalendarEventInstanceInfo>> {
        val events = findAllByStartDateGreaterThanEqual(from)
        return createEventInstancesMapFromEvents(events)
    }

    fun generateEventInstancesForAuthenticatedUser(): Map<String, List<CalendarEventInstanceInfo>> {
        val userSequences = sequenceService.findAllSequencesForAuthenticatedUser()
        val userEvents = repository.findAllBySequenceIdIn(userSequences)
        return createEventInstancesMapFromEvents(userEvents)
    }

    fun generateEventInstancesForUser(userId: Long): Map<String, List<CalendarEventInstanceInfo>> {
        val userSequences = sequenceService.findAllPublicSequencesForUser(userId)
        val userEvents = repository.findAllBySequenceIdIn(userSequences)
        return createEventInstancesMapFromEvents(userEvents, false)
    }

    fun generateInstancesForSequence(sequenceId: String): Map<String, List<CalendarEventInstanceInfo>> {
        val events = repository.findAllBySequenceId(sequenceId)
        return createEventInstancesMapFromEvents(events)
    }

    private fun createEventInstancesMapFromEvents(
        events: List<CalendarEvent>,
        includeVisibility: Boolean = true
    ): Map<String, List<CalendarEventInstanceInfo>> {
        val requests = events.map { it.toRRuleRequest() }
        val response = generateEventInstances(requests)

        val userSequencesVisibilityMap =
            if (includeVisibility)
                sequenceService.getUserSequencesVisibilityMap(userService.getAuthenticatedUserId())
            else emptyMap()

        // Process the response and map to CalendarEventInstanceInfo
        val eventInstances = events.zip(response) { event, dates ->
            dates.mapIndexed { index, date ->
                CalendarEventInstanceInfo(
                    eventId = event.id!!,
                    date = date,
                    duration = event.duration,
                    event = event.toDto(userSequencesVisibilityMap[event.sequenceId]),
                    order = index
                )
            }
        }

        // Group by date string
        return eventInstances.flatten()
            .groupBy { it.date.toLocalDate().toString() }
    }

    // this is used only after event creation, to get the instances of the created event
    fun generateInstancesForEvent(eventId: Long): Map<String, List<CalendarEventInstanceInfo>> {
        val event = repository.findById(eventId).orElseThrow()
        val isPublic = sequenceService.isSequenceForAuthenticatedUserPublic(event.sequenceId)
        val request = event.toRRuleRequest()
        val instances = restClient.post()
            .uri("/generate-event-instances-for-event")
            .contentType(APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(object : ParameterizedTypeReference<List<ZonedDateTime>>() {})!!
        val result = instances.withIndex().associate { instance ->
            instance.value.toLocalDate().toString() to listOf(
                CalendarEventInstanceInfo(
                    eventId = eventId,
                    date = instance.value,
                    duration = event.duration,
                    event = event.toDto(isPublic),
                    order = instance.index
                )
            )
        }
        return result
    }

    @Transactional
    fun save(request: CalendarEventCreationRequest): Long {
        val sequenceId = sequenceService.generateSequenceId()
        sequenceService.saveSequenceForUserAndOwnerOnAuthenticatedUser(sequenceId)
        return save(request.toEntity(sequenceId))
    }

    private fun save(calendarEvent: CalendarEvent): Long {
        val (rruleText, rruleString) = if (calendarEvent.isRepeating) {
            restClient.post()
                .uri("/get-rrule-text-and-string")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(calendarEvent.toRRuleRequest())
                .retrieve()
                .body(RRuleTextAndString::class.java)!!
        } else {
            RRuleTextAndString(null, null)
        }
        val repeatingPattern = calendarEvent.repeatingPattern?.let {
            it.copy(rruleText = rruleText, rruleString = rruleString).withBase(it)
        }
        return repository.save(calendarEvent.copy(repeatingPattern = repeatingPattern).withBase(calendarEvent)).id!!
    }

    fun saveAll(events: List<CalendarEvent>) {
        val (repeatingEvents, nonRepeatingEvents) = events.partition { it.isRepeating }
        val (rruleTexts, rruleStrings) = restClient.post()
            .uri("/get-rrule-text-and-string-for-all-events")
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(repeatingEvents.map { it.toRRuleRequest() })
            .retrieve()
            .body(RRuleTextsAndStrings::class.java)!!
        val zippedTextsAndStrings = rruleTexts.zip(rruleStrings)
        val updatedRepeatingEvents = repeatingEvents.zip(zippedTextsAndStrings) { event, (rruleText, rruleString) ->
            val repeatingPattern = event.repeatingPattern!!.copy(rruleText = rruleText, rruleString = rruleString)
                .withBase(event.repeatingPattern)
            event.copy(repeatingPattern = repeatingPattern).withBase(event)
        }
        repository.saveAll(updatedRepeatingEvents + nonRepeatingEvents)
    }

    fun saveRpWithNewRruleText(rp: RepeatingPattern, startDate: ZonedDateTime) {
        val (rruleText, rruleString) = restClient.post()
                .uri("/get-rrule-text-and-string")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(rp.toRRuleRequest(startDate))
                .retrieve()
                .body(RRuleTextAndString::class.java)!!
        repeatingPatternService.save(
            rp.copy(
                rruleText = rruleText,
                rruleString = rruleString
            ).withBase(rp)
        )
    }

    @Transactional
    fun delete(id: Long, fromDate: ZonedDateTime, actionType: ActionType, order: Int) {
        val event = repository.findById(id).orElseThrow()
        when (actionType) {
            ActionType.THIS_EVENT -> deleteThisInstance(event, fromDate, order)
            ActionType.THIS_AND_ALL_FOLLOWING_EVENTS -> deleteThisAndAllFollowingInstances(event, fromDate)
            ActionType.ALL_EVENTS -> deleteAllInstances(event)
        }
    }

    @Transactional
    fun update(updateRequest: CalendarEventUpdateRequest) {
        val event = repository.findById(updateRequest.eventId).orElseThrow()
        when (updateRequest.actionType) {
            ActionType.THIS_EVENT -> updateSingleInstance(
                event = event,
                newFromDate = updateRequest.newStartDate,
                oldFromDate = updateRequest.fromDate,
                newDuration = updateRequest.newDuration,
                order = updateRequest.order
            )
            ActionType.THIS_AND_ALL_FOLLOWING_EVENTS -> updateThisAndAllFollowingInstances(
                event = event,
                newFromDate = updateRequest.newStartDate,
                oldFromDate = updateRequest.fromDate,
                newDuration = updateRequest.newDuration,
                order = updateRequest.order
            )
            ActionType.ALL_EVENTS -> updateAllInstances(
                event = event,
                newFromDate = updateRequest.newStartDate,
                newDuration = updateRequest.newDuration
            )
        }
    }

    private fun deleteThisInstance(event: CalendarEvent, fromDate: ZonedDateTime, order: Int) {
        if (event.isNonRepeating) {
            repository.delete(event)
            return
        }
        val (previousOccurrence, nextOccurrence) = getPreviousAndNextOccurrence(event, fromDate)
        nextOccurrence?.let { it ->
            // must set occurrenceCount, in case until is null
            val occurrenceCount = event.repeatingPattern!!.occurrenceCount?.let { it - order - 1 }
            val repeatingPatternForNewEvent = event.repeatingPattern.copy(occurrenceCount = occurrenceCount)
            val newEvent = event.copy(startDate = it, repeatingPattern = repeatingPatternForNewEvent)
            save(newEvent) // save new event
        }
        if (previousOccurrence == null) {
            repository.delete(event)
        } else {
            val updatedRepeatingPattern =
                event.repeatingPattern!!.copy(until = previousOccurrence.plusMinutes(event.duration.toLong())).withBase(event.repeatingPattern)
            saveRpWithNewRruleText(updatedRepeatingPattern, event.startDate)
        }
    }

    private fun deleteThisAndAllFollowingInstances(event: CalendarEvent, fromDate: ZonedDateTime) {
        val otherEventsInSequence = repository.findAllBySequenceIdAndStartDateGreaterThan(event.sequenceId, fromDate)
        repository.deleteAll(otherEventsInSequence)
        if (event.isRepeating) {
            val newRepeatingPattern = event.repeatingPattern!!.copy(until = fromDate.atStartOfDay()).withBase(event.repeatingPattern)
            save(
                event.copy(repeatingPattern = newRepeatingPattern).withBase(event)
            )
        } else {
            repository.delete(event)
        }
    }

    private fun deleteAllInstances(event: CalendarEvent) = repository.deleteAllBySequenceId(event.sequenceId)

    private fun updateSingleInstance(event: CalendarEvent,
                                     oldFromDate: ZonedDateTime,
                                     newFromDate: ZonedDateTime,
                                     newDuration: Int,
                                     order: Int) {
        if (event.isNonRepeating) {
            save(event.copy(startDate = newFromDate, duration = newDuration).withBase(event))
            return
        }
        // create new non-repeating event on the date the update is applied
        val newNonRepeatingEvent = event.copy(
            startDate = newFromDate,
            duration = newDuration,
            repeatingPattern = null
        )
        save(newNonRepeatingEvent)
        // create new repeating event from the next occurrence onwards
        val (previousOccurrence, nextOccurrence) = getPreviousAndNextOccurrence(event, oldFromDate)
        nextOccurrence?.let {
            val newEvent = event.copy(
                startDate = nextOccurrence,
                repeatingPattern = event.repeatingPattern!!.copy(
                    occurrenceCount = event.repeatingPattern.occurrenceCount?.minus(order)
                )
            )
            save(newEvent)
        }
        // update the repeating pattern for the original event or delete the original event if the first instance is modified
        if (previousOccurrence == null) {
            repository.delete(event)
        } else {
            val updatedRepeatingPattern = event.repeatingPattern!!.copy(
                until = previousOccurrence.plusMinutes(event.duration.toLong())
            ).withBase(event.repeatingPattern)
            saveRpWithNewRruleText(updatedRepeatingPattern, event.startDate)
        }
    }

    private fun updateThisAndAllFollowingInstances(
        event: CalendarEvent, oldFromDate: ZonedDateTime,
        newFromDate: ZonedDateTime, newDuration: Int, order: Int
    ) {
        //update all other following events in sequence
        val events = repository.findAllBySequenceIdAndStartDateGreaterThan(event.sequenceId, oldFromDate)
        val updatedEvents = events.map {
            it.copy(
                startDate = it.startDate.withTimeFrom(newFromDate),
                duration = newDuration
            ).withBase(it)
        }
        saveAll(updatedEvents)

        if (event.isRepeating) {
            // create new event with the new time and duration
            val newEvent = event.copy(startDate = newFromDate, duration = newDuration, repeatingPattern = event.repeatingPattern!!.copy(
                until = event.repeatingPattern.until?.withTimeFrom(newFromDate)?.plusMinutes(newDuration.toLong()),
                occurrenceCount = event.repeatingPattern.occurrenceCount?.minus(order)
            ))
            save(newEvent)

            // modify repeating pattern for old event
            val previousOccurrence = getPreviousOccurrence(event, oldFromDate)
            if (previousOccurrence == null) {
                repository.delete(event)
            } else {
                val updatedRepeatingPattern = event.repeatingPattern.copy(
                    until = previousOccurrence.plusMinutes(event.duration.toLong())
                ).withBase(event.repeatingPattern)
                saveRpWithNewRruleText(updatedRepeatingPattern, event.startDate)
            }
        } else {
            save(event.copy(startDate = newFromDate, duration = newDuration).withBase(event))
        }
    }

    private fun updateAllInstances(event: CalendarEvent, newFromDate: ZonedDateTime, newDuration: Int) {
        val events = repository.findAllBySequenceId(event.sequenceId)
        val updatedEvents = events.map {
            it.copy(
                startDate = it.startDate.withTimeFrom(newFromDate),
                duration = newDuration
            ).withBase(it)
        }
        saveAll(updatedEvents)
    }

    fun getPreviousAndNextOccurrencesPublic(event: CalendarEvent, referenceDate: ZonedDateTime): Pair<ZonedDateTime?, ZonedDateTime?> {
        val (previousOccurrence, nextOccurrence) = getPreviousAndNextOccurrence(event, referenceDate)
        return previousOccurrence to nextOccurrence
    }

    private fun getPreviousAndNextOccurrence(event: CalendarEvent, fromDate: ZonedDateTime): PreviousAndNextOccurrence =
        restClient.post()
            .uri("/calculate-previous-next-execution")
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(
                mapOf(
                    "rruleRequest" to event.toRRuleRequest(),
                    "date" to fromDate.toString()
                )
            ).retrieve()
            .body(PreviousAndNextOccurrence::class.java)!!

    private fun getPreviousOccurrence(event: CalendarEvent, referenceDate: ZonedDateTime): ZonedDateTime? =
        restClient.post()
            .uri("/calculate-previous-execution")
            .contentType(APPLICATION_JSON)
            .body(
                mapOf(
                    "rruleRequest" to event.toRRuleRequest(),
                    "date" to referenceDate.toString()
                )
            ).retrieve()
            .body(ZonedDateTime::class.java)

    private fun generateEventInstances(rruleRequests: List<RRuleRequest>) =
        restClient.post()
            .uri("/generate-event-instances")
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(rruleRequests)
            .retrieve()
            .body(object : ParameterizedTypeReference<List<List<ZonedDateTime>>>() {})!!

    private data class PreviousAndNextOccurrence(
        val previousOccurrence: ZonedDateTime?,
        val nextOccurrence: ZonedDateTime?
    )

    fun shareEventSequenceWithUser(request: ShareEventRequest) {
        val (userId, sequenceId) = request
        if (!sequenceService.isAuthenticatedUserOwnerOfSequence(sequenceId)) {
            throw NotAuthorizedException("No permission to share specified event.")
        }
        sequenceService.saveSequenceForUser(userId, sequenceId)
    }

    fun createIcsFileForAuthenticatedUser(): String {
        val sequences = sequenceService.findAllSequencesForAuthenticatedUser()
        val events = sequences.flatMap { repository.findAllBySequenceId(it) }
        val fileStr = createIcsFile(events)
        return fileStr
    }

    private fun createIcsFile(events: List<CalendarEvent>): String {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
        val sb = StringBuilder()

        sb.append("BEGIN:VCALENDAR\n")
        sb.append("VERSION:2.0\n")
        sb.append("PRODID:-//MyCalendar//EN\n")

        for (event in events) {
            val start = event.startDate.withZoneSameInstant(ZoneOffset.UTC).format(dateTimeFormatter)
            val end = event.startDate.plusMinutes(event.duration.toLong()).withZoneSameInstant(ZoneOffset.UTC).format(dateTimeFormatter)

            sb.append("BEGIN:VEVENT\n")
            sb.append("UID:${UUID.randomUUID()}@mycalendar.com\n")
            sb.append("DTSTAMP:${event.createdAt!!.withZoneSameInstant(ZoneOffset.UTC).format(dateTimeFormatter)}\n")
            sb.append("DTSTART:$start\n")
            sb.append("DTEND:$end\n")
            sb.append("SUMMARY:${event.title}\n")
            sb.append("DESCRIPTION:${event.description}\n")
            //sb.append("SEQUENCE:${event.sequenceId}\n")

            event.repeatingPattern?.let { pattern ->
                val rrule = extractRruleLine(pattern.rruleString!!)
                sb.append("$rrule\n")
            }

            sb.append("END:VEVENT\n")
        }

        sb.append("END:VCALENDAR\n")

        return sb.toString()
    }

    fun extractRruleLine(rruleString: String): String {
        // Split the string by lines
        val lines = rruleString.split("\n")

        // Find the line that starts with "RRULE:"
        val rruleLine = lines.find { it.startsWith("RRULE:") }

        // Return the rrule line or throw an error if not found
        return rruleLine ?: throw IllegalArgumentException("RRULE not found in the string")
    }

    fun updateEventSequenceVisibility(sequenceId: String, isPublic: Boolean) {
        if (!sequenceService.isAuthenticatedUserOwnerOfSequence(sequenceId)) {
            throw NotAuthorizedException("No permission to change event visibility.")
        }
        sequenceService.updateEventSequenceVisibility(sequenceId, isPublic)
    }

    @Transactional
    fun updateVisibilityForAllAuthenticatedUserSequences(isPublic: Boolean) {
        val sequences = sequenceService.findAllSequencesForAuthenticatedUser()
        sequences.forEach { updateEventSequenceVisibility(it, isPublic) }
        userService.updateCalendarVisibilityForAuthenticatedUser(isPublic)
    }

    @Transactional
    fun addOrUpdateEmailNotificationForEvent(eventId: Long, minutes: Int) {
        val event = repository.findById(eventId).orElseThrow { CalendarEntityNotFoundException("Event does not exist") }
        sequenceService.saveNotificationConfigForEvent(event, minutes)
        if (!event.startDate.isInPastComparingTimeOnly()) {
            eventPublisher.publishEvent(
                EmailNotificationAddedOrUpdated(event = event, minutes = minutes)
            )
        }
    }
}

data class CalendarEventInstanceInfo(
    val eventId: Long,
    val date: ZonedDateTime,
    val duration: Int,
    val event: CalendarEventDto,
    val order: Int
)

data class RRuleTextAndString(
    val rruleText: String?,
    val rruleString: String?
)

data class RRuleTextsAndStrings(
    val rruleTexts: List<String>,
    val rruleStrings: List<String>
)
