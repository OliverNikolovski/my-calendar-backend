package org.example.mycalendarbackend.api.controller

import io.jsonwebtoken.ExpiredJwtException
import org.example.mycalendarbackend.exception.JwtTokenInvalidException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionHandler {

    @ExceptionHandler
    fun handleJwtTokenInvalidException(exception: JwtTokenInvalidException): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.message)

}