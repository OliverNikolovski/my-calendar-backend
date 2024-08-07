package org.example.mycalendarbackend.repository

import org.example.mycalendarbackend.domain.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

internal interface UserRepository : JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    fun findByUsernameField(username: String): User?

}
