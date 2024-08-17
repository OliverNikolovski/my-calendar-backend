package org.example.mycalendarbackend.config

import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.mycalendarbackend.service.JwtService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter


@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userDetailsService: UserDetailsService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (request.servletPath.contains("/api/auth")) {
            filterChain.doFilter(request, response)
            return
        }
        val authHeader = request.getHeader("Authorization")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }
        val jwt = authHeader.substring(7)
        val userEmail: String
        try {
            userEmail = jwtService.extractUsername(jwt)
        } catch (ex: ExpiredJwtException) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.write("Token expired")
            response.writer.flush()
            return
        }
        if (SecurityContextHolder.getContext().authentication == null) {
            val userDetails: UserDetails = this.userDetailsService.loadUserByUsername(userEmail)
            jwtService.checkTokenValidity(jwt, userDetails) // ???
            val authToken = UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.authorities
            )
            authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authToken
        }
        filterChain.doFilter(request, response)
    }

//    override fun doFilterInternal(
//        request: HttpServletRequest,
//        response: HttpServletResponse,
//        filterChain: FilterChain
//    ) {
//        if (request.servletPath.contains("/api/v1/auth")) {
//            filterChain.doFilter(request, response)
//            return
//        }
//        val authHeader = request.getHeader("Authorization")
//        val userEmail: String
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            filterChain.doFilter(request, response)
//            return
//        }
//        val jwt = authHeader.substring(7)
//        userEmail = jwtService.extractUsername(jwt)
//        if (SecurityContextHolder.getContext().authentication == null) {
//            val userDetails: UserDetails = this.userDetailsService.loadUserByUsername(userEmail)
//            if (jwtService.isTokenValid(jwt, userDetails)) {
//                val authToken = UsernamePasswordAuthenticationToken(
//                    userDetails,
//                    null,
//                    userDetails.authorities
//                )
//                authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
//                SecurityContextHolder.getContext().authentication = authToken
//            }
//        }
//        filterChain.doFilter(request, response)
//    }
}