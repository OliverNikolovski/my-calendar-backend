package org.example.mycalendarbackend.api.response

data class SelectOption(
    val value: Any,
    val name: Any
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SelectOption

        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}
