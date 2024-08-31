package org.example.mycalendarbackend.repository

import org.example.mycalendarbackend.domain.entity.User
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

internal interface UserRepository : JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    fun findByUsernameField(username: String): User?

    fun <E> findById(id: Long, type: Class<E>): E?

    fun findByUsernameFieldContainsIgnoreCaseOrNameContainsIgnoreCaseOrLastNameContainsIgnoreCase(
        username: String, name: String, lastName: String, pageable: Pageable
    ): List<User>

}
