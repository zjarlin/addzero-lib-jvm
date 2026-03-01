package site.addzero.lsi.psi.field

import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import site.addzero.lsi.assist.getDefaultAnyValueForType
import site.addzero.lsi.psi.anno.getArg
import site.addzero.lsi.psi.anno.guessFieldCommentOrNull
import site.addzero.lsi.psi.clazz.toDefaultValueMap
import site.addzero.lsi.psi.type.handleListDefaultValue
import site.addzero.lsi.psi.type.isCollectionType
import site.addzero.lsi.psi.type.isListType
import site.addzero.lsi.psi.type.toPsiClass
import site.addzero.util.str.cleanDocComment

fun PsiField.isStaticField(): Boolean {
    return hasModifierProperty(PsiModifier.STATIC)
}

fun PsiField.getColumnName(): String? {
    // 首先检查是否有关联注解
    val hasAssociation = this.annotations.any { annotation ->
        val fqName = annotation.qualifiedName
        fqName in setOf(
            "javax.persistence.OneToOne",
            "jakarta.persistence.OneToOne",
            "javax.persistence.ManyToOne",
            "jakarta.persistence.ManyToOne",
            "org.babyfish.jimmer.sql.OneToOne",
            "org.babyfish.jimmer.sql.ManyToOne"
        )
    }

    // 如果有关联注解，则检查 JoinColumn
    if (hasAssociation) {
        val joinColumnAnnotation = this.annotations.find { annotation ->
            val fqName = annotation.qualifiedName
            fqName in setOf(
                "javax.persistence.JoinColumn",
                "jakarta.persistence.JoinColumn"
            )
        }

        if (joinColumnAnnotation != null) {
            val columnName = joinColumnAnnotation.getArg("name")
            if (columnName != null) {
                return columnName
            }
        }
    }

    // 检查 Jimmer @Column 注解
    val jimmerColumnAnnotation = this.annotations.find { annotation ->
        annotation.qualifiedName == "org.babyfish.jimmer.meta.annotation.Column"
    }

    if (jimmerColumnAnnotation != null) {
        val columnName = jimmerColumnAnnotation.getArg("name")
        if (columnName != null) {
            return columnName
        }
    }

    // 检查 MyBatis Plus @TableField 注解
    val tableFieldAnnotation = this.annotations.find { annotation ->
        annotation.qualifiedName == "com.baomidou.mybatisplus.annotation.TableField"
    }

    if (tableFieldAnnotation != null) {
        val columnName = tableFieldAnnotation.getArg("value")
        if (columnName != null) {
            return columnName
        }
    }

    // 兜底策略：将字段名转换为下划线命名格式
    val toSnakeCaseLowerCaseInline = this.name?.toSnakeCaseLowerCaseInline()
    return toSnakeCaseLowerCaseInline
}

fun PsiField.getComment(): String? {
    // 首先尝试从注解中获取描述
    val annotations1 = this.annotations
    val guessFieldComment = annotations1.guessFieldCommentOrNull()
    return guessFieldComment ?: cleanDocComment(this.docComment?.text)
}

fun PsiField.isCollectionType(): Boolean {
    return this.type.isCollectionType()
}

fun PsiField.isConstantField(): Boolean {
    return hasModifierProperty(PsiModifier.FINAL) && hasModifierProperty(PsiModifier.STATIC)
}

/**
 * 获取字段的默认值
 * 支持：基本类型、包装类型、集合类型、自定义类型

 * @return 字段类型对应的默认值
 */
fun PsiField.getDefaultValue(): Any {
    val project = this.containingFile.project
    val type = this.type
    // 处理基本类型
    if (type is PsiPrimitiveType) {
        return getDefaultAnyValueForType(type.name)
    }
    // 对于引用类型，先尝试用全限定名获取默认值
    val canonicalText = type.canonicalText
    val defaultValue = getDefaultAnyValueForType(canonicalText)

    // 如果不是类型名本身（即找到了已知类型的默认值），直接返回
    if (defaultValue != canonicalText) {
        return defaultValue
    }
    // 对于未知的复杂类型，进一步处理
    // 处理集合类型（List、Set、Collection 等）
    if (type.isListType()) {
        return type.handleListDefaultValue(this.containingClass!!)
    }
    // 处理自定义类型 - 尝试解析类并生成其默认值 Map
    val resolvedClass = type.toPsiClass()
    val toDefaultValueMap = resolvedClass?.toDefaultValueMap()
    return toDefaultValueMap ?: canonicalText
}

fun PsiField.addComment() {
    val project = this.containingFile.project
    // 创建新的文档注释
    val factory = PsiElementFactory.getInstance(project)
    val newDocComment = factory.createDocCommentFromText("/** */")
    addBefore(newDocComment, this.firstChild)
}

// ============ 注解操作相关 ============

/**
 * 添加注解到字段
 * 从文档注释中提取描述并格式化为注解
 *
 * @param annotationTemplate 注解模板，如 "@Schema(description = \"{}\")"
 * @param description 描述文本（可选），如果不提供则从字段的文档注释中提取
 */
fun PsiField.addAnnotation(annotationTemplate: String, description: String? = null) {
    val project = this.containingFile.project

    // 获取描述文本
    val desc = description ?: getComment() ?: return
    val cleanedDesc = cleanDocComment(desc)

    if (cleanedDesc.isBlank()) return

    // 格式化注解模板
    val annotationText = annotationTemplate.replace("{}", cleanedDesc)

    // 创建并添加注解
    try {
        val factory = JavaPsiFacade.getElementFactory(project)
        val annotation = factory.createAnnotationFromText(annotationText, this)

        // 将注解添加到字段上方
        modifierList?.addBefore(annotation, modifierList?.firstChild)

        // 格式化代码
        CodeStyleManager.getInstance(project).reformat(this)
    } catch (e: Exception) {
        // 忽略创建失败的情况（可能是格式问题）
    }
}

/**
 * 检查字段是否包含指定简单名称的注解
 *
 * @param shortNames 注解简单名称列表，如 ["Schema", "ApiModelProperty"]
 * @return 如果包含任一注解返回 true，否则返回 false
 */
fun PsiField.hasAnnotationByShortName(vararg shortNames: String): Boolean {
    return annotations.any { annotation ->
        val shortName = annotation.qualifiedName?.substringAfterLast('.') ?: return@any false
        shortName in shortNames
    }
}

/**
 * 检查字段是否包含指定全限定名的注解
 *
 * @param qualifiedNames 注解全限定名列表
 * @return 如果包含任一注解返回 true，否则返回 false
 */
fun PsiField.hasAnnotationByQualifiedName(vararg qualifiedNames: String): Boolean {
    return annotations.any { annotation ->
        annotation.qualifiedName in qualifiedNames
    }
}

/**
 * 获取字段上指定简单名称的所有注解
 *
 * @param shortNames 注解简单名称列表
 * @return 匹配的注解列表
 */
fun PsiField.getAnnotationsByShortName(vararg shortNames: String): List<PsiAnnotation> {
    return annotations.filter { annotation ->
        val shortName = annotation.qualifiedName?.substringAfterLast('.') ?: return@filter false
        shortName in shortNames
    }
}

/**
 * 内联的下划线命名转换函数
 */
@Deprecated(message = "", replaceWith = ReplaceWith("site.addzero.toUnderlineLowerCase(*)"))
fun String.toSnakeCaseLowerCaseInline(): String {
    if (this.isBlank()) return this
    return this.mapIndexed { index, c ->
        when {
            c.isUpperCase() && index > 0 -> "_${c.lowercase()}"
            else -> c.lowercase()
        }
    }.joinToString("")
}

