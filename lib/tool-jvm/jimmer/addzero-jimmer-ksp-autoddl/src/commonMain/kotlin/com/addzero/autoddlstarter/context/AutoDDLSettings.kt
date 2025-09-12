package com.addzero.autoddlstarter.context

import com.addzero.autoddlstarter.generator.consts.DbType
import kotlinx.serialization.Serializable


@Serializable
data class Settings(
    val dbType: String = DbType.POSTGRESQL,
    val idType: String = "bigint",
)

/**
 * 全局配置上下文
 */
object AutoDDLSettings {
    lateinit var settings: Settings
}
