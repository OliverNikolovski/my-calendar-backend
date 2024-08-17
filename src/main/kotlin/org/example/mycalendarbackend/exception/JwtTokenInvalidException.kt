package org.example.mycalendarbackend.exception


// these exceptions are not needed
sealed class JwtTokenInvalidException(override val message: String) : RuntimeException(message)

data class JwtTokenInvalidSubjectException(override val message: String) : JwtTokenInvalidException(message)

data class JwtTokenExpiredException(override val message: String) : JwtTokenInvalidException(message)

data class JwtRefreshTokenExpiredException(override val message: String) : JwtTokenInvalidException(message)
