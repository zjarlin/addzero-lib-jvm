@file:OptIn(kotlin.time.ExperimentalTime::class)

package site.addzero.generated.isomorphic

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Contextual

@Serializable
data class SysColumnConfigIso(
    val columnKey: String = "",
    val columnComment: String = "",
    val kmpType: String = "",
    val sortOrder: Long = 0L,
    val showFilter: Boolean = false,
    val showSort: Boolean = false,
    val routeKey: String = "",
    val tableConfig: SysTableConfigIso? = null,
    val id: Long? = null,
    val updateBy: SysUserIso? = null,
    val createBy: SysUserIso? = null,
    @Contextual val createTime: kotlinx.datetime.LocalDateTime = kotlin.time.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()),
    @Contextual val updateTime: kotlinx.datetime.LocalDateTime? = null
)