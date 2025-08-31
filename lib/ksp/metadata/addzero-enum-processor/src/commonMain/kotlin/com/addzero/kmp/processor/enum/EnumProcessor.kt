
package com.addzero.kmp.processor.enum

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * 枚举处理器
 * 用于收集项目中所有的枚举类信息
 */
class EnumProcessor(
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {

    private val enumClassNames = mutableSetOf<KSName>()
    private var resolver = null as Resolver?

    override fun process(resolver: Resolver): List<KSAnnotated> {
        this.resolver = resolver

        // 获取所有枚举类
        val newEnumClasses = resolver.getAllFiles()
            .flatMap { it.declarations }
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.ENUM_CLASS }
            .filter { it.validate() }
            .filterNot { enumClassNames.contains(it.qualifiedName) }
            .mapNotNull { it.qualifiedName }

        enumClassNames.addAll(newEnumClasses)

        return emptyList()
    }

    override fun finish() {
        if (enumClassNames.isNotEmpty() && resolver != null) {
            // 通过名称重新解析枚举类，避免生命周期问题
            val enumClasses = enumClassNames.mapNotNull { name ->
                resolver?.getClassDeclarationByName(name)
            }.filter { it.classKind == ClassKind.ENUM_CLASS }

            if (enumClasses.isNotEmpty()) {
                generateEnumInfoClass(enumClasses)
            }
        }
    }

    /**
     * 生成枚举信息类
     */
    private fun generateEnumInfoClass(enumClasses: List<KSClassDeclaration>) {
        // 创建 EnumInfo 类
        val enumInfoClass = TypeSpec.classBuilder("EnumInfo")
            .addModifiers(KModifier.DATA)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("name", String::class)
                    .addParameter("comment", String::class.asTypeName().copy(nullable = true))
                    .addParameter("entries", LIST.parameterizedBy(ENUM_ENTRY_INFO))
                    .build()
            )
            .addProperty(
                PropertySpec.builder("name", String::class)
                    .initializer("name")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("comment", String::class.asTypeName().copy(nullable = true))
                    .initializer("comment")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("entries", LIST.parameterizedBy(ENUM_ENTRY_INFO))
                    .initializer("entries")
                    .build()
            )
            .build()

        // 创建 EnumEntryInfo 类
        val enumEntryInfoClass = TypeSpec.classBuilder("EnumEntryInfo")
            .addModifiers(KModifier.DATA)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("name", String::class)
                    .addParameter("comment", String::class.asTypeName().copy(nullable = true))
                    .build()
            )
            .addProperty(
                PropertySpec.builder("name", String::class)
                    .initializer("name")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("comment", String::class.asTypeName().copy(nullable = true))
                    .initializer("comment")
                    .build()
            )
            .build()

        // 创建 EnumRegistry 类
        val enumRegistryClass = TypeSpec.objectBuilder("EnumRegistry")
            .addProperty(
                PropertySpec.builder("allEnums", LIST.parameterizedBy(ENUM_INFO))
                    .initializer(buildEnumListInitializer(enumClasses))
                    .build()
            )
            .addFunction(
                FunSpec.builder("findEnumByName")
                    .addParameter("name", String::class)
                    .returns(ENUM_INFO.copy(nullable = true))
                    .addCode(
                        """
                        return allEnums.find { it.name == name }
                    """.trimIndent()
                    )
                    .build()
            )
            .build()

        // 创建文件
        val fileSpec = FileSpec.builder("com.addzero.kmp.generated.enum", "EnumRegistry")
            .addType(enumEntryInfoClass)
            .addType(enumInfoClass)
            .addType(enumRegistryClass)
            .build()

        // 写入文件
        fileSpec.writeTo(codeGenerator, Dependencies(true))
    }

    /**
     * 构建枚举列表初始化代码
     */
    private fun buildEnumListInitializer(enumClasses: List<KSClassDeclaration>): CodeBlock {
        val codeBlock = CodeBlock.builder()
            .add("listOf(\n")
            .indent()

        enumClasses.forEachIndexed { index, enumClass ->
            val className = enumClass.toClassName().canonicalName
            val comment = enumClass.docString?.trim()

            codeBlock.add("EnumInfo(\n")
                .indent()
                .add("%S,\n", className)
                .add("%S,\n", comment)
                .add("listOf(\n")
                .indent()

            // 获取枚举项
            val enumEntries = enumClass.declarations
                .filterIsInstance<KSClassDeclaration>()
                .filter { it.classKind == ClassKind.ENUM_ENTRY }
                .toList()

            enumEntries.forEachIndexed { entryIndex, entry ->
                val entryName = entry.simpleName.asString()
                val entryComment = entry.docString?.trim()

                codeBlock.add("EnumEntryInfo(%S, %S)", entryName, entryComment)
                if (entryIndex < enumEntries.size - 1) {
                    codeBlock.add(",\n")
                } else {
                    codeBlock.add("\n")
                }
            }

            codeBlock.unindent()
                .add(")\n")
                .unindent()
                .add(")")

            if (index < enumClasses.size - 1) {
                codeBlock.add(",\n")
            } else {
                codeBlock.add("\n")
            }
        }

        codeBlock.unindent()
            .add(")")

        return codeBlock.build()
    }

    companion object {
        private val ENUM_ENTRY_INFO = ClassName("com.addzero.kmp.generated.enum", "EnumEntryInfo")
        private val ENUM_INFO = ClassName("com.addzero.kmp.generated.enum", "EnumInfo")
        private val LIST = ClassName("kotlin.collections", "List")
    }
}

/**
 * 枚举处理器提供者
 */
class EnumProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return EnumProcessor(
            environment.codeGenerator
        )
    }
}
