package org.example.mycalendarbackend.service

import org.example.mycalendarbackend.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
internal class UserService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails =
        userRepository.findByUsernameField(username) ?: throw UsernameNotFoundException("Username $username does not exist.")

}