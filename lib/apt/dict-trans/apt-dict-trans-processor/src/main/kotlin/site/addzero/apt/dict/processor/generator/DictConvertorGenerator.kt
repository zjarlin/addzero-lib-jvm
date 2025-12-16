package site.addzero.apt.dict.processor.generator

import site.addzero.apt.dict.processor.model.EntityInfo
import site.addzero.apt.dict.processor.model.NestedEntityField
import javax.annotation.processing.ProcessingEnvironment

/**
 * 字典转换器生成器
 */
class DictConvertorGenerator(private val processingEnv: ProcessingEnvironment) {

    private fun getEntityTypeName(nestedField: NestedEntityField, entityInfo: EntityInfo): String {
        val nestedInfo = nestedField.nestedEntityInfo
        return if (nestedInfo != null && nestedInfo.packageName != entityInfo.packageName) {
            "${nestedInfo.packageName}.${nestedInfo.simpleName}"
        } else {
            nestedField.elementType ?: nestedField.fieldType
        }
    }

    private fun getDtoTypeName(nestedField: NestedEntityField, entityInfo: EntityInfo): String {
        val nestedInfo = nestedField.nestedEntityInfo
        return if (nestedInfo != null && nestedInfo.packageName != entityInfo.packageName) {
            "${nestedInfo.packageName}.${nestedInfo.simpleName}DictDTO"
        } else {
            "${nestedField.elementType ?: nestedField.fieldType}DictDTO"
        }
    }

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
            appendLine("import org.springframework.beans.factory.InitializingBean;")
            appendLine("import org.springframework.beans.factory.annotation.Autowired;")
            appendLine("import org.springframework.stereotype.Component;")
            appendLine("import site.addzero.apt.dict.trans.inter.DictConvertor;")
            appendLine("import site.addzero.apt.dict.trans.inter.TransApi;")
            appendLine("import site.addzero.apt.dict.trans.model.out.SystemDictModelResult;")
            appendLine("import site.addzero.apt.dict.trans.model.out.TableDictModelResult;")
            appendLine("import site.addzero.apt.dict.trans.registry.DictConvertorRegistry;")
            appendLine("import java.util.*;")
            appendLine("import java.util.stream.Collectors;")

            // 添加嵌套实体的导入
            val imports = mutableSetOf<String>()
            entityInfo.nestedEntityFields.forEach { nestedField ->
                nestedField.nestedEntityInfo?.let { nestedInfo ->
                    if (nestedInfo.packageName != entityInfo.packageName) {
                        imports.add("${nestedInfo.packageName}.${nestedInfo.simpleName}")
                        imports.add("${nestedInfo.packageName}.${nestedInfo.simpleName}DictDTO")
                        imports.add("${nestedInfo.packageName}.${nestedInfo.simpleName}DictConvertor")
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
            appendLine(" * $entityClassName 的字典转换器")
            appendLine(" * 自动生成，请勿手动修改")
            appendLine(" */")
            appendLine("@Component")
            appendLine("public class $className implements DictConvertor<$entityClassName, $dtoClassName>, InitializingBean {")
            appendLine()
            
            appendLine("    private TransApi transApi;")
            appendLine()
            
            // 生成嵌套转换器的字段
            entityInfo.nestedEntityFields.forEach { nestedField ->
                val nestedInfo = nestedField.nestedEntityInfo
                val nestedConverterType = if (nestedInfo != null && nestedInfo.packageName != entityInfo.packageName) {
                    "${nestedInfo.packageName}.${nestedInfo.simpleName}DictConvertor"
                } else {
                    "${nestedField.elementType ?: nestedField.fieldType}DictConvertor"
                }
                appendLine("    private $nestedConverterType ${nestedField.fieldName}Convertor;")
                appendLine()
            }

            appendLine("    @Autowired")
            appendLine("    public $className(TransApi transApi) {")
            appendLine("        this.transApi = transApi;")

            // 初始化嵌套转换器
            entityInfo.nestedEntityFields.forEach { nestedField ->
                val nestedInfo = nestedField.nestedEntityInfo
                val nestedConverterType = if (nestedInfo != null && nestedInfo.packageName != entityInfo.packageName) {
                    "${nestedInfo.packageName}.${nestedInfo.simpleName}DictConvertor"
                } else {
                    "${nestedField.elementType ?: nestedField.fieldType}DictConvertor"
                }
                appendLine("        this.${nestedField.fieldName}Convertor = new $nestedConverterType(transApi);")
            }

            appendLine("    }")
            appendLine()
            
            // 生成 afterPropertiesSet 方法
            appendLine("    @Override")
            appendLine("    public void afterPropertiesSet() {")
            appendLine("        DictConvertorRegistry.register($entityClassName.class, $dtoClassName.class, this);")
            appendLine("    }")
            appendLine()
            
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
                    appendLine("            .map(${entityClassName}::get${dictField.fieldName.replaceFirstChar { it.uppercase() }.toString().toString()})")
                    appendLine("            .filter(Objects::nonNull)")
                    appendLine("            .map(String::valueOf)")
                    appendLine("            .collect(Collectors.toSet());")
                        appendLine()
                    }
                }
                
                // 批量查询系统字典
                systemDictFields.forEach { dictField ->
                    dictField.dictConfigs.filter { it.isSystemDict }.forEach { config ->
                        appendLine("        final Map<String, String> ${dictField.fieldName}_${config.dictCode}_map;")
                        appendLine("        if (!${dictField.fieldName}_${config.dictCode}_keys.isEmpty()) {")
                        appendLine("            String ${dictField.fieldName}_${config.dictCode}_dictCodes = \"${config.dictCode}\";")
                        appendLine("            String ${dictField.fieldName}_${config.dictCode}_keysStr = String.join(\",\", ${dictField.fieldName}_${config.dictCode}_keys);")
                        appendLine("            List<SystemDictModelResult> ${dictField.fieldName}_${config.dictCode}_results = transApi.translateDictBatchCode2name(${dictField.fieldName}_${config.dictCode}_dictCodes, ${dictField.fieldName}_${config.dictCode}_keysStr);")
                        appendLine("            ${dictField.fieldName}_${config.dictCode}_map = ${dictField.fieldName}_${config.dictCode}_results.stream()")
                        appendLine("                .collect(Collectors.toMap(r -> r.getValue(), r -> r.getLabel()));")
                        appendLine("        } else {")
                        appendLine("            ${dictField.fieldName}_${config.dictCode}_map = new HashMap<>();")
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
                        appendLine("            .map(${entityClassName}::get${dictField.fieldName.replaceFirstChar { it.uppercase() }.toString().toString()})")
                        appendLine("            .filter(Objects::nonNull)")
                        appendLine("            .map(String::valueOf)")
                        appendLine("            .collect(Collectors.toSet());")
                        appendLine()
                        
                        appendLine("        final Map<String, String> ${mapKey}_map;")
                        appendLine("        if (!${mapKey}_keys.isEmpty()) {")
                        appendLine("            String ${mapKey}_keysStr = String.join(\",\", ${mapKey}_keys);")
                        appendLine("            List<TableDictModelResult> ${mapKey}_results = transApi.translateTableBatchCode2name(")
                        appendLine("                \"${config.tableName}\", \"${config.nameColumn}\", \"${config.codeColumn}\", ${mapKey}_keysStr);")
                        appendLine("            ${mapKey}_map = ${mapKey}_results.stream()")
                        appendLine("                .collect(Collectors.toMap(r -> r.getCodeColumnValue(), r -> r.getNameColumnValue()));")
                        appendLine("        } else {")
                        appendLine("            ${mapKey}_map = new HashMap<>();")
                        appendLine("        }")
                        appendLine()
                    }
                }
            }
        }
        
        // 批量处理嵌套实体
        entityInfo.nestedEntityFields.forEach { nestedField ->
            val entityTypeName = getEntityTypeName(nestedField, entityInfo)
            val dtoTypeName = getDtoTypeName(nestedField, entityInfo)
            if (nestedField.isCollection) {
                appendLine("        // 批量处理嵌套集合: ${nestedField.fieldName}")
                appendLine("        List<$entityTypeName> all${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()} = entities.stream()")
                appendLine("            .filter(Objects::nonNull)")
                appendLine("            .map(${entityClassName}::get${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()})")
                appendLine("            .filter(Objects::nonNull)")
                appendLine("            .flatMap(List::stream)")
                appendLine("            .filter(Objects::nonNull)")
                appendLine("            .collect(Collectors.toList());")
                appendLine("        List<$dtoTypeName> converted${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()} = ")
                appendLine("            ${nestedField.fieldName}Convertor.codes2names(all${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()});")
                appendLine("        Map<$entityTypeName, $dtoTypeName> ${nestedField.fieldName}Map = new HashMap<>();")
                appendLine("        for (int i = 0; i < all${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()}.size(); i++) {")
                appendLine("            if (i < converted${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()}.size()) {")
                appendLine("                ${nestedField.fieldName}Map.put(all${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()}.get(i), converted${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()}.get(i));")
                appendLine("            }")
                appendLine("        }")
                appendLine()
            } else {
                appendLine("        // 批量处理嵌套对象: ${nestedField.fieldName}")
                appendLine("        List<$entityTypeName> all${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()} = entities.stream()")
                appendLine("            .filter(Objects::nonNull)")
                appendLine("            .map(${entityClassName}::get${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()})")
                appendLine("            .filter(Objects::nonNull)")
                appendLine("            .distinct()")
                appendLine("            .collect(Collectors.toList());")
                appendLine("        List<$dtoTypeName> converted${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()} = ")
                appendLine("            ${nestedField.fieldName}Convertor.codes2names(all${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()});")
                appendLine("        Map<$entityTypeName, $dtoTypeName> ${nestedField.fieldName}Map = new HashMap<>();")
                appendLine("        for (int i = 0; i < all${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()}.size(); i++) {")
                appendLine("            if (i < converted${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()}.size()) {")
                appendLine("                ${nestedField.fieldName}Map.put(all${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()}.get(i), converted${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()}.get(i));")
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
                val capitalizedName = field.name.replaceFirstChar { it.uppercase() }.toString()
                appendLine("                dto.set$capitalizedName(entity.get$capitalizedName());")
            }
        }
        
        // 设置字典翻译字段
        entityInfo.dictFields.forEach { dictField ->
            dictField.dictConfigs.forEach { config ->
                val dictFieldName = if (!config.serializationAlias.isNullOrBlank()) {
                    config.serializationAlias!!
                } else if (config.isSystemDict) {
                    "${dictField.fieldName}_dictText"
                } else {
                    config.getGeneratedFieldName()
                }
                val capitalizedDictFieldName = dictFieldName.replaceFirstChar { it.uppercase() }.toString()
                val capitalizedFieldName = dictField.fieldName.replaceFirstChar { it.uppercase() }.toString()

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
            val capitalizedFieldName = nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()
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

        // 批量收集所有需要反向翻译的字典名称
        if (entityInfo.dictFields.isNotEmpty()) {
            appendLine("        // 批量收集字典反向翻译数据，避免N+1问题")

            // 系统字典批量反向查询
            val systemDictFields = entityInfo.dictFields.filter { field ->
                field.dictConfigs.any { it.isSystemDict }
            }

            if (systemDictFields.isNotEmpty()) {
                appendLine("        // 收集系统字典名称")
            systemDictFields.forEach { dictField ->
                dictField.dictConfigs.filter { it.isSystemDict }.forEach { config ->
                    val dictFieldName = if (!config.serializationAlias.isNullOrBlank()) {
                        config.serializationAlias!!
                    } else {
                        "${dictField.fieldName}_dictText"
                    }
                    appendLine("        Set<String> ${dictField.fieldName}_${config.dictCode}_names = dtos.stream()")
                    appendLine("            .filter(Objects::nonNull)")
                    appendLine("            .map(dto -> dto.get${dictFieldName.replaceFirstChar { it.uppercase() }.toString()}())")
                    appendLine("            .filter(Objects::nonNull)")
                    appendLine("            .collect(Collectors.toSet());")
                    appendLine()
                }
            }

                // 批量查询系统字典反向
                systemDictFields.forEach { dictField ->
                    dictField.dictConfigs.filter { it.isSystemDict }.forEach { config ->
                        appendLine("        final Map<String, String> ${dictField.fieldName}_${config.dictCode}_reverse_map;")
                        appendLine("        if (!${dictField.fieldName}_${config.dictCode}_names.isEmpty()) {")
                        appendLine("            String ${dictField.fieldName}_${config.dictCode}_dictCodes = \"${config.dictCode}\";")
                        appendLine("            String ${dictField.fieldName}_${config.dictCode}_namesStr = String.join(\",\", ${dictField.fieldName}_${config.dictCode}_names);")
                        appendLine("            List<SystemDictModelResult> ${dictField.fieldName}_${config.dictCode}_results = transApi.translateDictBatchName2code(${dictField.fieldName}_${config.dictCode}_dictCodes, ${dictField.fieldName}_${config.dictCode}_namesStr);")
                        appendLine("            ${dictField.fieldName}_${config.dictCode}_reverse_map = ${dictField.fieldName}_${config.dictCode}_results.stream()")
                        appendLine("                .collect(Collectors.toMap(r -> r.getLabel(), r -> r.getValue()));")
                        appendLine("        } else {")
                        appendLine("            ${dictField.fieldName}_${config.dictCode}_reverse_map = new HashMap<>();")
                        appendLine("        }")
                        appendLine()
                    }
                }
            }

            // 任意表字典批量反向查询
            val tableDictFields = entityInfo.dictFields.filter { field ->
                field.dictConfigs.any { !it.isSystemDict }
            }

            if (tableDictFields.isNotEmpty()) {
                appendLine("        // 收集任意表字典名称")
                tableDictFields.forEach { dictField ->
                    dictField.dictConfigs.filter { !it.isSystemDict }.forEach { config ->
                        val dictFieldName = if (!config.serializationAlias.isNullOrBlank()) {
                            config.serializationAlias!!
                        } else {
                            config.getGeneratedFieldName()
                        }
                        val mapKey = "${dictField.fieldName}_${config.tableName}_${config.nameColumn}"
                        appendLine("        Set<String> ${mapKey}_names = dtos.stream()")
                        appendLine("            .filter(Objects::nonNull)")
                        appendLine("            .map(dto -> dto.get${dictFieldName.replaceFirstChar { it.uppercase() }.toString()}())")
                        appendLine("            .filter(Objects::nonNull)")
                        appendLine("            .collect(Collectors.toSet());")
                        appendLine()

                        appendLine("        final Map<String, String> ${mapKey}_reverse_map;")
                        appendLine("        if (!${mapKey}_names.isEmpty()) {")
                        appendLine("            String ${mapKey}_namesStr = String.join(\",\", ${mapKey}_names);")
                        appendLine("            List<TableDictModelResult> ${mapKey}_results = transApi.translateTableBatchName2code(")
                        appendLine("                \"${config.tableName}\", \"${config.nameColumn}\", \"${config.codeColumn}\", ${mapKey}_namesStr);")
                        appendLine("            ${mapKey}_reverse_map = ${mapKey}_results.stream()")
                        appendLine("                .collect(Collectors.toMap(r -> r.getNameColumnValue(), r -> r.getCodeColumnValue()));")
                        appendLine("        } else {")
                        appendLine("            ${mapKey}_reverse_map = new HashMap<>();")
                        appendLine("        }")
                        appendLine()
                    }
                }
            }
        }

        // 批量处理嵌套实体反向转换
        entityInfo.nestedEntityFields.forEach { nestedField ->
            val entityTypeName = getEntityTypeName(nestedField, entityInfo)
            val dtoTypeName = getDtoTypeName(nestedField, entityInfo)
            if (nestedField.isCollection) {
                appendLine("        // 批量处理嵌套集合反向转换: ${nestedField.fieldName}")
                appendLine("        List<$dtoTypeName> all${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()}DTOs = dtos.stream()")
                appendLine("            .filter(Objects::nonNull)")
                appendLine("            .map($dtoClassName::get${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()})")
                appendLine("            .filter(Objects::nonNull)")
                appendLine("            .flatMap(List::stream)")
                appendLine("            .filter(Objects::nonNull)")
                appendLine("            .collect(Collectors.toList());")
                appendLine("        List<$entityTypeName> converted${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()} = ")
                appendLine("            ${nestedField.fieldName}Convertor.name2codes(all${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()}DTOs);")
                appendLine("        Map<$dtoTypeName, $entityTypeName> ${nestedField.fieldName}ReverseMap = new HashMap<>();")
                appendLine("        for (int i = 0; i < all${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()}DTOs.size(); i++) {")
                appendLine("            if (i < converted${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()}.size()) {")
                appendLine("                ${nestedField.fieldName}ReverseMap.put(all${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()}DTOs.get(i), converted${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()}.get(i));")
                appendLine("            }")
                appendLine("        }")
                appendLine()
            } else {
                appendLine("        // 批量处理嵌套对象反向转换: ${nestedField.fieldName}")
                appendLine("        List<$dtoTypeName> all${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()}DTOs = dtos.stream()")
                appendLine("            .filter(Objects::nonNull)")
                appendLine("            .map($dtoClassName::get${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()})")
                appendLine("            .filter(Objects::nonNull)")
                appendLine("            .distinct()")
                appendLine("            .collect(Collectors.toList());")
                appendLine("        List<$entityTypeName> converted${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()} = ")
                appendLine("            ${nestedField.fieldName}Convertor.name2codes(all${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()}DTOs);")
                appendLine("        Map<$dtoTypeName, $entityTypeName> ${nestedField.fieldName}ReverseMap = new HashMap<>();")
                appendLine("        for (int i = 0; i < all${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()}DTOs.size(); i++) {")
                appendLine("            if (i < converted${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()}.size()) {")
                appendLine("                ${nestedField.fieldName}ReverseMap.put(all${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()}DTOs.get(i), converted${nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()}.get(i));")
                appendLine("            }")
                appendLine("        }")
                appendLine()
            }
        }

        // 转换DTO回实体
        appendLine("        // 转换DTO回实体")
        appendLine("        return dtos.stream()")
        appendLine("            .map(dto -> {")
        appendLine("                if (dto == null) return null;")
        appendLine("                $entityClassName entity = new $entityClassName();")
        appendLine()

        // 复制基本字段（排除嵌套实体字段）
        entityInfo.allFields.forEach { field ->
            val isNestedEntity = entityInfo.nestedEntityFields.any { it.fieldName == field.name }
            if (!isNestedEntity) {
                val capitalizedName = field.name.replaceFirstChar { it.uppercase() }.toString()
                appendLine("                entity.set$capitalizedName(dto.get$capitalizedName());")
            }
        }

        // 设置字典字段的反向翻译
        entityInfo.dictFields.forEach { dictField ->
            dictField.dictConfigs.forEach { config ->
                val dictFieldName = if (!config.serializationAlias.isNullOrBlank()) {
                    config.serializationAlias!!
                } else if (config.isSystemDict) {
                    "${dictField.fieldName}_dictText"
                } else {
                    config.getGeneratedFieldName()
                }
                val capitalizedFieldName = dictField.fieldName.replaceFirstChar { it.uppercase() }.toString()
                val capitalizedDictFieldName = dictFieldName.replaceFirstChar { it.uppercase() }.toString()

                val codeValue = when (dictField.fieldType) {
                    "int", "java.lang.Integer" -> "Integer.valueOf(code)"
                    "long", "java.lang.Long" -> "Long.valueOf(code)"
                    "short", "java.lang.Short" -> "Short.valueOf(code)"
                    "byte", "java.lang.Byte" -> "Byte.valueOf(code)"
                    "double", "java.lang.Double" -> "Double.valueOf(code)"
                    "float", "java.lang.Float" -> "Float.valueOf(code)"
                    "boolean", "java.lang.Boolean" -> "Boolean.valueOf(code)"
                    else -> "code"
                }

                if (config.isSystemDict) {
                    val mapName = "${dictField.fieldName}_${config.dictCode}_reverse_map"
                    appendLine("                if (dto.get$capitalizedDictFieldName() != null) {")
                    appendLine("                    String code = $mapName.get(dto.get$capitalizedDictFieldName());")
                    appendLine("                    if (code != null) {")
                    appendLine("                        entity.set$capitalizedFieldName($codeValue);")
                    appendLine("                    }")
                    appendLine("                }")
                } else {
                    val mapName = "${dictField.fieldName}_${config.tableName}_${config.nameColumn}_reverse_map"
                    appendLine("                if (dto.get$capitalizedDictFieldName() != null) {")
                    appendLine("                    String code = $mapName.get(dto.get$capitalizedDictFieldName());")
                    appendLine("                    if (code != null) {")
                    appendLine("                        entity.set$capitalizedFieldName($codeValue);")
                    appendLine("                    }")
                    appendLine("                }")
                }
            }
        }

        // 设置嵌套实体字段的反向转换
        entityInfo.nestedEntityFields.forEach { nestedField ->
            val capitalizedFieldName = nestedField.fieldName.replaceFirstChar { it.uppercase() }.toString()
            if (nestedField.isCollection) {
                appendLine("                if (dto.get$capitalizedFieldName() != null) {")
                appendLine("                    List<${nestedField.elementType}> convertedList = dto.get$capitalizedFieldName().stream()")
                appendLine("                        .map(item -> ${nestedField.fieldName}ReverseMap.get(item))")
                appendLine("                        .filter(Objects::nonNull)")
                appendLine("                        .collect(Collectors.toList());")
                appendLine("                    entity.set$capitalizedFieldName(convertedList);")
                appendLine("                }")
            } else {
                appendLine("                if (dto.get$capitalizedFieldName() != null) {")
                appendLine("                    entity.set$capitalizedFieldName(${nestedField.fieldName}ReverseMap.get(dto.get$capitalizedFieldName()));")
                appendLine("                }")
            }
        }

        appendLine("                return entity;")
        appendLine("            })")
        appendLine("            .collect(Collectors.toList());")
        appendLine("    }")
        appendLine()
    }
}