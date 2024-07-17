package org.example.mycalendarbackend.extension

import org.example.mycalendarbackend.domain.base.BaseEntity

fun <ID, T : BaseEntity<ID>> T.withBase(o: T): T {
    this.id = o.id
    return this
}