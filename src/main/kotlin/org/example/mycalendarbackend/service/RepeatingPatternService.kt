package org.example.mycalendarbackend.service

import org.example.mycalendarbackend.domain.entity.RepeatingPattern
import org.example.mycalendarbackend.repository.RepeatingPatternRepository
import org.springframework.stereotype.Service

@Service
internal class RepeatingPatternService(
    private val repository: RepeatingPatternRepository
) {

    fun delete(id: Long) = repository.deleteById(id)

    fun delete(repeatingPattern: RepeatingPattern) = repository.delete(repeatingPattern)

    fun save(repeatingPattern: RepeatingPattern) = repository.save(repeatingPattern)

}