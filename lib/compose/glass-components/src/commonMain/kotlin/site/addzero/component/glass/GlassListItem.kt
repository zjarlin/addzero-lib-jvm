package site.addzero.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.shapes.RoundedRectangle

/**
 * GlassListItem — 玻璃风格列表项
 *
 * 带缩略图、标题、副标题和尾部插槽的列表项组件。
 * 适用于曲目列表、播放队列等场景。
 *
 * @param title 主标题
 * @param modifier 外部修饰符
 * @param subtitle 副标题（如艺术家名）
 * @param isSelected 是否选中
 * @param onClick 点击回调
 * @param shape 列表项形状，默认 12dp 圆角
 * @param thumbnailSize 缩略图尺寸，默认 44dp
 * @param leading 前置内容 slot（如封面缩略图）
 * @param trailing 尾部内容 slot（如播放时长、播放动画指示器）
 */
@Composable
fun GlassListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedRectangle(12.dp),
    thumbnailSize: Dp = 44.dp,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val backgroundColor = if (isSelected) {
        GlassTheme.GlassSurfaceHover
    } else {
        Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(backgroundColor)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        // 前置内容（缩略图）
        if (leading != null) {
            Box(
                modifier = Modifier
                    .size(thumbnailSize)
                    .clip(RoundedRectangle(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                leading()
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        // 标题 + 副标题
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                color = if (isSelected) GlassTheme.TextPrimary else GlassTheme.TextSecondary,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = GlassTheme.TextTertiary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // 尾部内容
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}
