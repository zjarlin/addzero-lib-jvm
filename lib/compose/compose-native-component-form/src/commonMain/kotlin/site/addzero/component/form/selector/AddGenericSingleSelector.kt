package site.addzero.component.form.selector

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 🎯 通用单选选择器组件
 *
 * 基于 AddGenericMultiSelector 的单选版本
 *
 * @param T 数据类型
 * @param value 当前选择的项目
 * @param onValueChange 选择变化回调
 * @param dataProvider 数据提供者
 * @param getId 获取项目ID的函数
 * @param getLabel 获取项目显示标签的函数
 * @param getChildren 获取子项目的函数
 * @param modifier 修饰符
 * @param placeholder 占位符文本
 * @param enabled 是否启用
 * @param maxHeight 最大高度
 * @param allowClear 是否允许清除选择
 * @param getIcon 获取项目图标的函数
 * @param getNodeType 获取节点类型的函数
 */
@Composable
fun <T> AddGenericSingleSelector(
    value: T?,
    onValueChange: (T?) -> Unit,
    dataProvider: suspend () -> List<T>,
    getId: (T) -> Any,
    getLabel: (T) -> String,
    getChildren: (T) -> List<T> = { emptyList() },
    modifier: Modifier = Modifier.Companion,
    placeholder: String = "请选择",
    enabled: Boolean = true,
    maxHeight: Dp = 400.dp,
    allowClear: Boolean = true,
    getIcon: @Composable (T) -> ImageVector? = { null },
    getNodeType: (T) -> String = { "item" }
) {
    AddGenericMultiSelector(
        value = value?.let { listOf(it) } ?: emptyList(),
        onValueChange = { items -> onValueChange(items.firstOrNull()) },
        dataProvider = dataProvider,
        getId = getId,
        getLabel = getLabel,
        getChildren = getChildren,
        modifier = modifier,
        placeholder = placeholder,
        enabled = enabled,
        maxHeight = maxHeight,
        allowClear = allowClear,
        multiSelect = false,
        showConfirmButton = false, // 单选模式不需要确认按钮
        getIcon = getIcon,
        getNodeType = getNodeType,
        commands = setOf(
            site.addzero.component.tree_command.TreeCommand.SEARCH,
            site.addzero.component.tree_command.TreeCommand.EXPAND_ALL,
            site.addzero.component.tree_command.TreeCommand.COLLAPSE_ALL
            // 不包含 MULTI_SELECT
        )
    )
}
