package org.example.mycalendarbackend.api.controller

import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/csrf")
class CsrfController {

    @GetMapping("/token")
    fun csrf(token: CsrfToken): CsrfToken = token

    @GetMapping("/hello")
    fun getHello() {
        println("GET /hello")
    }

    @PostMapping("/hello")
    fun postHello() {
        println("POST /hello")
    }

}