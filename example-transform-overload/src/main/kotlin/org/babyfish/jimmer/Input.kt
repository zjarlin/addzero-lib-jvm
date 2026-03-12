package org.babyfish.jimmer

interface Input<E> {
    fun toEntity(): E
}
