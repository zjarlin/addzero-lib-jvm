package site.addzero.jimmer.lowquery.runtime

import org.babyfish.jimmer.sql.kt.ast.query.KMutableRootQuery
import kotlin.reflect.KClass

/**
 * Jimmer 低代码查询运行时提供者。
 */
interface JimmerLowQueryProvider<E : Any> {
    /**
     * 当前提供者关联的实体类型。
     */
    val entityType: KClass<E>

    /**
     * 实体字段到查询参数名的映射。
     */
    val parameterNames: Map<String, String>

    /**
     * 当前提供者是否会写入排序条件。
     */
    val hasOrderBy: Boolean
        get() = false

    /**
     * 将已加载实体字段转换为查询条件，并写入注解声明的排序。
     */
    fun apply(
        query: KMutableRootQuery.ForEntity<E>,
        entity: E,
    )
}
