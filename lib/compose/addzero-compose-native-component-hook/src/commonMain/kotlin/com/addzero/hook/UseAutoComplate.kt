package com.addzero.hook

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.addzero.component.autocomplet.AddAutoComplete
import com.addzero.web.ui.hooks.UseHook


class UseAutoComplate<T>(
    val title: String,
    val suggestions: List<T>,
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
