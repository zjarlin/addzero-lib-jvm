//package com.addzero.autoddlstarter.util
//
//import com.google.devtools.ksp.symbol.KSAnnotation
//import com.google.devtools.ksp.symbol.KSClassDeclaration
//import com.google.devtools.ksp.symbol.KSPropertyDeclaration
//import java.io.File
//
//fun genCode(pathname: String, toJsonStr: String) {
////        val file = FileUtil.file(pathname)
////        FileUtil.writeUtf8String(toJsonStr, file)
//
//
//        val targetFile = File(pathname)
//        targetFile.parentFile?.mkdirs()
////        if (targetFile.exists()) {
////            environment.logger.info("文件已存在，跳过生成: ${targetFile.path}")
////            return
////        }
//        targetFile.writeText(toJsonStr)
//
//
//    }
//
//fun KSPropertyDeclaration.getAnno(annoShortName: String): KSAnnotation? {
//    return this.annotations.find { it.shortName.asString() == annoShortName }
//}
//
//fun KSAnnotation?.getArg(argName: String): Any? {
//    val value = this?.arguments?.firstOrNull { it.name?.asString() == argName }?.value
//    return value
//
//}
//
//
//fun KSPropertyDeclaration.isCustomClassType(): Boolean {
//    val type = this.type.resolve()
//    val declaration = type.declaration
//
//    // 情况1：声明是类（且不是基本类型）
//    if (declaration is KSClassDeclaration) {
//        val qualifiedName = declaration.qualifiedName?.asString()
//
//        // 排除Kotlin/Java的基本类型
//        return qualifiedName !in setOf(
//            "kotlin.String",
//            "kotlin.Int",
//            "kotlin.Long",
//            "kotlin.Boolean",
//            "kotlin.Float",
//            "kotlin.Double",
//            "kotlin.Byte",
//            "kotlin.Short",
//            "kotlin.Char",
//            "java.lang.String",
//            "java.lang.Integer" // 其他基本类型...
//        )
//    }
//
//    // 情况2：其他情况（如泛型、类型参数等）默认视为非基本类型
//    return true
//}
//
//
//fun KSPropertyDeclaration.ktType(): String {
//    val ktType = this.type.resolve().declaration.simpleName.asString()
//    return ktType
//}
//
//
//fun KSAnnotation?.getArgFirstValue(): String? {
//    return this?.arguments?.firstOrNull()?.value?.toString()
//
//}
//
//
//fun KSPropertyDeclaration.isNullable(): Boolean {
//    return this.type.resolve().isMarkedNullable
//}
//
//
//fun KSPropertyDeclaration.isNullableFlag(): String {
//    val nullable = this.isNullable()
//    return if (nullable) {
//        "NULL"
//
//    } else {
//        "NOT NULL"
//
//    }
//}
//
//
//fun KSPropertyDeclaration.ktName(): String {
//    return this.simpleName.asString()
//}
//
//
//fun KSPropertyDeclaration.isCollectionType(): Boolean {
//    val type = this.type.resolve()
//    val declaration = type.declaration
//
//    // 获取类型的全限定名（如 "kotlin.collections.List"）
//    val typeName = declaration.qualifiedName?.asString() ?: return false
//
//    // 检查是否是常见集合类型
//    return typeName in setOf(
//        "kotlin.collections.List",
//        "kotlin.collections.MutableList",
//        "kotlin.collections.Set",
//        "kotlin.collections.MutableSet",
//        "kotlin.collections.Map",
//        "kotlin.collections.MutableMap",
//        "java.util.List",
//        "java.util.ArrayList",
//        "java.util.Set",
//        "java.util.HashSet",
//        "java.util.Map",
//        "java.util.HashMap"
//    )
//}
//
//
//
///**
// * 猜测Jimmer实体的表名
// * 1. 优先读取@Table注解的name属性
// * 2. 没有则尝试从KDoc注释中提取@table标签
// * 3. 没有则用类名转下划线
// */
//fun guessTableName(ktClass: KSClassDeclaration): String {
//    // 1. 优先读取@Table注解
//    val tableAnn = ktClass.annotations.firstOrNull {
//        it.shortName.asString() == "Table" ||
//                it.annotationType.resolve().declaration.qualifiedName?.asString() == "org.babyfish.jimmer.sql.Table"
//    }
//    val tableNameFromAnn = tableAnn
//        ?.arguments
//        ?.firstOrNull { it.name?.asString() == "name" }
//        ?.value as? String
//    if (!tableNameFromAnn.isNullOrEmpty()) {
//        return tableNameFromAnn
//    }
//
//    // 2. 尝试从KDoc注释中提取@table标签
//    val doc = ktClass.docString
//    if (!doc.isNullOrEmpty()) {
//        // 支持 @table 表名 或 @table:表名
//        val regex = Regex("@table[:：]?\\s*([\\w_]+)")
//        val match = regex.find(doc)
//        if (match != null) {
//            return match.groupValues[1]
//        }
//    }
//
//    // 3. 默认用类名转下划线
//    val asString = ktClass.simpleName.asString().toUnderlineCase()
//    return asString
//}
//
//
//
//fun KSPropertyDeclaration.hasAnno(string: String): Boolean {
//    return this.getAnno(string)!=null
//}
