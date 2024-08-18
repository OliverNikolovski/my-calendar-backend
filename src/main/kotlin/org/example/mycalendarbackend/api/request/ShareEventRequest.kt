package org.example.mycalendarbackend.api.request

data class ShareEventRequest(
    val userId: Long,
    val sequenceId: String
)
