package site.addzero.ksp.metadata.jimmer.entity.spi

fun interface JimmerEntityConsumer {
    fun consume(entities: Set<JimmerEntityMeta>)
}
