package org.example.mycalendarbackend.domain.projection

import org.springframework.beans.factory.annotation.Value

interface UserSequenceVisibilityProjection {

    val sequenceId: String

    @get:Value("#{target.isPublic}")
    val isPublic: Boolean

}
