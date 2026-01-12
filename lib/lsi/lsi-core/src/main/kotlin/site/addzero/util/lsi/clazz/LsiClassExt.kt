package site.addzero.util.lsi.clazz

import site.addzero.util.lsi.anno.LsiAnnotation
import site.addzero.util.lsi.assist.collectFieldsRecursively
import site.addzero.util.lsi.field.LsiField
import site.addzero.util.lsi.field.hasAnnotation
import site.addzero.util.str.removeAnyQuote
import site.addzero.util.str.toUnderLineCase


/**
 * 递归获取所有字段（包括嵌套字段）
 */
fun LsiClass.getAllFieldsRecursively(maxDepth: Int = 5): List<LsiField> {
    val result = mutableListOf<LsiField>()
    collectFieldsRecursively(fields, result, 0, maxDepth)
    return result
}


/**
 * 获取指定注解的字段
 */
fun LsiClass.getAnnotatedFields(annotationSimpleName: String): List<LsiField> {
    return fields.filter { field ->
        field.annotations.any { it.simpleName == annotationSimpleName }
    }
}

/**
 * 检查是否有指定注解的字段
 */
fun LsiClass.hasAnnotatedFields(annotationName: String): Boolean {
    return fields.any { field ->
        field.annotations.any { it.simpleName == annotationName }
    }
}

fun LsiClass.filterPropertiesByAnnotations(vararg annotationFqNames: String): List<LsiField> {
    val filter = this.fields.filter { it.hasAnnotation(*annotationFqNames) }
    return filter
}

/**
 * 获取类注解的指定attr的值
 * 例如@Entity(arg1=xxx)   =>   getArg("Entity","arg1") 就拿到了xxx
 * @param annotationSimpleName 注解全限定名
 * @param parameterName 参数名称
 * @return 参数值，如果不存在则返回 null
 */
fun LsiClass.getArg(annotationSimpleName: String, parameterName: String = "value"): String? {
    val annotation = annotations.find { it.simpleName == annotationSimpleName } ?: return null
    val toString = annotation.getAttribute(parameterName)?.toString()
    return toString
}

/**
 * 表名注解提取策略
 * @param qualifiedNames 支持的注解全限定名列表
 * @param attributeExtractor 从注解中提取属性值的函数
 */
private data class TableNameStrategy(
    val qualifiedNames: List<String>, val attributeExtractor: (annotation: LsiAnnotation) -> String?
)

/**
 * 表名注解提取策略列表
 */
private val TABLE_NAME_STRATEGIES = listOf(
    // MyBatis Plus 策略：使用 value 属性
    TableNameStrategy(
        qualifiedNames = listOf("com.baomidou.mybatisplus.annotation.TableName"),
        attributeExtractor = { it.getAttribute("value")?.toString() }),
    // Jimmer/JPA 策略：使用 name 属性
    TableNameStrategy(
        qualifiedNames = listOf(
            "org.babyfish.jimmer.sql.Table", "javax.persistence.Table", "jakarta.persistence.Table"
        ), attributeExtractor = { it.getAttribute("name")?.toString() })
)

/**
 * 推断数据库表名
 * 业务逻辑：
 * 1. 优先从注解中获取表名（支持 MyBatis Plus @TableName, Jimmer @Table, JPA @Table）
 * 2. 如果注解中没有，使用类名转下划线命名
 * 3. 移除可能存在的引号
 *
 * @return 推断的表名，如果类名为null则返回空字符串
 */
val LsiClass.guessTableName: String
    get() = run {
        // 1. 尝试从注解中获取表名（使用策略模式）
        val tableNameFromAnno = this.annotations.asSequence().mapNotNull { annotation ->
            TABLE_NAME_STRATEGIES.firstOrNull { strategy ->
                annotation.qualifiedName in strategy.qualifiedNames
            }?.attributeExtractor?.invoke(annotation)
        }.firstOrNull()

        // 2. 如果注解中有表名，使用注解值；否则使用类名转下划线
        val tableName = tableNameFromAnno ?: this.name?.toUnderLineCase()?.lowercase() ?: ""
        // 3. 移除引号并返回
        val removeAnyQuote = tableName.removeAnyQuote()
        return removeAnyQuote
    }


/**
 * 检查类是否具有指定的注解
 * @param annotationNames 注解全限定名数组
 * @return 如果类具有其中任何一个注解，则返回true，否则返回false
 */
fun LsiClass.hasAnnotation(vararg annotationNames: String): Boolean {
    return annotationNames.any { annotationName ->
        annotations.any { annotation ->
            annotation.qualifiedName == annotationName
        }
    }
}

val LsiClass.hasNoArgConstructor: Boolean
    get() = methods.any { method ->
        method.name == "<init>" && (method.parameters.isEmpty() || method.parameters.all { param ->
            param.hasDefault
        })
    }

val LsiClass.packageName: String?
    get() = qualifiedName?.substringBeforeLast('.', "")?.takeIf { it.isNotEmpty() }
