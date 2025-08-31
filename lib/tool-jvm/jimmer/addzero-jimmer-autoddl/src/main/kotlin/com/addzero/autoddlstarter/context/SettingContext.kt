package com.addzero.autoddlstarter.context

import com.addzero.autoddlstarter.generator.consts.DbType


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
object SettingContext {

    lateinit var settings: Settings

    val context: Settings
        get() = settings

    /**
     * 初始化配置
     * @param op gradle/ksp传入的options
     */
    fun initialize(op: Map<String, String>) {
        settings = Settings(
            dbType = op["dbType"] ?: DbType.POSTGRESQL,
            idType = op["idType"] ?: "bigint",
            id = op["id"] ?: "id",
            createBy = op["createBy"] ?: "create_by",
            updateBy = op["updateBy"] ?: "update_by",
            createTime = op["createTime"] ?: "create_time",
            updateTime = op["updateTime"] ?: "update_time"
        )
    }
}
