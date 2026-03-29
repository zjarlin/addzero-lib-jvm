package site.addzero.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kyant.shapes.RoundedRectangle
import kotlin.math.roundToInt

/**
 * GlassSlider — 水玻璃风格滑块
 *
 * 自定义绘制的滑块组件，玻璃轨道 + 霓虹渐变已播放段 + 发光滑块。
 * 适用于播放进度条、音量控制等场景。
 *
 * @param value 当前值 (0.0 ~ 1.0)
 * @param onValueChange 值变化回调
 * @param modifier 外部修饰符
 * @param accentColor 强调色（已播放段和滑块发光色），默认 [GlassTheme.NeonCyan]
 * @param trackHeight 轨道高度，默认 4dp
 * @param thumbSize 滑块直径，默认 14dp
 * @param enabled 是否启用
 */
@Composable
fun GlassSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = GlassTheme.NeonCyan,
    trackHeight: Int = 4,
    thumbSize: Int = 14,
    enabled: Boolean = true,
) {
    val density = LocalDensity.current
    var trackWidthPx by remember { mutableStateOf(0f) }
    val thumbSizePx = with(density) { thumbSize.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thumbSize.dp)
            .onSizeChanged { trackWidthPx = it.width.toFloat() }
            .then(
                if (enabled) {
                    Modifier
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val newValue = (offset.x / trackWidthPx).coerceIn(0f, 1f)
                                onValueChange(newValue)
                            }
                        }
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, _ ->
                                change.consume()
                                val newValue =
                                    (change.position.x / trackWidthPx).coerceIn(0f, 1f)
                                onValueChange(newValue)
                            }
                        }
                } else Modifier
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        // 轨道背景
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight.dp)
                .clip(RoundedRectangle(trackHeight.dp / 2))
                .background(GlassTheme.GlassSurface)
                .border(
                    width = 0.5.dp,
                    color = GlassTheme.GlassBorder.copy(alpha = 0.3f),
                    shape = RoundedRectangle(trackHeight.dp / 2),
                )
        )

        // 已播放段（霓虹渐变）
        if (value > 0f && trackWidthPx > 0f) {
            val filledWidthDp = with(density) { (trackWidthPx * value).toDp() }
            Box(
                modifier = Modifier
                    .width(filledWidthDp)
                    .height(trackHeight.dp)
                    .clip(RoundedRectangle(trackHeight.dp / 2))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.5f),
                                accentColor.copy(alpha = 0.9f),
                            )
                        )
                    )
            )
        }

        // 滑块（发光圆点）
        if (trackWidthPx > 0f) {
            val thumbOffsetPx = (trackWidthPx * value - thumbSizePx / 2)
                .coerceIn(0f, trackWidthPx - thumbSizePx)
            Box(
                modifier = Modifier
                    .offset { IntOffset(thumbOffsetPx.roundToInt(), 0) }
                    .size(thumbSize.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White,
                                accentColor.copy(alpha = 0.8f),
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.8f),
                                accentColor.copy(alpha = 0.3f),
                            )
                        ),
                        shape = CircleShape,
                    )
            )
        }
    }
}

/**
 * GlassVerticalSlider — 垂直方向的水玻璃风格滑块
 *
 * 适用于音量控制等垂直场景。
 *
 * @param value 当前值 (0.0 ~ 1.0)，0 在底部，1 在顶部
 * @param onValueChange 值变化回调
 * @param modifier 外部修饰符
 * @param accentColor 强调色
 * @param trackWidth 轨道宽度，默认 4dp
 * @param thumbSize 滑块直径，默认 14dp
 * @param enabled 是否启用
 */
@Composable
fun GlassVerticalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = GlassTheme.NeonCyan,
    trackWidth: Int = 4,
    thumbSize: Int = 14,
    enabled: Boolean = true,
) {
    val density = LocalDensity.current
    var trackHeightPx by remember { mutableStateOf(0f) }
    val thumbSizePx = with(density) { thumbSize.dp.toPx() }

    Box(
        modifier = modifier
            .width(thumbSize.dp)
            .onSizeChanged { trackHeightPx = it.height.toFloat() }
            .then(
                if (enabled) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val newValue = (1f - offset.y / trackHeightPx).coerceIn(0f, 1f)
                            onValueChange(newValue)
                        }
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // 轨道背景
        Box(
            modifier = Modifier
                .width(trackWidth.dp)
                .matchParentSize()
                .clip(RoundedRectangle(trackWidth.dp / 2))
                .background(GlassTheme.GlassSurface)
                .border(
                    width = 0.5.dp,
                    color = GlassTheme.GlassBorder.copy(alpha = 0.3f),
                    shape = RoundedRectangle(trackWidth.dp / 2),
                )
        )

        // 已填充段
        if (value > 0f && trackHeightPx > 0f) {
            val filledHeightDp = with(density) { (trackHeightPx * value).toDp() }
            Box(
                modifier = Modifier
                    .width(trackWidth.dp)
                    .height(filledHeightDp)
                    .clip(RoundedRectangle(trackWidth.dp / 2))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.9f),
                                accentColor.copy(alpha = 0.5f),
                            )
                        )
                    )
            )
        }

        // 滑块
        if (trackHeightPx > 0f) {
            val thumbOffsetPx = -(trackHeightPx * value - thumbSizePx / 2)
                .coerceIn(0f, trackHeightPx - thumbSizePx)
            Box(
                modifier = Modifier
                    .offset { IntOffset(0, thumbOffsetPx.roundToInt()) }
                    .size(thumbSize.dp)
                    .align(Alignment.BottomCenter)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White,
                                accentColor.copy(alpha = 0.8f),
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.8f),
                                accentColor.copy(alpha = 0.3f),
                            )
                        ),
                        shape = CircleShape,
                    )
            )
        }
    }
}
