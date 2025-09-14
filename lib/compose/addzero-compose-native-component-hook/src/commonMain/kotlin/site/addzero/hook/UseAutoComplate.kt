package site.addzero.hook

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import site.addzero.component.autocomplet.AddAutoComplete
import site.addzero.web.ui.hooks.UseHook


class UseAutoComplate<T>(
    val suggestions: List<T>,
    val title: String,
    val getLabelFun: (T) -> String,
    val maxSuggestions: Int = 5,
    val initialValue: String = "",
    val initialSelected: T? = null

) : UseHook<UseAutoComplate<T>> {
    var _suggestions by mutableStateOf(suggestions)
    var _selectedItem by mutableStateOf(null as T)
    override val render: @Composable (() -> Unit)
        get() = {
            AddAutoComplete(
                title = title,
                suggestions = _suggestions,
                maxSuggestions = maxSuggestions,
                getLabelFun = getLabelFun,
                onItemSelected = {
                    _selectedItem = it
                },
                modifier = modifier,
                initialValue = initialValue,
                initialSelected = initialSelected
            )
        }
}
