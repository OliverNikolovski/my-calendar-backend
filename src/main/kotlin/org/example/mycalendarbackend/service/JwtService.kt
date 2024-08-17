package org.example.mycalendarbackend.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.example.mycalendarbackend.config.JwtConfigProps
import org.example.mycalendarbackend.exception.JwtTokenExpiredException
import org.example.mycalendarbackend.exception.JwtTokenInvalidSubjectException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import javax.crypto.SecretKey


@Service
class JwtService(
    val jwtConfigProps: JwtConfigProps
) {

    fun extractUsername(token: String): String = extractClaim(token, Claims::getSubject)

    fun extractExpiration(token: String) = ZonedDateTime.ofInstant(
        extractClaim(token, Claims::getExpiration).toInstant(),
        ZoneId.systemDefault()
    )

    private fun <T> extractClaim(token: String, resolver: (Claims) -> T): T = resolver(extractAllClaims(token))

    private fun extractAllClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(getSecretKey())
            .build()
            .parseSignedClaims(token)
            .payload

    private fun getSecretKey(): SecretKey = Keys.hmacShaKeyFor(
        Decoders.BASE64.decode(jwtConfigProps.secretKey)
    )

    private fun isTokenExpired(token: String): Boolean = extractExpiration(token).isBefore(ZonedDateTime.now())

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return (username == userDetails.username) && !isTokenExpired(token)
    }

    fun checkTokenValidity(token: String, userDetails: UserDetails) {
        val username = extractUsername(token)
        if (username != userDetails.username) {
            throw JwtTokenInvalidSubjectException("Invalid subject")
        }
        if (isTokenExpired(token)) {
            throw JwtTokenExpiredException("Token expired")
        }
    }

    fun generateToken(userDetails: UserDetails?): String {
        return generateToken(HashMap(), userDetails)
    }

    fun generateToken(
        extraClaims: Map<String, Any>,
        userDetails: UserDetails?
    ): String {
        return buildToken(extraClaims, userDetails!!, jwtConfigProps.expiration)
    }

    fun generateRefreshToken(
        userDetails: UserDetails?
    ): String {
        return buildToken(mapOf(), userDetails!!, jwtConfigProps.refreshToken.expiration)
    }

    private fun buildToken(
        extraClaims: Map<String, Any?>,
        userDetails: UserDetails,
        expiration: Long
    ): String {
        return Jwts
            .builder()
            .claims(extraClaims)
            .subject(userDetails.username)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + expiration))
            .signWith(getSecretKey(), Jwts.SIG.HS256)
            .compact()
    }

}