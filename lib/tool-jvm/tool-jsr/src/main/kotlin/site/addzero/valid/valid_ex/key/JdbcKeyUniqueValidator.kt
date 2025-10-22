package site.addzero.valid.valid_ex.key

import cn.hutool.extra.spring.SpringUtil
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import javax.sql.DataSource

/**
 * 默认的唯一性校验器实现，使用JDBC拼SQL实现
 *
 * @author zjarlin
 * @since 2025/10/22
 */
@Component
class JdbcKeyUniqueValidator : KeyUniqueValidator {


    override fun isUnique(
        tableName: String,
        group: String,
        fieldValues: Map<String, Any?>,
        excludeId: Any?
    ): Boolean {
               val java = DataSource::class.java

        val dataSource = SpringUtil.getBean(java)

        val jdbcTemplate = JdbcTemplate(dataSource)

        if (dataSource == null) {
            // 如果没有提供DataSource，则默认返回true
            return true
        }


        // 构建参数列表
        val params = fieldValues.filter { it.value != null }.values.toMutableList()
        if (excludeId != null) {
            params.add(excludeId)
        }

        // 构建SQL查询语句
        val sql = buildSql(tableName, fieldValues, excludeId)

        // 执行查询
        val count = jdbcTemplate.queryForObject(sql, params.toTypedArray(), Int::class.java)
        return count == 0
    }

    private fun buildSql(
        tableName: String,
        fieldValues: Map<String, Any?>,
        excludeId: Any?
    ): String {
        // 构建SQL查询语句
        val whereConditions = fieldValues.filter { it.value != null }
            .map { "${it.key} = ?" }
            .joinToString(" AND ")

        val excludeCondition = if (excludeId != null) " AND id != ?" else ""

        return "SELECT COUNT(*) FROM $tableName WHERE $whereConditions$excludeCondition"
    }
}
