package site.addzero.apt.dict.processor

import com.google.auto.service.AutoService
import site.addzero.apt.dict.annotations.Dict
import site.addzero.apt.dict.annotations.DictField
import site.addzero.apt.dict.dsl.*
import java.io.IOException
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic
import javax.tools.StandardLocation

/**
 * APT processor for compile-time dictionary translation
 * 
 * This processor generates enhanced entity classes at compile time using APT (Annotation Processing Tool)
 * instead of runtime reflection, providing better performance and type safety.
 * 
 * The processor now generates pure Java code using JavaEntityEnhancer with Kotlin multiline strings.
 */
@AutoService(Processor::class)
@SupportedAnnotationTypes("site.addzero.apt.dict.annotations.Dict")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class DictTranslateProcessor : AbstractProcessor() {

    private val javaEntityEnhancer = JavaEntityEnhancer()
    private lateinit var incrementalSupport: IncrementalCompilationSupport
    private lateinit var errorHandler: ErrorHandlingManager
    private lateinit var performanceMonitor: PerformanceMonitor
    
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        incrementalSupport = IncrementalCompilationSupport(processingEnv)
        errorHandler = ErrorHandlingManager(processingEnv)
        performanceMonitor = PerformanceMonitor(processingEnv)
        
        performanceMonitor.takeMemorySnapshot("initialization")
        errorHandler.reportInfo("APT Dictionary Translation Processor initialized")
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        if (roundEnv == null) return false
        
        return performanceMonitor.measureTime("totalProcessing") {
            performanceMonitor.takeMemorySnapshot("processing_start")
            
            try {
                val annotatedElements = roundEnv.getElementsAnnotatedWith(Dict::class.java)
                
                errorHandler.reportInfo("Processing ${annotatedElements.size} annotated elements")
                
                for (element in annotatedElements) {
                    if (element.kind != ElementKind.CLASS) {
                        errorHandler.reportError(
                            ErrorType.VALIDATION_ERROR,
                            "@Dict can only be applied to classes",
                            element
                        )
                        continue
                    }
                    
                    try {
                        performanceMonitor.measureTime("processClass") {
                            processClass(element as TypeElement)
                        }
                    } catch (e: Exception) {
                        val recovered = errorHandler.handleAnnotationError(element, "@Dict", e)
                        if (!recovered && errorHandler.isErrorThresholdExceeded()) {
                            errorHandler.reportError(
                                ErrorType.ANNOTATION_PROCESSING_ERROR,
                                "Error threshold exceeded, stopping processing",
                                element,
                                e
                            )
                            break
                        }
                    }
                }
                
                performanceMonitor.takeMemorySnapshot("processing_end")
                
                // Report final statistics
                val stats = errorHandler.getErrorStatistics()
                errorHandler.reportInfo("Processing completed. ${stats}")
                
                // Report performance statistics
                performanceMonitor.logPerformanceReport()
                
                // Check for performance issues
                if (performanceMonitor.isPerformanceDegraded()) {
                    val suggestions = performanceMonitor.getOptimizationSuggestions()
                    suggestions.forEach { suggestion ->
                        errorHandler.reportWarning("Performance optimization suggestion: $suggestion")
                    }
                }
                
                true
                
            } catch (e: Exception) {
                errorHandler.reportError(
                    ErrorType.ANNOTATION_PROCESSING_ERROR,
                    "Fatal error during annotation processing",
                    null,
                    e
                )
                false
            }
        }
    }
    
    private fun processClass(typeElement: TypeElement) {
        val className = typeElement.simpleName.toString()
        val packageName = processingEnv.elementUtils.getPackageOf(typeElement).qualifiedName.toString()
        
        try {
            // 获取 @Dict 注解
            val dictAnnotation = typeElement.getAnnotation(Dict::class.java)
            
            val suffix = dictAnnotation?.suffix ?: "Enhanced"
            val generateExtensions = dictAnnotation?.generateExtensions ?: true
            val generateBuilder = dictAnnotation?.generateBuilder ?: false
            
            // 获取字典字段
            val dictFields = extractDictFields(typeElement)
            
            if (dictFields.isEmpty()) {
                errorHandler.reportWarning(
                    "No @DictField annotations found in class $className",
                    typeElement
                )
                return
            }
            
            // 验证字典字段配置
            validateDictFields(dictFields, typeElement)
            
            // 检查是否需要重新处理（增量编译支持）
            if (!incrementalSupport.needsReprocessing(typeElement, dictFields)) {
                errorHandler.reportInfo(
                    "Skipping $className - no changes detected (incremental compilation)",
                    typeElement
                )
                return
            }
            
            // 创建DSL配置用于Java代码生成
            val dslConfig = try {
                createDslConfigFromAnnotations(typeElement, dictFields)
            } catch (e: Exception) {
                errorHandler.handleDslError("DSL config for $className", e)
                return
            }
            
            // 使用 JavaEntityEnhancer 生成纯Java代码
            val javaCode = try {
                performanceMonitor.measureTime("codeGeneration") {
                    javaEntityEnhancer.generateEnhancedEntityJava(
                        typeElement,
                        dslConfig,
                        packageName
                    )
                }
            } catch (e: Exception) {
                val recovered = errorHandler.handleCodeGenerationError(className, e)
                if (!recovered) return
                // Use fallback code if recovery succeeded
                generateMinimalFallbackClass(className, packageName)
            }
            
            // 写入Java文件
            try {
                performanceMonitor.measureTime("fileWriting") {
                    writeJavaFile(packageName, "${className}${suffix}", javaCode)
                }
            } catch (e: Exception) {
                errorHandler.reportError(
                    ErrorType.FILE_WRITING_ERROR,
                    "Failed to write Java file for $className",
                    typeElement,
                    e
                )
                return
            }
            
            // 更新缓存
            incrementalSupport.updateCache(typeElement, dictFields)
            
            errorHandler.reportInfo(
                "Generated enhanced Java class: ${packageName}.${className}${suffix}",
                typeElement
            )
            
        } catch (e: Exception) {
            errorHandler.reportError(
                ErrorType.ANNOTATION_PROCESSING_ERROR,
                "Unexpected error processing class $className",
                typeElement,
                e
            )
            throw e
        }
    }
    
    /**
     * Creates DSL configuration from annotation data
     */
    private fun createDslConfigFromAnnotations(
        typeElement: TypeElement,
        dictFields: List<DictFieldInfo>
    ): DslTemplateConfig {
        val className = typeElement.simpleName.toString()
        
        // Convert DictFieldInfo to DSL field rules
        val fieldRules = dictFields.map { dictField ->
            val dictConfigs = mutableListOf<DictConfig>()
            
            // Add system dictionary config if present
            if (dictField.dictCode.isNotEmpty()) {
                dictConfigs.add(
                    DictConfig(
                        type = TranslationType.SYSTEM_DICT,
                        dictCode = dictField.dictCode
                    )
                )
            }
            
            // Add table dictionary config if present
            if (dictField.table.isNotEmpty()) {
                dictConfigs.add(
                    DictConfig(
                        type = TranslationType.TABLE_DICT,
                        table = dictField.table,
                        codeColumn = dictField.codeColumn,
                        nameColumn = dictField.nameColumn,
                        condition = dictField.condition
                    )
                )
            }
            
            // Add SPEL expression config if present
            if (dictField.spelExp.isNotEmpty()) {
                dictConfigs.add(
                    DictConfig(
                        type = TranslationType.SPEL,
                        spelExpression = dictField.spelExp
                    )
                )
            }
            
            FieldTranslationRule(
                fieldName = dictField.sourceField,
                translationType = if (dictConfigs.size > 1) TranslationType.MULTI_DICT 
                                 else if (dictField.dictCode.isNotEmpty()) TranslationType.SYSTEM_DICT 
                                 else TranslationType.TABLE_DICT,
                dictConfigs = dictConfigs,
                targetFieldName = dictField.targetField
            )
        }
        
        val entityRule = EntityTranslationRule(
            entityName = className,
            fieldRules = fieldRules
        )
        
        return DslTemplateConfig(
            entityClass = className,
            translationRules = listOf(entityRule)
        )
    }
    
    /**
     * Writes the generated Java code to a file
     */
    private fun writeJavaFile(packageName: String, className: String, javaCode: String) {
        try {
            val sourceFile = processingEnv.filer.createSourceFile("$packageName.$className")
            sourceFile.openWriter().use { writer ->
                writer.write(javaCode)
            }
        } catch (e: IOException) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Failed to write Java file: ${e.message}"
            )
        }
    }
    
    private fun extractDictFields(typeElement: TypeElement): List<DictFieldInfo> {
        val dictFields = mutableListOf<DictFieldInfo>()
        
        // 获取所有字段，包括嵌套字段
        extractDictFieldsRecursively(typeElement, dictFields, "")
        
        return dictFields
    }
    
    /**
     * 递归提取字典字段，自动检测嵌套结构和 List
     */
    private fun extractDictFieldsRecursively(
        typeElement: TypeElement, 
        dictFields: MutableList<DictFieldInfo>,
        fieldPrefix: String,
        depth: Int = 0
    ) {
        // 防止无限递归
        if (depth > 5) return
        
        for (enclosedElement in typeElement.enclosedElements) {
            if (enclosedElement.kind == ElementKind.FIELD) {
                val fieldElement = enclosedElement as VariableElement
                val fieldName = fieldElement.simpleName.toString()
                val fullFieldName = if (fieldPrefix.isEmpty()) fieldName else "$fieldPrefix.$fieldName"
                
                // 处理直接标注 @DictField 的字段
                val dictFieldAnnotations = fieldElement.getAnnotationsByType(DictField::class.java)
                for (annotation in dictFieldAnnotations) {
                    val dictFieldInfo = parseDictFieldAnnotation(fieldElement, annotation, fullFieldName)
                    dictFields.add(dictFieldInfo)
                }
                
                // 自动检测嵌套结构
                val fieldType = fieldElement.asType()
                val fieldTypeString = fieldType.toString()
                
                // 检测 List 类型
                if (isListType(fieldTypeString)) {
                    val elementType = extractListElementType(fieldTypeString)
                    if (elementType != null && isCustomType(elementType)) {
                        val elementTypeElement = processingEnv.elementUtils.getTypeElement(elementType)
                        if (elementTypeElement != null) {
                            // 递归处理 List 元素类型
                            extractDictFieldsRecursively(elementTypeElement, dictFields, "$fullFieldName[]", depth + 1)
                        }
                    }
                }
                // 检测嵌套对象
                else if (isCustomType(fieldTypeString)) {
                    val nestedTypeElement = processingEnv.elementUtils.getTypeElement(fieldTypeString)
                    if (nestedTypeElement != null) {
                        // 递归处理嵌套对象
                        extractDictFieldsRecursively(nestedTypeElement, dictFields, fullFieldName, depth + 1)
                    }
                }
            }
        }
    }
    
    /**
     * 检测是否为 List 类型
     */
    private fun isListType(typeString: String): Boolean {
        return typeString.startsWith("java.util.List<") || 
               typeString.startsWith("List<") ||
               typeString.startsWith("java.util.ArrayList<") ||
               typeString.startsWith("ArrayList<")
    }
    
    /**
     * 提取 List 的元素类型
     */
    private fun extractListElementType(typeString: String): String? {
        val startIndex = typeString.indexOf('<')
        val endIndex = typeString.lastIndexOf('>')
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return typeString.substring(startIndex + 1, endIndex).trim()
        }
        return null
    }
    
    /**
     * 检测是否为自定义类型（非基本类型和常用 Java 类型）
     */
    private fun isCustomType(typeString: String): Boolean {
        val primitiveTypes = setOf(
            "int", "long", "double", "float", "boolean", "byte", "short", "char",
            "java.lang.Integer", "java.lang.Long", "java.lang.Double", "java.lang.Float",
            "java.lang.Boolean", "java.lang.Byte", "java.lang.Short", "java.lang.Character",
            "java.lang.String", "java.util.Date", "java.time.LocalDateTime", "java.time.LocalDate",
            "java.math.BigDecimal", "java.math.BigInteger"
        )
        
        return !primitiveTypes.contains(typeString) && 
               !typeString.startsWith("java.lang.") &&
               !typeString.startsWith("java.util.") &&
               !typeString.startsWith("java.time.") &&
               !typeString.startsWith("java.math.")
    }
    
    private fun parseDictFieldAnnotation(
        fieldElement: VariableElement, 
        annotation: DictField, 
        fullFieldName: String = fieldElement.simpleName.toString()
    ): DictFieldInfo {
        val fieldName = fieldElement.simpleName.toString()
        
        return DictFieldInfo(
            sourceField = fullFieldName,
            targetField = if (annotation.targetField.isNotEmpty()) annotation.targetField else "${fullFieldName.replace(".", "_").replace("[]", "_list")}Text",
            dictCode = annotation.dictCode,
            table = annotation.table,
            codeColumn = annotation.codeColumn,
            nameColumn = annotation.nameColumn,
            spelExp = annotation.spelExp,
            condition = annotation.condition
        )
    }
    
    /**
     * Validates dictionary field configurations
     */
    private fun validateDictFields(dictFields: List<DictFieldInfo>, typeElement: TypeElement) {
        dictFields.forEach { field ->
            // Check that at least one translation type is configured
            val hasSystemDict = field.dictCode.isNotEmpty()
            val hasTableDict = field.table.isNotEmpty()
            val hasSpelExp = field.spelExp.isNotEmpty()
            
            if (!hasSystemDict && !hasTableDict && !hasSpelExp) {
                errorHandler.reportError(
                    ErrorType.VALIDATION_ERROR,
                    "Field ${field.sourceField} has no translation configuration (dictCode, table, or spelExp)",
                    typeElement
                )
            }
            
            // Validate table dictionary configuration
            if (hasTableDict) {
                if (field.codeColumn.isEmpty() || field.nameColumn.isEmpty()) {
                    errorHandler.reportError(
                        ErrorType.VALIDATION_ERROR,
                        "Table dictionary for field ${field.sourceField} must specify both codeColumn and nameColumn",
                        typeElement
                    )
                }
            }
            
            // Check for duplicate target fields
            val duplicateTargets = dictFields.groupBy { it.targetField }
                .filter { it.value.size > 1 }
            
            if (duplicateTargets.isNotEmpty()) {
                duplicateTargets.forEach { (targetField, fields) ->
                    errorHandler.reportError(
                        ErrorType.VALIDATION_ERROR,
                        "Duplicate target field '$targetField' used by fields: ${fields.map { it.sourceField }}",
                        typeElement
                    )
                }
            }
        }
    }
    
    /**
     * Generates a minimal fallback class when normal generation fails
     */
    private fun generateMinimalFallbackClass(className: String, packageName: String): String {
        return """
            package $packageName;
            
            /**
             * Fallback enhanced class for $className
             * Generated due to processing error - provides minimal functionality
             */
            public class ${className}Enhanced extends $className {
                
                public ${className}Enhanced() {
                    super();
                }
                
                /**
                 * Minimal translation method - no-op due to processing error
                 */
                public void translate(site.addzero.apt.dict.service.TransApi transApi) {
                    // No-op implementation due to processing error
                }
                
                /**
                 * Minimal async translation method
                 */
                public java.util.concurrent.CompletableFuture<Void> translateAsync(site.addzero.apt.dict.service.TransApi transApi) {
                    return java.util.concurrent.CompletableFuture.completedFuture(null);
                }
            }
        """.trimIndent()
    }

}

