package site.addzero.appsidebar.spi

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 工作台骨架的稳定布局参数。
 */
interface ScaffoldConfigSpi {
    val contentHeaderScrollable
        get() = true
    val defaultSidebarRatio
        get() = 0.22f
    val minSidebarWidth
        get() = 248.dp
    val maxSidebarWidth
        get() = 360.dp
    val detailWidth
        get() = 320.dp
    val outerPadding
        get() = PaddingValues(0.dp)
    val contentPadding
        get() = PaddingValues(0.dp)
    val detailPadding
        get() = PaddingValues(0.dp)
}

/**
 * 创建一个工作台骨架配置实例。
 */
fun scaffoldConfig(
    contentHeaderScrollable: Boolean = true,
    defaultSidebarRatio: Float = 0.22f,
    minSidebarWidth: Dp = 248.dp,
    maxSidebarWidth: Dp = 360.dp,
    detailWidth: Dp = 320.dp,
    outerPadding: PaddingValues = PaddingValues(0.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    detailPadding: PaddingValues = PaddingValues(0.dp),
): ScaffoldConfigSpi = DefaultScaffoldConfigSpi(
    contentHeaderScrollable = contentHeaderScrollable,
    defaultSidebarRatio = defaultSidebarRatio,
    minSidebarWidth = minSidebarWidth,
    maxSidebarWidth = maxSidebarWidth,
    detailWidth = detailWidth,
    outerPadding = outerPadding,
    contentPadding = contentPadding,
    detailPadding = detailPadding,
)

@Immutable
private data class DefaultScaffoldConfigSpi(
    override val contentHeaderScrollable: Boolean,
    override val defaultSidebarRatio: Float,
    override val minSidebarWidth: Dp,
    override val maxSidebarWidth: Dp,
    override val detailWidth: Dp,
    override val outerPadding: PaddingValues,
    override val contentPadding: PaddingValues,
    override val detailPadding: PaddingValues,
) : ScaffoldConfigSpi
