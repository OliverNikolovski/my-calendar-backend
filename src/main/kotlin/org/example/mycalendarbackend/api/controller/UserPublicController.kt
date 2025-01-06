package org.example.mycalendarbackend.api.controller

import org.example.mycalendarbackend.api.response.SelectOption
import org.example.mycalendarbackend.service.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/users")
class UsePublicController internal constructor(
    private val userService: UserService
){

    @GetMapping("/username-exists")
    fun usernameExists(@RequestParam username: String): Boolean = userService.checkIfUsernameExists(username)

}