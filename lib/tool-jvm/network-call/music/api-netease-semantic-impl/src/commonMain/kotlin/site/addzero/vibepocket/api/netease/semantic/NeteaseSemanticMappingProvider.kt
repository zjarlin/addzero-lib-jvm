package site.addzero.vibepocket.api.netease.semantic

import site.addzero.ksp.metadata.semantic.SemanticMappingProvider
import site.addzero.ksp.metadata.semantic.SemanticMethodDefinition

class NeteaseSemanticMappingProvider : SemanticMappingProvider {

    override fun getSupportedClassNames(): List<String> {
        return listOf("site.addzero.vibepocket.api.netease.NeteaseApi")
    }

    override fun getMappings(qualifiedName: String): Map<String, List<SemanticMethodDefinition>>? {
        if (qualifiedName != "site.addzero.vibepocket.api.netease.NeteaseApi") return null

        val enumMapping = mapOf(
            "Songs" to 1,
            "Albums" to 10,
            "Artists" to 100,
            "Playlists" to 1000,
            "Lyrics" to 1006
        )

        val searchExtensions = enumMapping.map { (name, value) ->
            SemanticMethodDefinition(
                newMethodName = "search$name",
                fixedParameters = mapOf("type" to value),
                doc = "语义化特化：搜索$name"
            )
        }

      val mapOf = mapOf(
        "search" to searchExtensions
      )
      return mapOf
    }
}
