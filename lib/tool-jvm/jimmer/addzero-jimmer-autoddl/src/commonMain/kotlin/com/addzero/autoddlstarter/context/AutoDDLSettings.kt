package com.addzero.autoddlstarter.context

import com.addzero.autoddlstarter.generator.consts.DbType
import com.addzero.core.ext.map2bean
import kotlinx.serialization.Serializable


@Serializable
data class Settings(
    val dbType: String = DbType.POSTGRESQL,
    val idType: String = "bigint",
    val id: String = "id",
    val createBy: String = "create_by",
    val updateBy: String = "update_by",
    val createTime: String = "create_time",
    val updateTime: String = "update_time",
)

/**
 * 全局配置上下文
 */
object AutoDDLSettings {
    lateinit var settings: Settings
}
