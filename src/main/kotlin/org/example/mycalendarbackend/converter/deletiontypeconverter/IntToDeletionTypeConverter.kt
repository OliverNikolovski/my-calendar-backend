package org.example.mycalendarbackend.converter.deletiontypeconverter

import org.example.mycalendarbackend.domain.enums.ActionType
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class StringToDeletionTypeConverter : Converter<String, ActionType> {
    override fun convert(source: String): ActionType {
        return when (source) {
            "0" -> ActionType.THIS_EVENT
            "1" -> ActionType.THIS_AND_ALL_FOLLOWING_EVENTS
            "2" -> ActionType.ALL_EVENTS
            else -> throw IllegalArgumentException("Invalid DeletionType value: $source")
        }
    }
}