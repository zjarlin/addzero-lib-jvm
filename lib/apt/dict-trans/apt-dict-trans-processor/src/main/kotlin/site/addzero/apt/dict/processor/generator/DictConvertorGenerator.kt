package site.addzero.apt.dict.processor.generator

import site.addzero.apt.dict.processor.model.EntityInfo
import javax.annotation.processing.ProcessingEnvironment

/**
 * 字典转换器生成器
 */
class DictConvertorGenerator(private val processingEnv: ProcessingEnvironment) {
    
    /**
     * 生成字典转换器类
     */
    fun generateDictConvertor(entityInfo: EntityInfo): String {
        val className = "${entityInfo.simpleName}DictConvertor"
        val entityClassName = entityInfo.simpleName
        val dtoClassName = "${entityInfo.simpleName}DictDTO"
        val packageName = entityInfo.packageName
        
        return buildString {
            appendLine("package $packageName;")
            appendLine()
            
            // 导入必要的类
            appendLine("import site.addzero.apt.dict.trans.inter.DictConvertor;")
            appendLine("import site.addzero.apt.dict.trans.inter.TransApi;")
            appendLine("import site.addzero.apt.dict.trans.model.out.SystemDictModelResult;")
            appendLine("import site.addzero.apt.dict.trans.model.out.TableDictModelResult;")
            appendLine("import java.util.*;")
            appendLine("import java.util.stream.Collectors;")
            appendLine()
            
            appendLine("/**")
            appendLine(" * $entityClassName 的字典转换器")
            appendLine(" * 自动生成，请勿手动修改")
            appendLine(" */")
            appendLine("public class $className implements DictConvertor<$entityClassName, $dtoClassName> {")
            appendLine()
            
            appendLine("    private TransApi transApi;")
            appendLine()
            
            appendLine("    public $className(TransApi transApi) {")
            appendLine("        this.transApi = transApi;")
            appendLine("    }")
            appendLine()
            
            // 生成嵌套转换器的字段
            entityInfo.nestedEntityFields.forEach { nestedField ->
                val nestedConverterType = "${nestedField.elementType ?: nestedField.fieldType}DictConvertor"
                appendLine("    private $nestedConverterType ${nestedField.fieldName}Convertor;")
                appendLine()
            }
            
            // 生成 codes2names 方法
            generateCodes2NamesMethod(entityInfo, entityClassName, dtoClassName)
            
            // 生成 name2codes 方法
            generateName2CodesMethod(entityInfo, entityClassName, dtoClassName)
            
            appendLine("}")
        }
    }
    
    private fun StringBuilder.generateCodes2NamesMethod(
        entityInfo: EntityInfo,
        entityClassName: String,
        dtoClassName: String
    ) {
        appendLine("    @Override")
        appendLine("    public List<$dtoClassName> codes2names(List<$entityClassName> entities) {")
        appendLine("        if (entities == null || entities.isEmpty()) {")
        appendLine("            return new ArrayList<>();")
        appendLine("        }")
        appendLine()
        
        // 批量收集所有需要翻译的字典编码
        if (entityInfo.dictFields.isNotEmpty()) {
            appendLine("        // 批量收集字典翻译数据，避免N+1问题")
            
            // 系统字典批量查询
            val systemDictFields = entityInfo.dictFields.filter { field ->
                field.dictConfigs.any { it.isSystemDict }
            }
            
            if (systemDictFields.isNotEmpty()) {
                appendLine("        // 收集系统字典编码和键值")
                systemDictFields.forEach { dictField ->
                    dictField.dictConfigs.filter { it.isSystemDict }.forEach { config ->
                        appendLine("        Set<String> ${dictField.fieldName}_${config.dictCode}_keys = entities.stream()")
                        appendLine("            .filter(Objects::nonNull)")
                        appendLine("            .map(${entityClassName}::get${dictField.fieldName.replaceFirstChar { it.uppercase() }})")
                        appendLine("            .filter(Objects::nonNull)")
                        appendLine("            .map(String::valueOf)")
                        appendLine("            .collect(Collectors.toSet());")
                        appendLine()
                    }
                }
                
                // 批量查询系统字典
                systemDictFields.forEach { dictField ->
                    dictField.dictConfigs.filter { it.isSystemDict }.forEach { config ->
                        appendLine("        Map<String, String> ${dictField.fieldName}_${config.dictCode}_map = new HashMap<>();")
                        appendLine("        if (!${dictField.fieldName}_${config.dictCode}_keys.isEmpty()) {")
                        appendLine("            String dictCodes = \"${config.dictCode}\";")
                        appendLine("            String keys = String.join(\",\", ${dictField.fieldName}_${config.dictCode}_keys);")
                        appendLine("            List<SystemDictModelResult> results = transApi.translateDictBatchCode2name(dictCodes, keys);")
                        appendLine("            ${dictField.fieldName}_${config.dictCode}_map = results.stream()")
                        appendLine("                .collect(Collectors.toMap(r -> r.getValue(), r -> r.getLabel()));")
                        appendLine("        }")
                        appendLine()
                    }
                }
            }
            
            // 任意表字典批量查询
            val tableDictFields = entityInfo.dictFields.filter { field ->
                field.dictConfigs.any { !it.isSystemDict }
            }
            
            if (tableDictFields.isNotEmpty()) {
                appendLine("        // 收集任意表字典数据")
                tableDictFields.forEach { dictField ->
                    dictField.dictConfigs.filter { !it.isSystemDict }.forEach { config ->
                        val mapKey = "${dictField.fieldName}_${config.tableName}_${config.nameColumn}"
                        appendLine("        Set<String> ${mapKey}_keys = entities.stream()")
                        appendLine("            .filter(Objects::nonNull)")
                        appendLine("            .map(${entityClassName}::get${dictField.fieldName.replaceFirstChar { it.uppercase() }})")
                        appendLine("            .filter(Objects::nonNull)")
                        appendLine("            .map(String::valueOf)")
                        appendLine("            .collect(Collectors.toSet());")
                        appendLine()
                        
                        appendLine("        Map<String, String> ${mapKey}_map = new HashMap<>();")
                        appendLine("        if (!${mapKey}_keys.isEmpty()) {")
                        appendLine("            String ${mapKey}_keysStr = String.join(\",\", ${mapKey}_keys);")
                        appendLine("            List<TableDictModelResult> ${mapKey}_results = transApi.translateTableBatchCode2name(")
                        appendLine("                \"${config.tableName}\", \"${config.nameColumn}\", \"${config.codeColumn}\", ${mapKey}_keysStr);")
                        appendLine("            ${mapKey}_map = ${mapKey}_results.stream()")
                        appendLine("                .collect(Collectors.toMap(r -> r.getCodeColumnValue(), r -> r.getNameColumnValue()));")
                        appendLine("        }")
                        appendLine()
                    }
                }
            }
        }
        
        // 批量处理嵌套实体
        entityInfo.nestedEntityFields.forEach { nestedField ->
            if (nestedField.isCollection) {
                appendLine("        // 批量处理嵌套集合: ${nestedField.fieldName}")
                appendLine("        List<${nestedField.elementType}> all${nestedField.fieldName.replaceFirstChar { it.uppercase() }} = entities.stream()")
                appendLine("            .filter(Objects::nonNull)")
                appendLine("            .map(${entityClassName}::get${nestedField.fieldName.replaceFirstChar { it.uppercase() }})")
                appendLine("            .filter(Objects::nonNull)")
                appendLine("            .flatMap(List::stream)")
                appendLine("            .filter(Objects::nonNull)")
                appendLine("            .collect(Collectors.toList());")
                appendLine("        List<${nestedField.elementType}DictDTO> converted${nestedField.fieldName.replaceFirstChar { it.uppercase() }} = ")
                appendLine("            ${nestedField.fieldName}Convertor.codes2names(all${nestedField.fieldName.replaceFirstChar { it.uppercase() }});")
                appendLine("        Map<${nestedField.elementType}, ${nestedField.elementType}DictDTO> ${nestedField.fieldName}Map = new HashMap<>();")
                appendLine("        for (int i = 0; i < all${nestedField.fieldName.replaceFirstChar { it.uppercase() }}.size(); i++) {")
                appendLine("            if (i < converted${nestedField.fieldName.replaceFirstChar { it.uppercase() }}.size()) {")
                appendLine("                ${nestedField.fieldName}Map.put(all${nestedField.fieldName.replaceFirstChar { it.uppercase() }}.get(i), converted${nestedField.fieldName.replaceFirstChar { it.uppercase() }}.get(i));")
                appendLine("            }")
                appendLine("        }")
                appendLine()
            } else {
                appendLine("        // 批量处理嵌套对象: ${nestedField.fieldName}")
                appendLine("        List<${nestedField.fieldType}> all${nestedField.fieldName.replaceFirstChar { it.uppercase() }} = entities.stream()")
                appendLine("            .filter(Objects::nonNull)")
                appendLine("            .map(${entityClassName}::get${nestedField.fieldName.replaceFirstChar { it.uppercase() }})")
                appendLine("            .filter(Objects::nonNull)")
                appendLine("            .distinct()")
                appendLine("            .collect(Collectors.toList());")
                appendLine("        List<${nestedField.fieldType}DictDTO> converted${nestedField.fieldName.replaceFirstChar { it.uppercase() }} = ")
                appendLine("            ${nestedField.fieldName}Convertor.codes2names(all${nestedField.fieldName.replaceFirstChar { it.uppercase() }});")
                appendLine("        Map<${nestedField.fieldType}, ${nestedField.fieldType}DictDTO> ${nestedField.fieldName}Map = new HashMap<>();")
                appendLine("        for (int i = 0; i < all${nestedField.fieldName.replaceFirstChar { it.uppercase() }}.size(); i++) {")
                appendLine("            if (i < converted${nestedField.fieldName.replaceFirstChar { it.uppercase() }}.size()) {")
                appendLine("                ${nestedField.fieldName}Map.put(all${nestedField.fieldName.replaceFirstChar { it.uppercase() }}.get(i), converted${nestedField.fieldName.replaceFirstChar { it.uppercase() }}.get(i));")
                appendLine("            }")
                appendLine("        }")
                appendLine()
            }
        }
        
        // 转换实体
        appendLine("        // 转换实体")
        appendLine("        return entities.stream()")
        appendLine("            .map(entity -> {")
        appendLine("                if (entity == null) return null;")
        appendLine("                $dtoClassName dto = new $dtoClassName();")
        appendLine()
        
        // 复制基本字段（排除嵌套实体字段）
        entityInfo.allFields.forEach { field ->
            val isNestedEntity = entityInfo.nestedEntityFields.any { it.fieldName == field.name }
            if (!isNestedEntity) {
                val capitalizedName = field.name.replaceFirstChar { it.uppercase() }
                appendLine("                dto.set$capitalizedName(entity.get$capitalizedName());")
            }
        }
        
        // 设置字典翻译字段
        entityInfo.dictFields.forEach { dictField ->
            dictField.dictConfigs.forEach { config ->
                val dictFieldName = if (config.serializationAlias.isNullOrBlank()) {
                    "${dictField.fieldName}_dictText"
                } else {
                    config.serializationAlias
                }
                val capitalizedDictFieldName = dictFieldName.replaceFirstChar { it.uppercase() }
                val capitalizedFieldName = dictField.fieldName.replaceFirstChar { it.uppercase() }
                
                if (config.isSystemDict) {
                    val mapName = "${dictField.fieldName}_${config.dictCode}_map"
                    appendLine("                if (entity.get$capitalizedFieldName() != null) {")
                    appendLine("                    dto.set$capitalizedDictFieldName($mapName.get(String.valueOf(entity.get$capitalizedFieldName())));")
                    appendLine("                }")
                } else {
                    val mapName = "${dictField.fieldName}_${config.tableName}_${config.nameColumn}_map"
                    appendLine("                if (entity.get$capitalizedFieldName() != null) {")
                    appendLine("                    dto.set$capitalizedDictFieldName($mapName.get(String.valueOf(entity.get$capitalizedFieldName())));")
                    appendLine("                }")
                }
            }
        }
        
        // 设置嵌套实体字段
        entityInfo.nestedEntityFields.forEach { nestedField ->
            val capitalizedFieldName = nestedField.fieldName.replaceFirstChar { it.uppercase() }
            if (nestedField.isCollection) {
                appendLine("                if (entity.get$capitalizedFieldName() != null) {")
                appendLine("                    List<${nestedField.elementType}DictDTO> convertedList = entity.get$capitalizedFieldName().stream()")
                appendLine("                        .map(item -> ${nestedField.fieldName}Map.get(item))")
                appendLine("                        .filter(Objects::nonNull)")
                appendLine("                        .collect(Collectors.toList());")
                appendLine("                    dto.set$capitalizedFieldName(convertedList);")
                appendLine("                }")
            } else {
                appendLine("                if (entity.get$capitalizedFieldName() != null) {")
                appendLine("                    dto.set$capitalizedFieldName(${nestedField.fieldName}Map.get(entity.get$capitalizedFieldName()));")
                appendLine("                }")
            }
        }
        
        appendLine("                return dto;")
        appendLine("            })")
        appendLine("            .collect(Collectors.toList());")
        appendLine("    }")
        appendLine()
    }
    
    private fun StringBuilder.generateName2CodesMethod(
        entityInfo: EntityInfo,
        entityClassName: String,
        dtoClassName: String
    ) {
        appendLine("    @Override")
        appendLine("    public List<$entityClassName> name2codes(List<$dtoClassName> dtos) {")
        appendLine("        if (dtos == null || dtos.isEmpty()) {")
        appendLine("            return new ArrayList<>();")
        appendLine("        }")
        appendLine()
        appendLine("        // TODO: 实现反向转换逻辑")
        appendLine("        // 这里需要实现从DTO转换回实体的逻辑")
        appendLine("        return new ArrayList<>();")
        appendLine("    }")
        appendLine()
    }
}