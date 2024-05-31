package org.example.mycalendarbackend.domain.dto

data class DateTime(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int? = null,
    val minute: Int? = null,
    val second: Int? = null
)
