package site.addzero.apt.dict.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import site.addzero.apt.dict.annotations.DictTranslate
import site.addzero.apt.dict.annotations.DictField
import site.addzero.apt.dict.annotations.DictConfig

class DictTranslateProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(DictTranslate::class.qualifiedName!!)
        val ret = symbols.filter { !it.validate() }.toList()
        
        symbols
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach { it ->
                val classDeclaration = it as KSClassDeclaration
                processClass(classDeclaration)
            }
        
        return ret
    }
    
    private fun processClass(classDeclaration: KSClassDeclaration) {
        val className = classDeclaration.simpleName.asString()
        val packageName = classDeclaration.packageName.asString()
        
        // 获取 @DictTranslate 注解
        val dictTranslateAnnotation = classDeclaration.annotations
            .find { it.shortName.asString() == "DictTranslate" }
        
        val suffix = dictTranslateAnnotation?.arguments
            ?.find { it.name?.asString() == "suffix" }?.value as? String ?: "Enhanced"
            
        val generateExtensions = dictTranslateAnnotation?.arguments
            ?.find { it.name?.asString() == "generateExtensions" }?.value as? Boolean ?: true
            
        val generateBuilder = dictTranslateAnnotation?.arguments
            ?.find { it.name?.asString() == "generateBuilder" }?.value as? Boolean ?: false
        
        // 获取字典字段
        val dictFields = extractDictFields(classDeclaration)
        
        if (dictFields.isEmpty()) {
            logger.warn("No @DictField annotations found in class $className")
            return
        }
        
        // 生成增强类
        generateEnhancedClass(
            packageName, 
            className, 
            suffix, 
            classDeclaration, 
            dictFields,
            generateExtensions,
            generateBuilder
        )
    }
    
    private fun extractDictFields(classDeclaration: KSClassDeclaration): List<DictFieldInfo> {
        val dictFields = mutableListOf<DictFieldInfo>()
        
        // 获取所有属性
        classDeclaration.getAllProperties().forEach { property ->
            property.annotations
                .filter { it.shortName.asString() == "DictField" }
                .forEach { annotation ->
                    val dictFieldInfo = parseDictFieldAnnotation(property, annotation)
                    dictFields.add(dictFieldInfo)
                }
        }
        
        return dictFields
    }
    
    private fun parseDictFieldAnnotation(property: KSPropertyDeclaration, annotation: KSAnnotation): DictFieldInfo {
        val args = annotation.arguments.associate { it.name?.asString() to it.value }
        
        return DictFieldInfo(
            sourceField = property.simpleName.asString(),
            sourceType = property.type.resolve().toClassName(),
            dictCode = args["dictCode"] as? String ?: "",
            table = args["table"] as? String ?: "",
            codeColumn = args["codeColumn"] as? String ?: "",
            nameColumn = args["nameColumn"] as? String ?: "",
            targetField = args["targetField"] as? String ?: "${property.simpleName.asString()}Name",
            spelExp = args["spelExp"] as? String ?: "",
            ignoreNull = args["ignoreNull"] as? Boolean ?: true,
            defaultValue = args["defaultValue"] as? String ?: "",
            cached = args["cached"] as? Boolean ?: true
        )
    }
    
    private fun generateEnhancedClass(
        packageName: String,
        originalClassName: String,
        suffix: String,
        originalClass: KSClassDeclaration,
        dictFields: List<DictFieldInfo>,
        generateExtensions: Boolean,
        generateBuilder: Boolean
    ) {
        val enhancedClassName = "${originalClassName}$suffix"
        
        // 创建增强类
        val enhancedClassBuilder = TypeSpec.classBuilder(enhancedClassName)
            .addModifiers(KModifier.DATA)
        
        // 添加原始类的所有属性
        val constructorBuilder = FunSpec.constructorBuilder()
        originalClass.getAllProperties().forEach { property ->
            val propertyName = property.simpleName.asString()
            val propertyType = property.type.resolve().toClassName()
            
            enhancedClassBuilder.addProperty(
                PropertySpec.builder(propertyName, propertyType)
                    .initializer(propertyName)
                    .build()
            )
            
            constructorBuilder.addParameter(propertyName, propertyType)
        }
        
        // 添加字典翻译后的属性
        dictFields.forEach { dictField ->
            enhancedClassBuilder.addProperty(
                PropertySpec.builder(dictField.targetField, String::class.asTypeName().copy(nullable = true))
                    .initializer("null")
                    .mutable(true)
                    .build()
            )
        }
        
        enhancedClassBuilder.primaryConstructor(constructorBuilder.build())
        
        // 添加翻译方法
        enhancedClassBuilder.addFunction(generateTranslateMethod(dictFields))
        
        // 如果启用扩展函数，生成扩展函数
        val fileBuilder = FileSpec.builder(packageName, enhancedClassName)
            .addType(enhancedClassBuilder.build())
        
        if (generateExtensions) {
            fileBuilder.addFunction(generateExtensionFunction(packageName, originalClassName, enhancedClassName, dictFields))
        }
        
        if (generateBuilder) {
            fileBuilder.addType(generateBuilderClass(enhancedClassName, originalClass, dictFields))
        }
        
        // 写入文件
        fileBuilder.build().writeTo(codeGenerator, Dependencies(true, originalClass.containingFile!!))
    }
    
    private fun generateTranslateMethod(dictFields: List<DictFieldInfo>): FunSpec {
        val methodBuilder = FunSpec.builder("translate")
            .addParameter("dictService", ClassName("site.addzero.apt.dict.service", "DictService"))
            .returns(UNIT)
        
        dictFields.forEach { dictField ->
            if (dictField.dictCode.isNotEmpty()) {
                // 系统字典翻译
                methodBuilder.addStatement(
                    "this.%L = dictService.translateByDictCode(%S, this.%L?.toString())",
                    dictField.targetField,
                    dictField.dictCode,
                    dictField.sourceField
                )
            } else if (dictField.table.isNotEmpty()) {
                // 自定义表翻译
                methodBuilder.addStatement(
                    "this.%L = dictService.translateByTable(%S, %S, %S, this.%L)",
                    dictField.targetField,
                    dictField.table,
                    dictField.codeColumn,
                    dictField.nameColumn,
                    dictField.sourceField
                )
            }
        }
        
        return methodBuilder.build()
    }
    
    private fun generateExtensionFunction(
        packageName: String,
        originalClassName: String,
        enhancedClassName: String,
        dictFields: List<DictFieldInfo>
    ): FunSpec {
        return FunSpec.builder("toEnhanced")
            .receiver(ClassName(packageName, originalClassName))
            .returns(ClassName(packageName, enhancedClassName))
            .addStatement("return %T(${originalClassName.lowercase()})", ClassName(packageName, enhancedClassName))
            .build()
    }
    
    private fun generateBuilderClass(
        enhancedClassName: String,
        originalClass: KSClassDeclaration,
        dictFields: List<DictFieldInfo>
    ): TypeSpec {
        // Builder 模式实现
        return TypeSpec.classBuilder("${enhancedClassName}Builder")
            .addModifiers(KModifier.DATA)
            .build()
    }
}

data class DictFieldInfo(
    val sourceField: String,
    val sourceType: ClassName,
    val dictCode: String,
    val table: String,
    val codeColumn: String,
    val nameColumn: String,
    val targetField: String,
    val spelExp: String,
    val ignoreNull: Boolean,
    val defaultValue: String,
    val cached: Boolean
)