package org.example.mycalendarbackend.api.request

data class AuthenticationRequest(
    val email: String,
    val password: String
)
