package site.addzero.biz.spec.iot.tdengine

import site.addzero.biz.spec.iot.requireText

/**
 * SQL string plus ordered parameter list.
 */
class SqlStatement(
    sql: String?,
    parameters: List<Any?>,
) {

    val sql: String = requireText(sql, "sql")
    val parameters: List<Any?> = ArrayList(parameters).toList()
}
