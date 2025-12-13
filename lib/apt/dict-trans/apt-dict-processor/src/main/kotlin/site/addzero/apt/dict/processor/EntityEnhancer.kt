package site.addzero.apt.dict.processor

import site.addzero.apt.dict.metadata.DictFieldMetadata
import site.addzero.apt.dict.template.JTETemplateManager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement

/**
 * Entity enhancer for generating enhanced entity classes
 */
class EntityEnhancer(
    private val processingEnv: ProcessingEnvironment,
    private val templateManager: JTETemplateManager
) {
    
    /**
     * Enhances an entity with dictionary translation capabilities
     */
    fun enhanceEntity(
        entityElement: TypeElement,
        dictFields: List<DictFieldMetadata>,
        config: EnhancedEntityConfig
    ): String {
        val className = "${entityElement.simpleName}Enhanced"
        val packageName = processingEnv.elementUtils.getPackageOf(entityElement).qualifiedName.toString()
        
        val fieldMaps = dictFields.map { field ->
            mapOf(
                "name" to field.targetField,
                "type" to "String", // Translation fields are always String
                "dictCode" to (field.dictCode ?: ""),
                "translationType" to field.translationType.name
            )
        }
        
        return templateManager.renderEnhancedEntity(
            className = className,
            packageName = packageName,
            fields = fieldMaps
        )
    }
}

/**
 * Configuration for enhanced entity generation
 */
data class EnhancedEntityConfig(
    val generateGetters: Boolean = true,
    val generateSetters: Boolean = true,
    val generateToString: Boolean = true,
    val generateEquals: Boolean = true,
    val generateHashCode: Boolean = true
)