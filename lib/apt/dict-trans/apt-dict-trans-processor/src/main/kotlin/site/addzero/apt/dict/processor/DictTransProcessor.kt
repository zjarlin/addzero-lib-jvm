package site.addzero.apt.dict.processor

import site.addzero.aop.dicttrans.anno.Dict
import site.addzero.apt.dict.processor.generator.DictConvertorGenerator
import site.addzero.apt.dict.processor.generator.DictDTOGenerator
import site.addzero.apt.dict.processor.model.*
import java.io.IOException
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

/**
 * 字典转换APT处理器
 */
@SupportedAnnotationTypes("site.addzero.aop.dicttrans.anno.Dict")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class DictTransProcessor : AbstractProcessor() {
    
    private lateinit var elementUtils: Elements
    private lateinit var typeUtils: Types
    private lateinit var messager: Messager
    private lateinit var filer: Filer
    
    private lateinit var dtoGenerator: DictDTOGenerator
    private lateinit var convertorGenerator: DictConvertorGenerator
    
    // 缓存已处理的实体信息
    private val processedEntities = mutableMapOf<String, EntityInfo>()
    
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        elementUtils = processingEnv.elementUtils
        typeUtils = processingEnv.typeUtils
        messager = processingEnv.messager
        filer = processingEnv.filer
        
        dtoGenerator = DictDTOGenerator(processingEnv)
        convertorGenerator = DictConvertorGenerator(processingEnv)
    }
    
    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        try {
            messager.printMessage(Diagnostic.Kind.NOTE, "开始处理注解，注解数量: ${annotations.size}")
            
            // 收集所有包含@Dict注解的类
            val annotatedElements = roundEnv.getElementsAnnotatedWith(Dict::class.java)
            messager.printMessage(Diagnostic.Kind.NOTE, "找到 @Dict 注解的元素数量: ${annotatedElements.size}")
            
            val entityClasses = mutableSetOf<TypeElement>()
            
            for (element in annotatedElements) {
                messager.printMessage(Diagnostic.Kind.NOTE, "处理元素: ${element.simpleName}, 类型: ${element.kind}")
                if (element.kind == ElementKind.FIELD) {
                    val enclosingClass = element.enclosingElement as TypeElement
                    entityClasses.add(enclosingClass)
                    messager.printMessage(Diagnostic.Kind.NOTE, "添加实体类: ${enclosingClass.qualifiedName}")
                }
            }
            
            messager.printMessage(Diagnostic.Kind.NOTE, "需要处理的实体类数量: ${entityClasses.size}")
            
            // 处理每个实体类
            for (entityClass in entityClasses) {
                messager.printMessage(Diagnostic.Kind.NOTE, "开始处理实体类: ${entityClass.qualifiedName}")
                processEntity(entityClass)
            }
            
        } catch (e: Exception) {
            messager.printMessage(Diagnostic.Kind.ERROR, "处理注解时发生错误: ${e.message}")
            e.printStackTrace()
        }
        
        return true
    }
    
    private fun processEntity(entityClass: TypeElement) {
        val qualifiedName = entityClass.qualifiedName.toString()
        
        // 避免重复处理
        if (processedEntities.containsKey(qualifiedName)) {
            return
        }
        
        try {
            val entityInfo = analyzeEntity(entityClass)
            processedEntities[qualifiedName] = entityInfo
            
            // 生成DTO类
            generateDTOClass(entityInfo)
            
            // 生成转换器类
            generateConvertorClass(entityInfo)
            
            messager.printMessage(Diagnostic.Kind.NOTE, "成功处理实体: ${entityInfo.qualifiedName}")
            
        } catch (e: Exception) {
            messager.printMessage(Diagnostic.Kind.ERROR, "处理实体 $qualifiedName 时发生错误: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun analyzeEntity(entityClass: TypeElement): EntityInfo {
        val packageName = elementUtils.getPackageOf(entityClass).qualifiedName.toString()
        val simpleName = entityClass.simpleName.toString()
        val qualifiedName = entityClass.qualifiedName.toString()
        
        val allFields = mutableListOf<FieldInfo>()
        val dictFields = mutableListOf<DictFieldInfo>()
        val nestedEntityFields = mutableListOf<NestedEntityField>()
        
        // 分析所有字段
        for (element in entityClass.enclosedElements) {
            if (element.kind == ElementKind.FIELD) {
                val field = element as VariableElement
                val fieldInfo = analyzeField(field)
                allFields.add(fieldInfo)
                
                // 检查是否有@Dict注解
                val dictAnnotations = field.getAnnotationsByType(Dict::class.java)
                if (dictAnnotations.isNotEmpty()) {
                    val dictConfigs = dictAnnotations.map { annotation ->
                        DictConfig(
                            dictCode = annotation.value.takeIf { it.isNotBlank() } ?: annotation.dicCode.takeIf { it.isNotBlank() },
                            tableName = annotation.tab.takeIf { it.isNotBlank() },
                            codeColumn = annotation.codeColumn.takeIf { it.isNotBlank() } ?: "code",
                            nameColumn = annotation.nameColumn.takeIf { it.isNotBlank() } ?: "name",
                            serializationAlias = annotation.serializationAlias.takeIf { it.isNotBlank() }
                        )
                    }
                    
                    dictFields.add(DictFieldInfo(
                        fieldName = field.simpleName.toString(),
                        fieldType = fieldInfo.type,
                        dictConfigs = dictConfigs
                    ))
                }
                
                // 检查是否为嵌套实体
                if (isEntityType(field.asType())) {
                    val nestedField = analyzeNestedField(field)
                    if (nestedField != null) {
                        nestedEntityFields.add(nestedField)
                    }
                }
            }
        }
        
        return EntityInfo(
            typeElement = entityClass,
            packageName = packageName,
            simpleName = simpleName,
            qualifiedName = qualifiedName,
            allFields = allFields,
            dictFields = dictFields,
            nestedEntityFields = nestedEntityFields
        )
    }
    
    private fun analyzeField(field: VariableElement): FieldInfo {
        val fieldName = field.simpleName.toString()
        val fieldType = field.asType()
        
        // 检查是否为集合类型
        if (isCollectionType(fieldType)) {
            val elementType = getCollectionElementType(fieldType)
            return FieldInfo(
                name = fieldName,
                type = fieldType.toString(),
                isCollection = true,
                elementType = elementType?.toString()
            )
        }
        
        return FieldInfo(
            name = fieldName,
            type = fieldType.toString()
        )
    }
    
    private fun analyzeNestedField(field: VariableElement): NestedEntityField? {
        val fieldName = field.simpleName.toString()
        val fieldType = field.asType()
        
        if (isCollectionType(fieldType)) {
            val elementType = getCollectionElementType(fieldType)
            if (elementType != null && isEntityType(elementType)) {
                return NestedEntityField(
                    fieldName = fieldName,
                    fieldType = fieldType.toString(),
                    isCollection = true,
                    elementType = getSimpleTypeName(elementType)
                )
            }
        } else if (isEntityType(fieldType)) {
            return NestedEntityField(
                fieldName = fieldName,
                fieldType = getSimpleTypeName(fieldType),
                isCollection = false
            )
        }
        
        return null
    }
    
    private fun isCollectionType(type: TypeMirror): Boolean {
        if (type is DeclaredType) {
            val typeElement = type.asElement() as? TypeElement ?: return false
            val qualifiedName = typeElement.qualifiedName.toString()
            return qualifiedName.startsWith("java.util.List") ||
                   qualifiedName.startsWith("java.util.Set") ||
                   qualifiedName.startsWith("java.util.Collection")
        }
        return false
    }
    
    private fun getCollectionElementType(type: TypeMirror): TypeMirror? {
        if (type is DeclaredType && type.typeArguments.isNotEmpty()) {
            return type.typeArguments[0]
        }
        return null
    }
    
    private fun isEntityType(type: TypeMirror): Boolean {
        if (type is DeclaredType) {
            val typeElement = type.asElement() as? TypeElement ?: return false
            val qualifiedName = typeElement.qualifiedName.toString()
            
            // 排除基本类型和常用类型
            return !qualifiedName.startsWith("java.") &&
                   !qualifiedName.startsWith("javax.") &&
                   !qualifiedName.startsWith("kotlin.") &&
                   typeElement.kind == ElementKind.CLASS
        }
        return false
    }
    
    private fun getSimpleTypeName(type: TypeMirror): String {
        if (type is DeclaredType) {
            val typeElement = type.asElement() as? TypeElement
            return typeElement?.simpleName?.toString() ?: type.toString()
        }
        return type.toString()
    }
    
    private fun generateDTOClass(entityInfo: EntityInfo) {
        try {
            val dtoCode = dtoGenerator.generateDictDTO(entityInfo)
            val dtoClassName = "${entityInfo.simpleName}DictDTO"
            
            val sourceFile = filer.createSourceFile("${entityInfo.packageName}.$dtoClassName")
            sourceFile.openWriter().use { writer ->
                writer.write(dtoCode)
            }
            
            messager.printMessage(Diagnostic.Kind.NOTE, "生成DTO类: ${entityInfo.packageName}.$dtoClassName")
            
        } catch (e: IOException) {
            messager.printMessage(Diagnostic.Kind.ERROR, "生成DTO类失败: ${e.message}")
        }
    }
    
    private fun generateConvertorClass(entityInfo: EntityInfo) {
        try {
            val convertorCode = convertorGenerator.generateDictConvertor(entityInfo)
            val convertorClassName = "${entityInfo.simpleName}DictConvertor"
            
            val sourceFile = filer.createSourceFile("${entityInfo.packageName}.$convertorClassName")
            sourceFile.openWriter().use { writer ->
                writer.write(convertorCode)
            }
            
            messager.printMessage(Diagnostic.Kind.NOTE, "生成转换器类: ${entityInfo.packageName}.$convertorClassName")
            
        } catch (e: IOException) {
            messager.printMessage(Diagnostic.Kind.ERROR, "生成转换器类失败: ${e.message}")
        }
    }
}