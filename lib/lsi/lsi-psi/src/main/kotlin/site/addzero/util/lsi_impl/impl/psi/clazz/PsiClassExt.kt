package site.addzero.util.lsi_impl.impl.psi.clazz

import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import org.apache.commons.lang3.StringUtils.firstNonBlank
import site.addzero.util.lsi.assist.getDefaultAnyValueForType
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.constant.*
import site.addzero.util.lsi.types.PojoAnnotationType
import site.addzero.util.lsi_impl.impl.psi.anno.guessTableName
import site.addzero.util.lsi_impl.impl.psi.field.getDefaultValue
import site.addzero.util.str.cleanDocComment
import site.addzero.util.str.toUnderLineCase

/**
 * 将 PsiClass 转换为 LsiClass
 */
fun PsiClass.toLsiClass(): LsiClass {
    return PsiLsiClass(this)
}


fun PsiClass.toDefaultValueMap(): java.util.LinkedHashMap<Any?, Any?> {
    val outputMap: java.util.LinkedHashMap<Any?, Any?> = LinkedHashMap()
    val psiFields = this.fields
    for (field in psiFields) {
        outputMap[field.name] = field.getDefaultValue()
    }
    return outputMap
}

/**
 * 判断 PsiClass 是否为 POJO/实体类
 * 支持：
 * - JPA/Jimmer Entity（包括 interface 形式的 Jimmer 实体）
 * - Lombok @Data/@Getter/@Setter 注解的类
 * - MyBatis Plus @TableName 注解的类
 */
fun PsiClass.isPojo(): Boolean {
    return site.addzero.util.lsi.assist.checkIsPojo(
        isInterface = isInterface,
        isEnum = isEnum,
        isAbstract = hasModifierProperty(PsiModifier.ABSTRACT),
        isDataClass = false,
        annotationNames = annotations.mapNotNull { it.qualifiedName },
        isShortName = false
    )
}

// ============ 集合类型判断相关 ============


/**
 * 判断 PsiClass 是否为集合类型
 */
fun PsiClass.isCollectionType(): Boolean {
    val qualifiedName = qualifiedName ?: return false

    // 检查是否为集合类型
    return COLLECTION_TYPE_FQ_NAMES.any { qualifiedName.startsWith(it) } ||
            supers.any { superClass ->
                val superQualifiedName = superClass.qualifiedName ?: return@any false
                COLLECTION_TYPE_FQ_NAMES.any { superQualifiedName.startsWith(it) }
            }
}


fun PsiClass.comment(): String? {
    val string = this.docComment?.text ?: ""
    return string
}

/**
 * 根据类名解析 PsiClass
 * 支持从当前类的导入语句中查找
 *
 * @param className 要查找的类名（简单名或全限定名）
 * @return 找到的 PsiClass，未找到返回 null
 */
fun PsiClass.resolveClassByName(className: String): PsiClass? {
    val project = this.project
    val classes = PsiShortNamesCache.getInstance(project)
        .getClassesByName(className, GlobalSearchScope.projectScope(project))

    return when {
        classes.isEmpty() -> null
        classes.size == 1 -> classes[0]
        else -> findClassFromImports(classes)
    }
}

fun PsiClass.findClassFromImports(classes: Array<PsiClass>): PsiClass? {
    val containingFile = this.containingFile as? PsiJavaFile ?: return null
    val importList = containingFile.importList ?: return null
    val importedQualifiedNames = importList.importStatements.mapNotNull { it.qualifiedName }.toSet()

    return classes.firstOrNull { psiClass ->
        val qualifiedName = psiClass.qualifiedName
        qualifiedName != null && importedQualifiedNames.contains(qualifiedName)
    }
}

fun PsiClass.toMap(): Map<String, Any?> {
    val associate = fields.associate {
        val name1 = it.name
        name1 to it.getDefaultValue()
    }
    return associate
}

fun PsiClass.docComment(): String {
    return cleanDocComment(this.docComment?.text)
}

fun PsiClass.importList(): List<String?>? {
    val importList = toPsiJavaFile().importList
    val importStatements = importList?.importStatements
    val map = importStatements?.map { it.qualifiedName }
    return map
}

fun PsiClass.toPsiJavaFile(): PsiJavaFile {
    val targetClassContainingFile = this.containingFile as PsiJavaFile
    return targetClassContainingFile
}

fun PsiClass.packageName(): String {
    val packageName = toPsiJavaFile().packageName
    return packageName
}

/**
 * 推断数据库表名
 * 优先从注解获取，否则使用类名转下划线命名
 */
fun PsiClass.guessTableName(): String? {
    val text = name?.toUnderLineCase()
    // 获取所有注解
    val guessTableNameByAnno = guessTableNameByAnno()

    val firstNonBlank = firstNonBlank(guessTableNameByAnno, text)
    return firstNonBlank
}

/**
 * 从注解中推断表名
 * 支持 MyBatis Plus、Jimmer、JPA 的表名注解
 */
fun PsiClass.guessTableNameByAnno(): String? {
    val annotations = annotations
    val guessTableName = annotations.guessTableName()
    val any = guessTableName ?: (comment())
    return any
}

/**
 * jimmerClass可以这么带出来
 * 如果类实现了带 @Entity 或 @MappedSuperclass 注解的接口，
 * 则也会包含这些接口中的方法
 */
fun PsiClass.getJimmerFields(): List<PsiMethod> {
    val supers = interfaces.filter { interfaceClass ->
        interfaceClass.annotations.any {
            it.qualifiedName in listOf(PojoAnnotationType.JIMMER_ENTITY.fqName, MAPPED_SUPERCLASS)
        }
    }
    return methods.toList() + supers.map { it.getJimmerFields() }.flatten()
}


// ============ JSON/Map 生成相关 ============

private const val MAX_RECURSION_DEPTH = 3

/**
 * 将 PsiClass 转换为 Map 结构，支持嵌套对象和集合类型
 * 使用递归深度限制防止无限递归
 *
 * @param depth 当前递归深度，默认为 0
 * @return 表示类结构的 Map，key 为字段名，value 为示例值
 */
fun PsiClass.generateMap(depth: Int = 0): Map<String, Any?> {
    if (depth > MAX_RECURSION_DEPTH) return emptyMap()

    val project = this.project
    val outputMap = LinkedHashMap<String, Any?>()

    allFields.forEach { field ->
        val fieldType = field.type
        val fieldName = field.name
        if (fieldName != null) {
            outputMap[fieldName] = fieldType.getObjectForType(this, depth + 1)
        }
    }

    return outputMap
}

/**
 * 根据 PsiType 生成对应的示例值
 * 支持基本类型、集合类型、数组类型和自定义类型
 */
private fun PsiType.getObjectForType(
    containingClass: PsiClass,
    depth: Int = 0
): Any? {
    if (depth > MAX_RECURSION_DEPTH) return null

    return when {
        this is PsiArrayType -> handleArrayType(containingClass, depth)
        this is PsiClassType && isCollectionType() -> handleCollectionType(containingClass, depth)
        else -> getPrimitiveOrCustomValue(containingClass, depth)
    }
}

/**
 * 处理数组类型
 */
private fun PsiArrayType.handleArrayType(
    containingClass: PsiClass,
    depth: Int
): List<Any?> {
    if (depth > MAX_RECURSION_DEPTH) return emptyList()

    val sampleValue = componentType.getObjectForType(containingClass, depth + 1)
    return listOf(sampleValue)
}

/**
 * 处理集合类型（List、Set、Collection 等）
 */
private fun PsiClassType.handleCollectionType(
    containingClass: PsiClass,
    depth: Int
): List<Any?> {
    if (depth > MAX_RECURSION_DEPTH) return emptyList()

    val elementType = parameters.firstOrNull()
    val sampleValue = elementType?.getObjectForType(containingClass, depth + 1)
    return listOfNotNull(sampleValue)
}

/**
 * 判断是否为集合类型
 */
private fun PsiClassType.isCollectionType(): Boolean {
    val qualifiedName = resolve()?.qualifiedName ?: return false
    return qualifiedName.startsWith("java.util.") && (
            qualifiedName.contains("List") ||
                    qualifiedName.contains("Set") ||
                    qualifiedName.contains("Collection")
            )
}

/**
 * 获取基本类型或自定义类型的示例值
 * 使用统一的类型默认值函数
 */
private fun PsiType.getPrimitiveOrCustomValue(containingClass: PsiClass, depth: Int): Any? {
    if (depth > MAX_RECURSION_DEPTH) return null

    val presentableText = presentableText

    // 先尝试使用统一的类型默认值函数
    val defaultValue = getDefaultAnyValueForType(presentableText)

    // 如果返回的是类型名本身（表示不是已知的基本类型），则尝试解析为自定义类
    return if (defaultValue == presentableText) {
        // 处理自定义类型 - 使用 resolve() 获取类定义并递归生成
        val targetClass = (this as? PsiClassType)?.resolve()
        targetClass?.generateMap(depth + 1)
            ?: mapOf("type" to presentableText)
    } else {
        defaultValue
    }
}


