package org.example.mycalendarbackend.service

import java.util.*

class SequenceGenerator {

    companion object {
        fun generateId(): String = UUID.randomUUID().toString()
    }

}