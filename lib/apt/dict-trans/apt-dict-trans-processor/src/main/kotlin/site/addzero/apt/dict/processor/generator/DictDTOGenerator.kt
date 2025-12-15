package site.addzero.apt.dict.processor.generator

import site.addzero.apt.dict.processor.model.EntityInfo
import site.addzero.apt.dict.processor.model.FieldInfo
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic

/**
 * 字典DTO生成器
 */
class DictDTOGenerator(private val processingEnv: ProcessingEnvironment) {
    
    /**
     * 生成字典DTO类
     */
    fun generateDictDTO(entityInfo: EntityInfo): String {
        val className = "${entityInfo.simpleName}DictDTO"
        val packageName = entityInfo.packageName
        
        return buildString {
            appendLine("package $packageName;")
            appendLine()
            
            // 导入必要的类
            val imports = mutableSetOf<String>()
            entityInfo.allFields.forEach { field ->
                when {
                    field.isCollection -> {
                        imports.add("java.util.List")
                    }
                }
            }

            // 添加嵌套实体的导入
            entityInfo.nestedEntityFields.forEach { nestedField ->
                if (nestedField.isCollection) {
                    imports.add("java.util.List")
                }
                // 如果嵌套实体在不同包，需要导入
                nestedField.nestedEntityInfo?.let { nestedInfo ->
                    if (nestedInfo.packageName != entityInfo.packageName) {
                        imports.add("${nestedInfo.packageName}.${nestedInfo.simpleName}DictDTO")
                    }
                }
            }
            
            imports.sorted().forEach { import ->
                appendLine("import $import;")
            }
            if (imports.isNotEmpty()) {
                appendLine()
            }
            
            appendLine("/**")
            appendLine(" * ${entityInfo.simpleName} 的字典DTO类")
            appendLine(" * 自动生成，请勿手动修改")
            appendLine(" */")
            appendLine("public class $className {")
            appendLine()
            
            // 生成所有原始字段（排除嵌套实体字段，因为它们需要特殊处理）
            entityInfo.allFields.forEach { field ->
                val isNestedEntity = entityInfo.nestedEntityFields.any { it.fieldName == field.name }
                if (!isNestedEntity) {
                    generateField(field)
                 } else {
                     // 生成嵌套实体字段，类型改为对应的DictDTO
                     val nestedField = entityInfo.nestedEntityFields.first { it.fieldName == field.name }
                     val nestedInfo = nestedField.nestedEntityInfo
                     val dtoTypeName = if (nestedInfo != null && nestedInfo.packageName != entityInfo.packageName) {
                         "${nestedInfo.packageName}.${nestedInfo.simpleName}DictDTO"
                     } else {
                         "${nestedField.elementType ?: nestedField.fieldType}DictDTO"
                     }
                     if (nestedField.isCollection) {
                         appendLine("    /**")
                         appendLine("     * ${field.name} - 嵌套实体集合")
                         appendLine("     */")
                         appendLine("    private List<$dtoTypeName> ${field.name};")
                         appendLine()
                     } else {
                         appendLine("    /**")
                         appendLine("     * ${field.name} - 嵌套实体")
                         appendLine("     */")
                         appendLine("    private $dtoTypeName ${field.name};")
                         appendLine()
                     }
                 }
            }
            
            // 生成字典字段
            entityInfo.dictFields.forEach { dictField ->
                dictField.dictConfigs.forEach { config ->
                    val dictFieldName = if (!config.serializationAlias.isNullOrBlank()) {
                        config.serializationAlias!!
                    } else if (config.isSystemDict) {
                        "${dictField.fieldName}_dictText"
                    } else {
                        config.getGeneratedFieldName()
                    }
                    appendLine("    /**")
                    appendLine("     * ${dictField.fieldName} 的字典翻译字段")
                    appendLine("     */")
                    appendLine("    private String $dictFieldName;")
                    appendLine()
                }
            }
            
            // 生成getter和setter方法
            entityInfo.allFields.forEach { field ->
                val isNestedEntity = entityInfo.nestedEntityFields.any { it.fieldName == field.name }
                if (!isNestedEntity) {
                    generateGetterSetter(field)
                 } else {
                     // 生成嵌套实体字段的getter和setter
                     val nestedField = entityInfo.nestedEntityFields.first { it.fieldName == field.name }
                     val nestedInfo = nestedField.nestedEntityInfo
                     val dtoTypeName = if (nestedInfo != null && nestedInfo.packageName != entityInfo.packageName) {
                         "${nestedInfo.packageName}.${nestedInfo.simpleName}DictDTO"
                     } else {
                         "${nestedField.elementType ?: nestedField.fieldType}DictDTO"
                     }
                     if (nestedField.isCollection) {
                         generateGetterSetter(FieldInfo(field.name, "List<$dtoTypeName>"))
                     } else {
                         generateGetterSetter(FieldInfo(field.name, dtoTypeName))
                     }
                 }
            }
            
            // 生成字典字段的getter和setter
            entityInfo.dictFields.forEach { dictField ->
                dictField.dictConfigs.forEach { config ->
                    val dictFieldName = if (!config.serializationAlias.isNullOrBlank()) {
                        config.serializationAlias!!
                    } else if (config.isSystemDict) {
                        "${dictField.fieldName}_dictText"
                    } else {
                        config.getGeneratedFieldName()
                    }
                    generateGetterSetter(FieldInfo(dictFieldName, "String"))
                }
            }
            
            appendLine("}")
        }
    }
    
    private fun StringBuilder.generateField(field: FieldInfo) {
        appendLine("    /**")
        appendLine("     * ${field.name}")
        appendLine("     */")
        appendLine("    private ${field.type} ${field.name};")
        appendLine()
    }
    
    private fun StringBuilder.generateGetterSetter(field: FieldInfo) {
        val capitalizedName = field.name.replaceFirstChar { it.uppercase() }.toString()
        val fieldType = field.type
        
        // Getter
        appendLine("    public $fieldType get$capitalizedName() {")
        appendLine("        return ${field.name};")
        appendLine("    }")
        appendLine()
        
        // Setter
        appendLine("    public void set$capitalizedName($fieldType ${field.name}) {")
        appendLine("        this.${field.name} = ${field.name};")
        appendLine("    }")
        appendLine()
    }
}