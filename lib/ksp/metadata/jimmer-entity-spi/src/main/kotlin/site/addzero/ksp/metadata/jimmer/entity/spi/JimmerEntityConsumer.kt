package site.addzero.ksp.metadata.jimmer.entity.spi

import androidx.room.compiler.processing.XTypeElement

fun interface JimmerEntityConsumer {
    fun consume(entities: Set<XTypeElement>)
}
