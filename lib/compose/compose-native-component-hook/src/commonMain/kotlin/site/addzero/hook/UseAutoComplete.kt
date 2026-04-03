package site.addzero.hook

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import site.addzero.component.autocomplet.AddAutoComplete

/**
 * 自动补全 Hook。
 *
 * 统一维护建议项和当前选中项，避免旧实现里通过 `null as T` 伪造初始值。
 *
 * @param T 建议项类型
 * @param suggestions 初始建议项列表
 * @param title 输入框标题
 * @param getLabelFun 建议项文案提取函数
 * @param maxSuggestions 最多展示的建议项数量
 * @param initialValue 初始输入内容
 * @param initialSelected 初始选中项
 */
class UseAutoComplete<T>(
    suggestions: List<T>,
    val title: String,
    val getLabelFun: (T) -> String,
    val maxSuggestions: Int = 5,
    val initialValue: String = "",
    initialSelected: T? = null,
) : UseHook {

    /**
     * 当前可选建议项列表。
     */
    var currentSuggestions by mutableStateOf(suggestions)

    /**
     * 当前选中的建议项。
     */
    var selectedItem by mutableStateOf(initialSelected)

    override val render: @Composable (() -> Unit)
        get() = {
            AddAutoComplete(
                title = title,
                suggestions = currentSuggestions,
                maxSuggestions = maxSuggestions,
                getLabelFun = getLabelFun,
                onItemSelected = { selectedItem = it },
                modifier = modifier,
                initialValue = initialValue,
                initialSelected = selectedItem,
            )
        }
}

/**
 * 兼容旧拼写，后续统一迁移到 [UseAutoComplete]。
 */
@Deprecated(
    message = "请改用 UseAutoComplete，旧拼写会在后续版本移除。",
    replaceWith = ReplaceWith("UseAutoComplete<T>"),
)
typealias UseAutoComplate<T> = UseAutoComplete<T>
