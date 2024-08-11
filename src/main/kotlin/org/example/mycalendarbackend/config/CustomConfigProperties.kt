package org.example.mycalendarbackend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "application.security.jwt")
data class JwtConfigProps(
    val secretKey: String,
    val expiration: Long,
    val refreshToken: JwtRefreshToken
)

data class JwtRefreshToken(
    val expiration: Long
)
