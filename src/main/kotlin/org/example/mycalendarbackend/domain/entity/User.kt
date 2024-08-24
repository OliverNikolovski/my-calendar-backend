package org.example.mycalendarbackend.domain.entity

import jakarta.persistence.*
import org.example.mycalendarbackend.domain.base.BaseEntity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity
@Table(schema = "calendar", name = "users")
data class User(
    @Column(name = "username", nullable = false, unique = true)
    val usernameField: String,

    @Column(name = "password", nullable = false)
    val passwordField: String,

    @Column(name = "first_name")
    val name: String?,

    @Column(name = "last_name")
    val lastName: String?,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", schema = "calendar", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "role")
    val roles: Set<String> = setOf(),

    @Column(name = "public_calendar", nullable = false)
    val isCalendarPublic: Boolean = false
): BaseEntity<Long>(), UserDetails {

    override fun getAuthorities(): Set<GrantedAuthority> = roles.map { SimpleGrantedAuthority(it) }.toSet()

    override fun getPassword(): String = passwordField

    override fun getUsername(): String = usernameField

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}
