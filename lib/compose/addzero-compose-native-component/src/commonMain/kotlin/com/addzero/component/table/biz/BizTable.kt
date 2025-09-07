package com.addzero.component.table.biz

import androidx.compose.runtime.Composable
import com.addzero.component.search_bar.AddSearchBar
import com.addzero.component.table.original.TableOriginalState
import com.addzero.component.table.original.TableOriginalWidget


@Composable
context(tableViewModel: BizTableViewModel<*, *>, tableButtonViewModel: TableButtonViewModel)
inline fun <reified T, C> BizTable(
    state: TableOriginalState<T, C>,
    noinline buttonSlot: @Composable () -> Unit
) {
    state.topSlot = {
        AddSearchBar(
            keyword = tableViewModel.keyword,
            onKeyWordChanged = { tableViewModel.keyword = it },
            onSearch = { tableViewModel.onSearch() },
            leftSloat = {
                RenderButtons(buttonSlot)
            }
        )
    }
//    state.
    TableOriginalWidget(
        state = state
    )
}
