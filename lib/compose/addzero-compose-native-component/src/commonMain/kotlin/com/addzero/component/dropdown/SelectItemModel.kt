package com.addzero.component.dropdown

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 选择项数据模型
 * @param title 显示标题
 * @param value 实际值
 * @param enabled 是否启用
 */
data class SelectItemModel(
    val title: String,
    val value: Any,
    val enabled: Boolean = true
)

/**
 * 增强的基础选择框组件
 * @param modifier 修饰符
 * @param isExpanded 是否展开下拉列表
 * @param selectedItem 当前选中的项目
 * @param items 选择项列表
 * @param onToggle 切换展开状态的回调
 * @param onItemSelected 选择项目的回调
 * @param placeholder 占位符文本
 * @param enabled 是否启用组件
 * @param isError 是否显示错误状态
 * @param errorMessage 错误信息
 * @param leadingIcon 前置图标
 * @param backgroundColor 背景色
 * @param shape 形状
 * @param borderWidth 边框宽度
 * @param contentPadding 内容内边距
 * @param maxDropdownHeight 下拉列表最大高度
 * @param showCheckIcon 是否显示选中图标
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseSelect(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    selectedItem: SelectItemModel? = null,
    items: List<SelectItemModel> = emptyList(),
    onToggle: () -> Unit,
    onItemSelected: (SelectItemModel) -> Unit,
    placeholder: String = "请选择",
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    shape: Shape = RoundedCornerShape(8.dp),
    borderWidth: Dp = 1.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    maxDropdownHeight: Dp = 200.dp,
    showCheckIcon: Boolean = true
) {
    // 动画状态
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "arrow_rotation"
    )

    // 交互状态
    val interactionSource = remember { MutableInteractionSource() }

    // 边框颜色
    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        isExpanded -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    // 文本颜色
    val textColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        selectedItem != null -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }

    Column(modifier = modifier) {
        // 主选择框
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = enabled,
                    onClick = onToggle,
                    interactionSource = interactionSource,
                    indication = null
                )
                .semantics {
                    role = Role.DropdownList
                    contentDescription = "选择框，当前选择：${selectedItem?.title ?: placeholder}"
                },
            shape = shape,
            color = backgroundColor,
            border = BorderStroke(borderWidth, borderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 前置图标
                    leadingIcon?.let { icon ->
                        icon()
                    }

                    // 显示文本
                    Text(
                        text = selectedItem?.title ?: placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor,
                        fontWeight = if (selectedItem != null) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 箭头图标
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "收起" else "展开",
                    tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.38f
                    ),
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(arrowRotation)
                )
            }
        }

        // 错误信息
        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxDropdownHeight),
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,

            content = {
                // 下拉列表
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(animationSpec = tween(200)),
                    exit = shrinkVertically(animationSpec = tween(200)),
                    content = {

                        LazyColumn {
                            items(items) { item ->
                                SelectItem(
                                    item = item,
                                    isSelected = selectedItem?.value == item.value,
                                    onItemClick = {
                                        if (item.enabled) {
                                            onItemSelected(item)
                                        }
                                    },
                                    showCheckIcon = showCheckIcon,
                                    contentPadding = contentPadding
                                )
                            }
                        }
                    }
                )
            }
        )
    }
}

/**
 * 选择项组件
 */
@Composable
private fun SelectItem(
    item: SelectItemModel,
    isSelected: Boolean,
    onItemClick: () -> Unit,
    showCheckIcon: Boolean,
    contentPadding: PaddingValues
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else -> Color.Transparent
    }

    val textColor = when {
        !item.enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = item.enabled,
                onClick = onItemClick
            ),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (showCheckIcon && isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已选中",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
