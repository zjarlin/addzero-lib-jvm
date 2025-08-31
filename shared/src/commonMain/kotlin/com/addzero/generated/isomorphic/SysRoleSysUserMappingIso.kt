@file:OptIn(ExperimentalTime::class)

package com.addzero.generated.isomorphic

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
data class SysRoleSysUserMappingIso(
    val sysRoleId: Long? = null,
    val sysUserId: Long? = null,
    val id: Long? = null,
    val updateBy: SysUserIso? = null,
    val createBy: SysUserIso? = null,
    @Contextual val createTime: LocalDateTime = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()),
    @Contextual val updateTime: LocalDateTime? = null
)
