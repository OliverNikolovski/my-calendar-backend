package org.example.mycalendarbackend.converter.deletiontypeconverter

import org.example.mycalendarbackend.domain.enums.DeletionType
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class StringToDeletionTypeConverter : Converter<String, DeletionType> {
    override fun convert(source: String): DeletionType {
        return when (source) {
            "0" -> DeletionType.THIS_EVENT
            "1" -> DeletionType.THIS_AND_ALL_FOLLOWING_EVENTS
            "2" -> DeletionType.ALL_EVENTS
            else -> throw IllegalArgumentException("Invalid DeletionType value: $source")
        }
    }
}