package site.addzero.ksp.metadata.jimmer.entity.spi

import com.google.devtools.ksp.processing.KSPLogger

data class JimmerEntityProcessContext(
    val logger: KSPLogger,
    val options: Map<String, String>,
    val entitiesByQualifiedName: Map<String, JimmerEntityMeta>
) {
    val entities: Set<JimmerEntityMeta>
        get() = LinkedHashSet(entitiesByQualifiedName.values)
}
