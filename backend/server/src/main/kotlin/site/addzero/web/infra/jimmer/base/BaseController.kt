package site.addzero.web.infra.jimmer.base

import cn.hutool.extra.spring.SpringUtil
import site.addzero.entity.PageResult
import site.addzero.entity.low_table.CommonTableDaTaInputDTO
import site.addzero.jimmer.adv_search.queryPage
import org.babyfish.jimmer.Page
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.web.bind.annotation.*

interface BaseController<E : Any> :BaseGenericContext<E>{
        val type get() = CLASS().kotlin

    //interface Noting
    val idName: String
        get() {
            return "id"
        }

    // 懒加载 sqlClient，确保只初始化一次并缓存结果
    val sql: KSqlClient get() = lazySqlClient
//    val fetcher: Fetcher<T> get() = newFetcher(CLASS_E()).by{
//        allScalarFields()
//    }

//    fun CLASS(): KClass<E> {
//        val typeArgument = TypeUtil.getTypeArgument(this.javaClass, 0)
//        val type = typeArgument as Class<E>
//        return type.kotlin
//    }


    @PostMapping("/page")
    fun page(
        @RequestBody commonTableDaTaInputDTO: CommonTableDaTaInputDTO
    ): PageResult<E> {
        val stateSorts = commonTableDaTaInputDTO.stateSorts
        val stateSearchForms = commonTableDaTaInputDTO.stateSearches
        val pageNo = commonTableDaTaInputDTO.pageNo
        val pageSize = commonTableDaTaInputDTO.pageSize
        val queryPage = sql.queryPage<E>(
            sortStats = stateSorts,
            searchConditions = stateSearchForms,
            entityClass =type,
            stateVos = mutableSetOf(),
            pageNo = pageNo,
            pageSize = pageSize,
        )
        val pageRes = queryPage.toPageResult()
        return pageRes
    }


    @PostMapping("/save")
    fun save(@RequestBody input: E): Int {
        val modifiedEntity = sql.save(input).totalAffectedRowCount
        return modifiedEntity
    }


    @PutMapping("/update")
    fun edit(@RequestBody e: E): Int {
        val update = sql.update(e).totalAffectedRowCount
        return update
    }



    @DeleteMapping("/delete")
    fun deleteByIds(@RequestParam vararg ids: String): Int {
        val affectedRowCountMap = sql.deleteByIds(type, listOf(*ids)).totalAffectedRowCount
        return affectedRowCountMap
    }

    @PostMapping("/saveBatch")
    fun saveBatch(
        @RequestBody input: List<E>,
    ): Int {
        val saveEntities = sql.saveEntities(input)
        return saveEntities.totalAffectedRowCount
    }


    @GetMapping("/findById")
    fun findById(id: String): E? {
        val byId = sql.findById(type, id)
        return byId
    }

    @GetMapping("/loadTableConfig")
    fun loadTableConfig()  {

    }



    companion object {
        private val lazySqlClient: KSqlClient by lazy {
            SpringUtil.getBean(KSqlClient::class.java)
        }
    }
}

private fun <E> Page<E>.toPageResult(): PageResult<E> {
    return PageResult(this.rows, this.totalRowCount, this.totalPageCount.toInt())
}
