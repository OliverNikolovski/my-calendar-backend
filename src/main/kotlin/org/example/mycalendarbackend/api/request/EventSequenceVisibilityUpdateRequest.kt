package org.example.mycalendarbackend.api.request

data class EventSequenceVisibilityUpdateRequest(
    val sequenceId: String,
    val isPublic: Boolean
)
