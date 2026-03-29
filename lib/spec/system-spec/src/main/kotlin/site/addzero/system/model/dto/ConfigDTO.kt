package site.addzero.system.model.dto

import site.addzero.system.model.common.PageQuery
import java.time.Instant

/**
 * 配置项数据传输对象
 */
data class ConfigDTO(
    val id: String,
    val key: String,
    val value: String,
    val description: String?,
    val category: String,
    val valueType: ValueType,
    val isEncrypted: Boolean,
    val isSystem: Boolean,      // 系统内置配置，不可删除
    val editable: Boolean,      // 是否可编辑
    val sortOrder: Int,
    val createdAt: Instant,
    val updatedAt: Instant
)

enum class ValueType {
    STRING,
    INTEGER,
    BOOLEAN,
    FLOAT,
    JSON,
    YAML,
    PASSWORD   // 加密存储
}

data class ConfigCreateRequest(
    val key: String,
    val value: String,
    val description: String? = null,
    val category: String = "default",
    val valueType: ValueType = ValueType.STRING,
    val isEncrypted: Boolean = false,
    val isSystem: Boolean = false,
    val editable: Boolean = true,
    val sortOrder: Int = 0
)

data class ConfigUpdateRequest(
    val value: String? = null,
    val description: String? = null,
    val editable: Boolean? = null,
    val sortOrder: Int? = null
)

data class ConfigQuery(
    override val pageNum: Int = 1,
    override val pageSize: Int = 10,
    val keyword: String? = null,
    val category: String? = null,
    val valueType: ValueType? = null,
    val isSystem: Boolean? = null
) : PageQuery(pageNum, pageSize)
