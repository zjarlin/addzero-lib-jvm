package site.addzero.component.dropdown

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.*
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
 * 选择模式枚举
 */
enum class SelectMode {
    SINGLE,  // 单选
    MULTIPLE // 多选
}

/**
 * 泛型选择框选项作用域
 * @param T 数据类型
 */
class SelectScope<T> internal constructor(
    val item: T,
    val isSelected: Boolean,
    val labelProvider: (T) -> String,
    val showCheckIcon: Boolean
) {
    /**
     * 获取项目的标签文本
     */
    fun label(): String = labelProvider(item)

}

/**
 * 多选模式下显示选中项的组件
 */
@Composable
private fun <T> SelectedItemsDisplay(
    values: List<T>,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        values.forEach { item ->
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = label(item),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * 泛型选择框组件
 * @param T 数据类型
 * @param modifier 修饰符
 * @param value 当前选中的项目（单选模式）
 * @param values 当前选中的项目列表（多选模式）
 * @param items 选择项列表
 * @param onValueChange 选择项目的回调（单选模式）
 * @param onValuesChange 选择项目的回调（多选模式）
 * @param selectMode 选择模式（单选或多选）
 * @param label 标签提供函数 (T) -> String
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
 * @param itemContent 自定义选项内容渲染
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> AddSelect(
    title:String ="",
    modifier: Modifier = Modifier,
    value: T? = null,
    values: List<T> = emptyList(),
    items: List<T> = emptyList(),
    onValueChange: ((T) -> Unit)? = null,
    onValuesChange: ((List<T>) -> Unit)? = null,
    selectMode: SelectMode = SelectMode.SINGLE,
    label: (T) -> String = { it.toString() },
    placeholder: String = "请选择$title",
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    shape: Shape = RoundedCornerShape(8.dp),
    borderWidth: Dp = 1.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    maxDropdownHeight: Dp = 200.dp,
    showCheckIcon: Boolean = true,
    itemContent: @Composable SelectScope<T>.() -> Unit = {
        DefaultItem()
    }
) {
    // 内部管理展开状态
    var isExpanded by remember { mutableStateOf(false) }

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
        (selectMode == SelectMode.SINGLE && value != null) || (selectMode == SelectMode.MULTIPLE && values.isNotEmpty()) -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }

    Column(modifier = modifier) {
        // 主选择框
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = enabled,
                    onClick = { isExpanded = !isExpanded },
                    interactionSource = interactionSource,
                    indication = null
                )
                .semantics {
                    role = Role.DropdownList
                    contentDescription = "选择框，当前选择：${
                        when (selectMode) {
                            SelectMode.SINGLE -> value?.let { label(it) } ?: placeholder
                            SelectMode.MULTIPLE -> if (values.isEmpty()) placeholder else "${values.size} 项已选择"
                        }
                    }"
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
                    Box(modifier = Modifier.weight(1f)) {
                        when (selectMode) {
                            SelectMode.SINGLE -> {
                                Text(
                                    text = value?.let { label(it) } ?: placeholder,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textColor,
                                    fontWeight = if (value != null) FontWeight.Medium else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            SelectMode.MULTIPLE -> {
                                if (values.isEmpty()) {
                                    Text(
                                        text = placeholder,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = textColor,
                                        fontWeight = FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                } else {
                                    SelectedItemsDisplay(
                                        values = values,
                                        label = label,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
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

        // 下拉列表
        AnimatedVisibility(
            visible = isExpanded && items.isNotEmpty(),
            enter = expandVertically(animationSpec = tween(200)),
            exit = shrinkVertically(animationSpec = tween(200)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxDropdownHeight),
                shape = shape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                LazyColumn {
                    items(items) { item ->
                        val isSelected = when (selectMode) {
                            SelectMode.SINGLE -> value?.let { it == item } ?: false
                            SelectMode.MULTIPLE -> values.contains(item)
                        }
                        val scope = remember(item, isSelected) {
                            SelectScope(item, isSelected, label, showCheckIcon)
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    enabled = enabled,
                                    onClick = {
                                        when (selectMode) {
                                            SelectMode.SINGLE -> {
                                                onValueChange?.invoke(item)
                                                isExpanded = false
                                            }
                                            SelectMode.MULTIPLE -> {
                                                val newValues = if (isSelected) {
                                                    values.filter { it != item }
                                                } else {
                                                    values + item
                                                }
                                                onValuesChange?.invoke(newValues)
                                            }
                                        }
                                    }
                                ),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
                        ) {
                            scope.itemContent()
                        }
                    }
                }
            }
        }
    }
}

/**
 * 默认选项渲染组件
 */
@Composable
private fun <T> SelectScope<T>.DefaultItem() {
    val textColor = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label(),
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
