package org.example.mycalendarbackend.api.controller

import org.example.mycalendarbackend.api.response.SelectOption
import org.example.mycalendarbackend.service.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController internal constructor(
    private val userService: UserService
){

    @GetMapping("/search")
    fun findFirstNMatches(@RequestParam n: Int, @RequestParam q: String): List<SelectOption> =
        userService.findFirstNMatches(
            searchTerm = q,
            n = n
        )

    @GetMapping("/is-calendar-public")
    fun isCalendarPublic(): Boolean = userService.isAuthenticatedUserCalendarPublic()

}