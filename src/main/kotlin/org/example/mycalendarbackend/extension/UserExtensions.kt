package org.example.mycalendarbackend.extension

import org.example.mycalendarbackend.api.response.SelectOption
import org.example.mycalendarbackend.domain.entity.User

fun User.toSelectOption(): SelectOption = SelectOption(
    value = id!!,
    name = username
)

fun List<User>.toSelectOptionList(): List<SelectOption> = map { it.toSelectOption() }
