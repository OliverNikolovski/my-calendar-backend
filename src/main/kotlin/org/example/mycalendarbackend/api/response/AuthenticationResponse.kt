package org.example.mycalendarbackend.api.response

data class AuthenticationResponse(
    val accessToken: String,
    val refreshToken: String
)