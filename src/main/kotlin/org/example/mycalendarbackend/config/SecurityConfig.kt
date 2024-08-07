package org.example.mycalendarbackend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.session.HttpSessionEventPublisher


@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http.authorizeHttpRequests {
            it.requestMatchers("/public").permitAll()
            it.anyRequest().authenticated()
        }.sessionManagement { sessionManagementConfigurer ->
            sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            sessionManagementConfigurer.sessionFixation {
                it.migrateSession()
            }
            sessionManagementConfigurer.sessionConcurrency {
                it.maximumSessions(1)
            }
        }.build()
    }

    @Bean
    fun httpSessionEventPublisher(): HttpSessionEventPublisher {
        return HttpSessionEventPublisher()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(10)
    }

}