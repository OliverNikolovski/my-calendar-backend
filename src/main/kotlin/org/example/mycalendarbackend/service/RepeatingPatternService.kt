package org.example.mycalendarbackend.service

import org.example.mycalendarbackend.domain.dto.RepeatingPatternDto
import org.example.mycalendarbackend.extension.toEntity
import org.example.mycalendarbackend.repository.RepeatingPatternRepository
import org.springframework.stereotype.Service

@Service
internal class RepeatingPatternService(
    private val repository: RepeatingPatternRepository
) {

    fun save(repeatingPatternDto: RepeatingPatternDto) = repository.save(repeatingPatternDto.toEntity()).id

    fun delete(id: Long) = repository.deleteById(id)

}