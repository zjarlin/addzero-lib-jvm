package site.addzero.liquidglass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class LiquidGlassTabsColors(
    val selectedContentColor: Color,
    val unselectedContentColor: Color,
    val disabledContentColor: Color,
)

object LiquidGlassTabsDefaults {
    val trackSpec
        @Composable
        get() = LiquidGlassDefaults.card.copy(
            frost = 16.dp,
            refraction = 0.58f,
            curve = 0.18f,
            edge = 0.003f,
            shape = RoundedCornerShape(22.dp),
            tint = Color.White.copy(alpha = 0.018f),
            surfaceColor = Color.White.copy(alpha = 0.10f),
            borderColor = Color.White.copy(alpha = 0.08f),
        )

    val thumbSpec
        @Composable
        get() = LiquidGlassDefaults.primaryButton.copy(
            frost = 18.dp,
            refraction = 0.70f,
            curve = 0.28f,
            edge = 0.010f,
            shape = RoundedCornerShape(18.dp),
            tint = Color(0xFF8ED7FF).copy(alpha = 0.062f),
            surfaceColor = Color.White.copy(alpha = 0.15f),
            borderColor = Color.White.copy(alpha = 0.14f),
        )

    val colors
        @Composable
        get() = LiquidGlassTabsColors(
            selectedContentColor = MaterialTheme.colorScheme.primary,
            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
        )

    val labelTextStyle
        @Composable
        get() = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.SemiBold,
        )
}

@Composable
fun <T> LiquidGlassTabs(
    items: List<T>,
    selectedItem: T,
    onSelectedItemChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    trackSpec: LiquidGlassSpec = LiquidGlassTabsDefaults.trackSpec,
    thumbSpec: LiquidGlassSpec = LiquidGlassTabsDefaults.thumbSpec,
    tabHeight: Dp = 56.dp,
    enabled: (T) -> Boolean = { true },
    label: @Composable BoxScope.(item: T, selected: Boolean) -> Unit,
) {
    if (items.isEmpty()) {
        return
    }

    val selectedIndex = items.indexOf(selectedItem).takeIf { it >= 0 } ?: 0

    BoxWithConstraints(
        modifier = modifier.liquidGlassTabsTrack(
            trackSpec = trackSpec,
            tabHeight = tabHeight,
        ),
    ) {
        val tabWidth = maxWidth / items.size
        val thumbOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = spring(
                dampingRatio = 0.82f,
                stiffness = 600f,
            ),
            label = "liquidGlassTabsThumbOffset",
        )

        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .liquidGlassTabsThumb(
                        thumbSpec = thumbSpec,
                        tabWidth = tabWidth,
                        thumbOffset = thumbOffset,
                    ),
            )

            Row(
                modifier = Modifier.fillMaxSize(),
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .liquidGlassTabsItem(
                                enabled = enabled(item),
                                onClick = {
                                    onSelectedItemChange(item)
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        label(item, isSelected)
                    }
                }
            }
        }
    }
}

@Composable
fun LiquidGlassTabs(
    labels: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    trackSpec: LiquidGlassSpec = LiquidGlassTabsDefaults.trackSpec,
    thumbSpec: LiquidGlassSpec = LiquidGlassTabsDefaults.thumbSpec,
    tabHeight: Dp = 56.dp,
    enabled: (Int) -> Boolean = { true },
    colors: LiquidGlassTabsColors = LiquidGlassTabsDefaults.colors,
    textStyle: TextStyle = LiquidGlassTabsDefaults.labelTextStyle,
) {
    if (labels.isEmpty()) {
        return
    }

    LiquidGlassTabs(
        items = labels.indices.toList(),
        selectedItem = selectedIndex.coerceIn(labels.indices),
        onSelectedItemChange = onSelectedIndexChange,
        modifier = modifier,
        trackSpec = trackSpec,
        thumbSpec = thumbSpec,
        tabHeight = tabHeight,
        enabled = enabled,
    ) { index, selected ->
        val textColor by animateColorAsState(
            targetValue = when {
                !enabled(index) -> colors.disabledContentColor
                selected -> colors.selectedContentColor
                else -> colors.unselectedContentColor
            },
            label = "liquidGlassTabsTextColor",
        )
        Text(
            text = labels[index],
            color = textColor,
            style = textStyle,
            maxLines = 1,
        )
    }
}

/** 轨道层：给顶部切换一个轻量玻璃底板，而不是整块白条。 */
private fun Modifier.liquidGlassTabsTrack(
    trackSpec: LiquidGlassSpec,
    tabHeight: Dp,
): Modifier {
    return fillMaxWidth()
        .height(tabHeight)
        .liquidGlassSurface(trackSpec)
        .padding(4.dp)
}

/** 选中滑块：用更亮一点的玻璃块托住当前 tab。 */
private fun Modifier.liquidGlassTabsThumb(
    thumbSpec: LiquidGlassSpec,
    tabWidth: Dp,
    thumbOffset: Dp,
): Modifier {
    return fillMaxHeight()
        .offset(x = thumbOffset)
        .width(tabWidth)
        .liquidGlassSurface(thumbSpec)
}

/** 点击层：保持命中区域干净，避免手势打到相邻 tab。 */
private fun Modifier.liquidGlassTabsItem(
    enabled: Boolean,
    onClick: () -> Unit,
): Modifier {
    return clip(RoundedCornerShape(18.dp))
        .clickable(
            enabled = enabled,
            interactionSource = null,
            indication = null,
            role = Role.Tab,
            onClick = onClick,
        )
        .padding(horizontal = 12.dp, vertical = 8.dp)
}
