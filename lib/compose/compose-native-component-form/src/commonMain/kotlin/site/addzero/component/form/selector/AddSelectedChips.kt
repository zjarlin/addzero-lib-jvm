package site.addzero.component.form.selector

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val filterChipColors
    @Composable get() = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
    )

/**
 * 🏷️ 通用的已选择项目标签组件
 *
 * 支持任意类型的数据，显示为可移除的标签列表
 *
 * @param T 数据类型
 * @param selectedItems 已选择的项目列表
 * @param onRemoveItem 移除项目的回调
 * @param getLabel 获取项目显示文本的函数
 * @param getId 获取项目唯一标识的函数（可选，用于优化性能）
 * @param modifier 修饰符
 * @param enabled 是否启用交互
 * @param showRemoveIcon 是否显示移除图标
 * @param removeIcon 自定义移除图标
 * @param maxItems 最大显示项目数量，超出部分显示"..."
 * @param chipColors 标签颜色配置
 * @param contentPadding 内容边距
 * @param itemSpacing 项目间距
 */
@Composable
fun <T> AddSelectedChips(
    selectedItems: List<T>,
    onRemoveItem: (T) -> Unit,
    getLabel: (T) -> String,
    modifier: Modifier = Modifier,
    getId: ((T) -> Any)? = null,
    enabled: Boolean = true,
    showRemoveIcon: Boolean = true,
    removeIcon: ImageVector = Icons.Default.Close,
    maxItems: Int? = null,
    chipColors: SelectableChipColors = filterChipColors,
    contentPadding: PaddingValues = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
    itemSpacing: Dp = 6.dp
) {
    if (selectedItems.isEmpty()) return

    // 处理最大显示数量
    val displayItems = if (maxItems != null && selectedItems.size > maxItems) {
        selectedItems.take(maxItems)
    } else {
        selectedItems
    }

    val moreItemCount = if (maxItems != null && selectedItems.size > maxItems) {
        selectedItems.size - maxItems
    } else {
        0
    }
    val hasMoreItems = moreItemCount > 0

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        contentPadding = contentPadding,
        modifier = modifier
    ) {
        items(
            count = displayItems.size,
            key = if (getId != null) { index -> getId(displayItems[index]) } else null
        ) { index ->
            val item = displayItems[index]

            FilterChip(
                selected = true,
                onClick = {
                    if (enabled && showRemoveIcon) {
                        onRemoveItem(item)
                    }
                },
                label = {
                    Text(
                        text = getLabel(item),
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                trailingIcon = if (enabled && showRemoveIcon) {
                    {
                        Icon(
                            removeIcon,
                            contentDescription = "移除 ${getLabel(item)}",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else null,
                enabled = enabled,
                colors = chipColors
            )
        }

        // 显示"更多"指示器
        if (hasMoreItems) {
            item {
                FilterChip(
                    selected = false,
                    onClick = { },
                    label = {
                        Text(
                            text = "...+$moreItemCount",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    enabled = false,
                    colors = FilterChipDefaults.filterChipColors(
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
