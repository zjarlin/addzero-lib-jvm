package com.addzero.jimmer.adv_search

import com.addzero.entity.low_table.StateSearch
import com.addzero.entity.low_table.StateSort
import com.addzero.entity.low_table.StateVo
import org.babyfish.jimmer.Page
import org.babyfish.jimmer.View
import org.babyfish.jimmer.sql.kt.KSqlClient
import kotlin.reflect.KClass


fun <E : Any, V : View<E>> KSqlClient.queryPage(
    sortStats: MutableSet<StateSort>,
    searchConditions: MutableSet<StateSearch>,
    entityClass: KClass<E>,
    view: KClass<V>,
    pageNo: Int = 1,
    pageSize: Int = 10,
): Page<V> {
    val fetchPage = this.createLowQuery(
        sortStats, searchConditions, entityClass, view
    ).fetchPage(pageNo - 1, pageSize)
    return fetchPage
}

fun <E : Any> KSqlClient.queryPage(
    sortStats: MutableSet<StateSort>,
    searchConditions: MutableSet<StateSearch>?,
    entityClass: KClass<E>,
    stateVos: MutableSet<StateVo>,
    pageNo: Int = 1,
    pageSize: Int = 10,
): Page<E> {
    val fetchPage = this.createLowQuery(
        sortStats, searchConditions, entityClass, stateVos
    ).fetchPage(pageNo - 1, pageSize)
    return fetchPage
}
