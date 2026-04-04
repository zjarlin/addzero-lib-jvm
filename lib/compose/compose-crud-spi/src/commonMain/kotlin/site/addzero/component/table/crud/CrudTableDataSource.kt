package site.addzero.component.table.crud

import site.addzero.entity.PageResult

/**
 * 渲染无关的表格数据源 SPI。
 *
 * `query` 是必选能力；删除能力默认关闭，由控制器按需探测。
 */
interface CrudTableDataSource<T, ID : Any> {
    val supportsDelete
        get() = false

    suspend fun query(query: CrudTableQuery): PageResult<T>

    suspend fun deleteByIds(ids: Set<ID>): Int {
        error("当前 CrudTableDataSource 未实现删除能力")
    }
}
