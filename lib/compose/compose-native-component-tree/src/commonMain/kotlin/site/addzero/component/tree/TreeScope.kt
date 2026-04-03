package site.addzero.component.tree

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import site.addzero.component.search_bar.AddSearchBar

/**
 * `AddTree` 暴露给调用方的插槽范围。
 */
interface TreeScope<T> {
    val viewModel: TreeViewModel<T>

    /**
     * 在树顶部插入自定义内容。
     */
    @Composable
    fun TopSlot(content: @Composable () -> Unit)

    /**
     * 在搜索栏与树主体之间插入控制区内容。
     */
    @Composable
    fun ControlsSlot(content: @Composable () -> Unit)

    /**
     * 在树底部插入附加内容。
     */
    @Composable
    fun BottomSlot(content: @Composable () -> Unit)

    /**
     * 渲染内置搜索栏。
     */
    @Composable
    fun SearchBar()

    /**
     * 渲染内置展开/收起全部控制条。
     */
    @Composable
    fun ExpandCollapseControls()
}

/**
 * `TreeScope` 的默认实现。
 */
internal class TreeScopeImpl<T>(
    override val viewModel: TreeViewModel<T>,
) : TreeScope<T> {
    @Composable
    override fun TopSlot(content: @Composable () -> Unit) {
        content()
    }

    @Composable
    override fun ControlsSlot(content: @Composable () -> Unit) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent,
        ) {
            content()
        }
    }

    @Composable
    override fun BottomSlot(content: @Composable () -> Unit) {
        content()
    }

    @Composable
    override fun SearchBar() {
        if (viewModel.showSearchBar) {
            AddSearchBar(
                keyword = viewModel.searchQuery,
                onKeyWordChanged = viewModel::updateSearchQuery,
                onSearch = viewModel::performSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                placeholder = "搜索树节点...",
            )
        }
    }

    @Composable
    override fun ExpandCollapseControls() {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = viewModel::expandAll) {
                Text("展开全部")
            }
            TextButton(onClick = viewModel::collapseAll) {
                Text("收起全部")
            }
        }
    }
}
