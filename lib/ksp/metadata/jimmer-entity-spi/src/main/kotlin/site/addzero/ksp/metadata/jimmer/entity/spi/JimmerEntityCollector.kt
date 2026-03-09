package site.addzero.ksp.metadata.jimmer.entity.spi

import androidx.room.compiler.processing.ExperimentalProcessingApi
import androidx.room.compiler.processing.XProcessingEnv
import androidx.room.compiler.processing.XTypeElement
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate

const val JIMMER_ENTITY_ANNOTATION = "org.babyfish.jimmer.sql.Entity"

data class JimmerEntityCollectionResult(
    val deferred: List<KSAnnotated>,
    val entitiesByQualifiedName: Map<String, XTypeElement>
) {
    val entities: Set<XTypeElement>
        get() = entitiesByQualifiedName.values.toSet()
}

object JimmerEntityCollector {
}
