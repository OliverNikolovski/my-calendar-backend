package org.example.mycalendarbackend.domain.projection

interface UserSequenceVisibilityProjection {

    val sequenceId: String

    val isPublic: Boolean

}
