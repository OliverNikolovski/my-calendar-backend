package org.example.mycalendarbackend.exception

class NotAuthorizedException(override val message: String) : RuntimeException(message)