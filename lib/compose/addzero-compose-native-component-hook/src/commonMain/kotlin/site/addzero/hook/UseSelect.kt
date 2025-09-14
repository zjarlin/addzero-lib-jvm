package site.addzero.hook

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import site.addzero.component.dropdown.AddSelect
import site.addzero.component.dropdown.SelectMode
import site.addzero.web.ui.hooks.UseHook

/**
 * 下拉选择Hook
 * @param T 数据类型
 * @param items 选项列表
 * @param title 标题
 * @param getLabelFun 标签获取函数
 * @param placeholder 占位符文本
 * @param selectMode 选择模式
 * @param initialValue 初始值（单选模式）
 * @param initialValues 初始值列表（多选模式）
 */
class UseSelect<T>(
    val items: List<T>,
    val title: String = "下拉选择",
    val getLabelFun: (T) -> String = { it.toString() },
    val placeholder: String = "请选择",
    val selectMode: SelectMode = SelectMode.SINGLE,
    val initialValue: T? = null,
    val initialValues: List<T> = emptyList()
) : UseHook<UseSelect<T>> {

    // 单选模式下的选中值
    var selectedValue by mutableStateOf(initialValue)

    // 多选模式下的选中值列表
    var selectedValues by mutableStateOf(initialValues)

    override val render: @Composable (() -> Unit)
        get() = {
            AddSelect(
                modifier = modifier,
                value = selectedValue,
                values = selectedValues,
                items = items,
                onValueChange = { selectedValue = it },
                onValuesChange = { selectedValues = it },
                selectMode = selectMode,
                label = getLabelFun,
                placeholder = placeholder
            )
        }
}
