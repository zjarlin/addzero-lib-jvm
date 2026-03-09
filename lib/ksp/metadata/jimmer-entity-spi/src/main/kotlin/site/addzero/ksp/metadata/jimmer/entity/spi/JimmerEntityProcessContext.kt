package site.addzero.ksp.metadata.jimmer.entity.spi

import androidx.room.compiler.processing.XTypeElement
import com.google.devtools.ksp.processing.KSPLogger

data class JimmerEntityProcessContext(
    val logger: KSPLogger,
    val options: Map<String, String>,
    val entities: Set<XTypeElement>
)
