package org.example.mycalendarbackend.api.request

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val username: String,
    val password: String
)
