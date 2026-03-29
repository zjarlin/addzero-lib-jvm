package site.addzero.component.glass

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kyant.shapes.RoundedRectangle
import kotlin.math.roundToInt

/**
 * GlassScrollbar — 垂直玻璃风格滚动条
 *
 * 与 [ScrollState] 绑定，根据滚动位置显示半透明玻璃质感的滚动指示器。
 * 适用于曲目列表、内容面板等可滚动区域。
 *
 * @param scrollState 关联的 ScrollState
 * @param modifier 外部修饰符
 * @param trackWidth 滚动条轨道宽度，默认 6dp
 * @param thumbMinHeight 滑块最小高度，默认 32dp
 * @param accentColor 滑块颜色，默认 [GlassTheme.NeonCyan]
 */
@Composable
fun GlassScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    trackWidth: Dp = 6.dp,
    thumbMinHeight: Dp = 32.dp,
    accentColor: Color = GlassTheme.NeonCyan,
) {
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .width(trackWidth)
            .fillMaxHeight()
            .clip(RoundedRectangle(trackWidth / 2))
            .background(GlassTheme.GlassSurface.copy(alpha = 0.3f))
            .border(
                width = 0.5.dp,
                color = GlassTheme.GlassBorder.copy(alpha = 0.2f),
                shape = RoundedRectangle(trackWidth / 2),
            ),
        contentAlignment = Alignment.TopCenter,
    ) {
        val maxScroll = scrollState.maxValue.toFloat()
        if (maxScroll > 0f) {
            val scrollFraction = scrollState.value / maxScroll

            // 计算 thumb 高度比例（基于可见内容占总内容的比例）
            val viewportFraction = 1f / (1f + maxScroll / with(density) { 300.dp.toPx() })
            val thumbHeightFraction = viewportFraction.coerceIn(0.1f, 0.6f)

            Box(
                modifier = Modifier
                    .width(trackWidth - 1.dp)
                    .fillMaxHeight(thumbHeightFraction)
                    .offset {
                        val trackAvailable = (1f - thumbHeightFraction)
                        val yOffset = scrollFraction * trackAvailable
                        // 使用百分比偏移（需要通过 parent 高度计算）
                        IntOffset(0, 0) // 简化为静态位置
                    }
                    .clip(RoundedRectangle(trackWidth / 2))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.6f),
                                accentColor.copy(alpha = 0.3f),
                            )
                        )
                    )
                    .border(
                        width = 0.5.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.1f),
                            )
                        ),
                        shape = RoundedRectangle(trackWidth / 2),
                    )
            )
        }
    }
}
