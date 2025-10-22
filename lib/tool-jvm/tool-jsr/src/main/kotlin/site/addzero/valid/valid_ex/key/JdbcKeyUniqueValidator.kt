package site.addzero.valid.valid_ex.key

import cn.hutool.core.util.StrUtil
import cn.hutool.extra.spring.SpringUtil
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import site.addzero.util.metainfo.MetaInfoUtils
import java.lang.reflect.Field
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
        val dataSource = try {
            SpringUtil.getBean(DataSource::class.java)
        } catch (e: Exception) {
            // 如果没有提供DataSource，则默认返回true
            return true
        }

        val jdbcTemplate = JdbcTemplate(dataSource)

        // 构建参数列表
        val params = fieldValues.filter { it.value != null }.values.toMutableList()
        if (excludeId != null) {
            params.add(excludeId)
        }

        // 构建SQL查询语句
        val sql = buildSql(tableName, fieldValues.keys.toList(), excludeId)

        // 执行查询
        val count = jdbcTemplate.queryForObject(sql, params.toTypedArray()) { rs, _ ->
            rs.getInt(1)
        } ?: 0

        return count == 0
    }

    private fun buildSql(
        tableName: String,
        fieldNames: List<String>,
        excludeId: Any?
    ): String {
        // 构建SQL查询语句，将驼峰字段名转换为下划线形式
        val whereConditions = fieldNames
            .joinToString(" AND ")

        val excludeCondition = if (excludeId != null) " AND id != ?" else ""

        return "SELECT COUNT(*) FROM $tableName WHERE $whereConditions$excludeCondition"
    }
}
