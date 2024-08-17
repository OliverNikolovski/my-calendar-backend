package org.example.mycalendarbackend.domain.base

import jakarta.persistence.*
import java.io.Serializable
import java.time.ZonedDateTime

@MappedSuperclass
abstract class BaseEntity<T : Serializable> : Serializable {

    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    var id: T? = null

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: ZonedDateTime? = null
        private set

    @Column(name = "updated_at", nullable = false)
    var updatedAt: ZonedDateTime? = null
        private set

    @PrePersist
    private fun onCreate() {
        val now = ZonedDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    private fun onUpdate() {
        updatedAt = ZonedDateTime.now()
    }

    private fun setCreatedAt(date: ZonedDateTime) {
        this.createdAt = date
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseEntity<*>

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}