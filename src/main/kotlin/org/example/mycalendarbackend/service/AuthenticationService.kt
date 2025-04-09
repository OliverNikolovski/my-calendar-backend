package org.example.mycalendarbackend.service

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.mycalendarbackend.api.request.AuthenticationRequest
import org.example.mycalendarbackend.api.request.RegisterRequest
import org.example.mycalendarbackend.api.response.AuthenticationResponse
import org.example.mycalendarbackend.domain.entity.User
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service


@Service
internal class AuthenticationService(
    private val authenticationManager: AuthenticationManager,
    private val userService: UserService,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder
) {

    fun register(request: RegisterRequest): AuthenticationResponse {
        val user = User(
            name = request.firstName,
            lastName = request.lastName,
            usernameField = request.username,
            passwordField = passwordEncoder.encode(request.password)
        )
        val savedUser = userService.save(user)
        val jwtToken = jwtService.generateToken(user)
        val refreshToken = jwtService.generateRefreshToken(user)
        return AuthenticationResponse(
            accessToken = jwtToken,
            refreshToken = refreshToken,
            username = savedUser.username,
        )
    }

    fun authenticate(request: AuthenticationRequest): AuthenticationResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                request.email,
                request.password
            )
        )
        val user = userService.loadUserByUsername(request.email)
        val jwtToken = jwtService.generateToken(user)
        val refreshToken = jwtService.generateRefreshToken(user)
        return AuthenticationResponse(
            accessToken = jwtToken,
            refreshToken = refreshToken,
            username = user.username
        )
    }

    fun refreshToken(
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
        val userEmail: String
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw IllegalStateException("Missing token")
        }
        val refreshToken = authHeader.substring(7)
        userEmail = jwtService.extractUsername(refreshToken)
        val user = userService.loadUserByUsername(userEmail)
        jwtService.checkTokenValidity(refreshToken, user)
        val accessToken = jwtService.generateToken(user)
        val authResponse = AuthenticationResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            username = user.username
        )
        ObjectMapper().writeValue(response.outputStream, authResponse)
    }

}