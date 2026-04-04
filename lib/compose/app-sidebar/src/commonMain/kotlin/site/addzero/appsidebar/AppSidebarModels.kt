package site.addzero.appsidebar

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * 侧栏的基础行为配置。
 */
interface AppSidebarConfig {
    val supportText: String?
        get() = null
    val searchEnabled
        get() = true
    val searchPlaceholder
        get() = "搜索菜单"
}

/**
 * 侧栏对外暴露的事件回调集合。
 */
interface AppSidebarEvents<T> {
    fun onKeywordChange(keyword: String) {}
    fun onItemClick(item: T) {}
}

/**
 * 侧栏可插拔的布局插槽。
 */
interface AppSidebarSlots<T> {
    val header: @Composable ColumnScope.() -> Unit
        get() = {}
    val footer: @Composable ColumnScope.() -> Unit
        get() = {}
    val empty: @Composable ColumnScope.(String) -> Unit
        get() = { keyword ->
            DefaultSidebarEmpty(
                keyword = keyword,
            )
        }
    val leading: (@Composable RowScope.(T, Boolean, Boolean) -> Unit)?
        get() = null
    val label: (@Composable RowScope.(T, Boolean, Boolean) -> Unit)?
        get() = null
    val trailing: (@Composable RowScope.(T, Boolean, Boolean) -> Unit)?
        get() = null
}

/**
 * 创建侧栏配置。
 */
fun appSidebarConfig(
    supportText: String? = null,
    searchEnabled: Boolean = true,
    searchPlaceholder: String = "搜索菜单",
): AppSidebarConfig = DefaultAppSidebarConfig(
    supportText = supportText,
    searchEnabled = searchEnabled,
    searchPlaceholder = searchPlaceholder,
)

/**
 * 创建侧栏事件集合。
 */
fun <T> appSidebarEvents(
    onKeywordChange: (String) -> Unit = {},
    onItemClick: (T) -> Unit = {},
): AppSidebarEvents<T> = DefaultAppSidebarEvents(
    onKeywordChangeHandler = onKeywordChange,
    onItemClickHandler = onItemClick,
)

/**
 * 创建侧栏插槽集合。
 */
fun <T> appSidebarSlots(
    header: @Composable ColumnScope.() -> Unit = {},
    footer: @Composable ColumnScope.() -> Unit = {},
    empty: @Composable ColumnScope.(String) -> Unit = { keyword ->
        DefaultSidebarEmpty(
            keyword = keyword,
        )
    },
    leading: (@Composable RowScope.(T, Boolean, Boolean) -> Unit)? = null,
    label: (@Composable RowScope.(T, Boolean, Boolean) -> Unit)? = null,
    trailing: (@Composable RowScope.(T, Boolean, Boolean) -> Unit)? = null,
): AppSidebarSlots<T> = DefaultAppSidebarSlots(
    header = header,
    footer = footer,
    empty = empty,
    leading = leading,
    label = label,
    trailing = trailing,
)

@Immutable
@Serializable
private data class DefaultAppSidebarConfig(
    override val supportText: String?,
    override val searchEnabled: Boolean,
    override val searchPlaceholder: String,
) : AppSidebarConfig

private class DefaultAppSidebarEvents<T>(
    private val onKeywordChangeHandler: (String) -> Unit,
    private val onItemClickHandler: (T) -> Unit,
) : AppSidebarEvents<T> {
    override fun onKeywordChange(keyword: String) {
        onKeywordChangeHandler(keyword)
    }

    override fun onItemClick(item: T) {
        onItemClickHandler(item)
    }
}

private class DefaultAppSidebarSlots<T>(
    override val header: @Composable ColumnScope.() -> Unit,
    override val footer: @Composable ColumnScope.() -> Unit,
    override val empty: @Composable ColumnScope.(String) -> Unit,
    override val leading: (@Composable RowScope.(T, Boolean, Boolean) -> Unit)?,
    override val label: (@Composable RowScope.(T, Boolean, Boolean) -> Unit)?,
    override val trailing: (@Composable RowScope.(T, Boolean, Boolean) -> Unit)?,
) : AppSidebarSlots<T>
