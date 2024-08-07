package org.example.mycalendarbackend.repository

import org.example.mycalendarbackend.domain.entity.RepeatingPattern
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

internal interface RepeatingPatternRepository :
    JpaRepository<RepeatingPattern, Long>,
    JpaSpecificationExecutor<RepeatingPattern>
