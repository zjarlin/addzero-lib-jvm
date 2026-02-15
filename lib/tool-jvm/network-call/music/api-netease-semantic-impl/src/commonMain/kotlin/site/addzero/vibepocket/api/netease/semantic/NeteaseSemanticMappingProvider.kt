package site.addzero.vibepocket.api.netease.semantic

import site.addzero.ksp.metadata.semantic.SemanticHelper
import site.addzero.ksp.metadata.semantic.SemanticMappingProvider
import site.addzero.ksp.metadata.semantic.SemanticMethodDefinition
import site.addzero.vibepocket.api.netease.model.MusicSearchType

class NeteaseSemanticMappingProvider : SemanticMappingProvider {
    
    override fun getSupportedClassNames(): List<String> {
        return listOf("site.addzero.vibepocket.api.netease.NeteaseApi")
    }

    override fun getMappings(qualifiedName: String): Map<String, List<SemanticMethodDefinition>>? {
        if (qualifiedName != "site.addzero.vibepocket.api.netease.NeteaseApi") return null

        return mapOf(
            "search" to SemanticHelper.expandEnum(
                originMethod = "search",
                paramName = "type",
                enumValues = MusicSearchType.entries.toTypedArray(),
                nameTemplate = "{method}{Name}",
                docTemplate = "语义化特化：搜索{Name}",
                valueProvider = { it.value }
            )
        )
    }
}
