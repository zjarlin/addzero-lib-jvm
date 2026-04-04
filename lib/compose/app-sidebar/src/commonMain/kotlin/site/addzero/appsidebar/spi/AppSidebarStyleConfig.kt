package site.addzero.appsidebar.spi

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

/**
 * 侧栏样式 SPI。
 *
 * `app-sidebar` 核心只负责结构与交互，这里把所有视觉 token 交给外部 adapter 决定。
 */
interface AppSidebarStyleConfig {
    val contentPadding: PaddingValues
    val emptyVerticalPadding: Dp
    val containerShape: Shape
    val searchShape: Shape
    val itemShape: Shape
    val containerBackground: Color
    val containerBorder: Color
    val containerBrush: Brush
    val searchBackground: Color
    val searchBorder: Color
    val selectedBackgroundBrush: Brush
    val selectedBorder: Color
    val ancestorBackground: Color
    val ancestorBorder: Color
    val textPrimary: Color
    val textMuted: Color
    val textFaint: Color
    val itemStartPadding: Dp
    val itemIndentStep: Dp
    val itemVerticalPadding: Dp
    val itemEndPadding: Dp
}

fun appSidebarStyleConfig(
    contentPadding: PaddingValues,
    emptyVerticalPadding: Dp,
    containerShape: Shape,
    searchShape: Shape,
    itemShape: Shape,
    containerBackground: Color,
    containerBorder: Color,
    containerBrush: Brush,
    searchBackground: Color,
    searchBorder: Color,
    selectedBackgroundBrush: Brush,
    selectedBorder: Color,
    ancestorBackground: Color,
    ancestorBorder: Color,
    textPrimary: Color,
    textMuted: Color,
    textFaint: Color,
    itemStartPadding: Dp,
    itemIndentStep: Dp,
    itemVerticalPadding: Dp,
    itemEndPadding: Dp,
): AppSidebarStyleConfig = DefaultAppSidebarStyleConfig(
    contentPadding = contentPadding,
    emptyVerticalPadding = emptyVerticalPadding,
    containerShape = containerShape,
    searchShape = searchShape,
    itemShape = itemShape,
    containerBackground = containerBackground,
    containerBorder = containerBorder,
    containerBrush = containerBrush,
    searchBackground = searchBackground,
    searchBorder = searchBorder,
    selectedBackgroundBrush = selectedBackgroundBrush,
    selectedBorder = selectedBorder,
    ancestorBackground = ancestorBackground,
    ancestorBorder = ancestorBorder,
    textPrimary = textPrimary,
    textMuted = textMuted,
    textFaint = textFaint,
    itemStartPadding = itemStartPadding,
    itemIndentStep = itemIndentStep,
    itemVerticalPadding = itemVerticalPadding,
    itemEndPadding = itemEndPadding,
)

@Immutable
private data class DefaultAppSidebarStyleConfig(
    override val contentPadding: PaddingValues,
    override val emptyVerticalPadding: Dp,
    override val containerShape: Shape,
    override val searchShape: Shape,
    override val itemShape: Shape,
    override val containerBackground: Color,
    override val containerBorder: Color,
    override val containerBrush: Brush,
    override val searchBackground: Color,
    override val searchBorder: Color,
    override val selectedBackgroundBrush: Brush,
    override val selectedBorder: Color,
    override val ancestorBackground: Color,
    override val ancestorBorder: Color,
    override val textPrimary: Color,
    override val textMuted: Color,
    override val textFaint: Color,
    override val itemStartPadding: Dp,
    override val itemIndentStep: Dp,
    override val itemVerticalPadding: Dp,
    override val itemEndPadding: Dp,
) : AppSidebarStyleConfig
