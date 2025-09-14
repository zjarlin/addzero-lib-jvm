@file:OptIn(kotlin.time.ExperimentalTime::class)

package site.addzero.generated.isomorphic

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Contextual

@Serializable
data class SysTableConfigIso(
    val routeKey: String = "",
    val showPagination: Boolean = false,
    val showSearchBar: Boolean = false,
    val showBatchActions: Boolean = false,
    val showRowSelection: Boolean = false,
    val showDefaultRowActions: Boolean = false,
    val enableSorting: Boolean = false,
    val enableAdvancedSearch: Boolean = false,
    val headerHeightDp: Float = 0f,
    val rowHeightDp: Float = 0f,
    val columns: List<SysColumnConfigIso> = emptyList(),
    val id: Long? = null,
    val updateBy: SysUserIso? = null,
    val createBy: SysUserIso? = null,
    @Contextual val createTime: kotlinx.datetime.LocalDateTime = kotlin.time.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()),
    @Contextual val updateTime: kotlinx.datetime.LocalDateTime? = null
)