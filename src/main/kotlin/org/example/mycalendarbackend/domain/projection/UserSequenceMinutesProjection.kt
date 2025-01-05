package org.example.mycalendarbackend.domain.projection

interface UserSequenceMinutesProjection {

    val sequenceId: String

    val notifyMinutesBefore: Int?

}