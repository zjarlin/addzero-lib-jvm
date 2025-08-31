@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.addzero.generated.isomorphic

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Contextual

@Serializable
data class JdbcColumnMetadataIso(
    val columnName: String = "",
    val jdbcType: Long = 0,
    val columnType: String = "",
    val columnLength: Long = 0,
    val nullableBoolean: Boolean = false,
    val nullableFlag: String = "",
    val remarks: String? = null,
    val defaultValue: String? = null,
    val primaryKeyFlag: String = "",
    val table: JdbcTableMetadataIso? = null,
    val jdbcColumnMetadataAttach: JdbcColumnMetadataAttachIso? = null,
    val id: Long? = null,
    val updateBy: SysUserIso? = null,
    val createBy: SysUserIso? = null,
    @Contextual val createTime: kotlinx.datetime.LocalDateTime = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    @Contextual val updateTime: kotlinx.datetime.LocalDateTime? = null
)