package org.example.mycalendarbackend.service

import org.example.mycalendarbackend.domain.entity.User
import org.example.mycalendarbackend.extension.toSelectOptionList
import org.example.mycalendarbackend.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.context.SecurityContextHolder
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

    fun save(user: User) = userRepository.save(user)
//
//    fun getAuthenticatedUserId(): Long {
//        val authentication = SecurityContextHolder.getContext().authentication
//        val userDetails = authentication.principal as UserDetails
//        val user = loadUserByUsername(userDetails.username) as User
//        return user.id!!
//    }

    fun getAuthenticatedUserId(): Long =
       (SecurityContextHolder.getContext().authentication.principal as User).id!!

    fun findFirstNMatches(n: Int, searchTerm: String) = userRepository.findByUsernameFieldContainsIgnoreCaseOrNameContainsIgnoreCaseOrLastNameContainsIgnoreCase(
        username = searchTerm,
        name = searchTerm,
        lastName = searchTerm,
        pageable = PageRequest.ofSize(n)
    ).toSelectOptionList()

}