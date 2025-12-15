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
                    if (nestedField.isCollection) {
                        val elementType = nestedField.elementType ?: nestedField.fieldType
                        appendLine("    /**")
                        appendLine("     * ${field.name} - 嵌套实体集合")
                        appendLine("     */")
                        appendLine("    private List<${elementType}DictDTO> ${field.name};")
                        appendLine()
                    } else {
                        appendLine("    /**")
                        appendLine("     * ${field.name} - 嵌套实体")
                        appendLine("     */")
                        appendLine("    private ${nestedField.fieldType}DictDTO ${field.name};")
                        appendLine()
                    }
                }
            }
            
            // 生成字典字段
            entityInfo.dictFields.forEach { dictField ->
                dictField.dictConfigs.forEach { config ->
                    val dictFieldName = if (config.serializationAlias.isNullOrBlank()) {
                        "${dictField.fieldName}_dictText"
                    } else {
                        config.serializationAlias
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
                    if (nestedField.isCollection) {
                        val elementType = nestedField.elementType ?: nestedField.fieldType
                        generateGetterSetter(FieldInfo(field.name, "List<${elementType}DictDTO>"))
                    } else {
                        generateGetterSetter(FieldInfo(field.name, "${nestedField.fieldType}DictDTO"))
                    }
                }
            }
            
            // 生成字典字段的getter和setter
            entityInfo.dictFields.forEach { dictField ->
                dictField.dictConfigs.forEach { config ->
                    val dictFieldName = if (config.serializationAlias.isNullOrBlank()) {
                        "${dictField.fieldName}_dictText"
                    } else {
                        config.serializationAlias
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
        val capitalizedName = field.name.replaceFirstChar { it.uppercase() }
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