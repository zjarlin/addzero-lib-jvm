package site.addzero.appsidebar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * 侧栏搜索、选中和展开状态的统一持有者。
 */
@Stable
class AppSidebarState internal constructor(
    initialSelectedId: String?,
) {
    var selectedId by mutableStateOf(initialSelectedId)
        private set

    var keyword by mutableStateOf("")
        private set

    internal val expandedItems = mutableStateMapOf<String, Boolean>()

    /**
     * 更新当前选中的节点 ID。
     */
    fun updateSelectedId(id: String?) {
        selectedId = id
    }

    /**
     * 更新搜索关键字。
     */
    fun updateKeyword(nextKeyword: String) {
        keyword = nextKeyword
    }

    /**
     * 清空当前搜索关键字。
     */
    fun clearKeyword() {
        keyword = ""
    }

    /**
     * 切换某个分组节点的展开状态。
     */
    fun toggleExpanded(id: String) {
        expandedItems[id] = !(expandedItems[id] ?: true)
    }

    /**
     * 按侧栏语义选择一个节点。
     *
     * 默认只允许叶子节点进入选中态，分组节点保留给展开/折叠交互。
     */
    fun <T> select(
        item: T,
        itemId: (T) -> String,
        children: (T) -> List<T>,
        selectable: (T) -> Boolean = { node -> children(node).isEmpty() },
    ) {
        if (selectable(item)) {
            selectedId = itemId(item)
        }
    }

    /**
     * 依据初始展开规则重建整棵树的展开态。
     */
    fun <T> resetExpandedState(
        items: List<T>,
        itemId: (T) -> String,
        children: (T) -> List<T>,
        initiallyExpanded: (T) -> Boolean = { true },
    ) {
        expandedItems.clear()
        registerInitialExpandedState(
            items = items,
            itemId = itemId,
            children = children,
            initiallyExpanded = initiallyExpanded,
        )
    }

    /**
     * 为当前选中节点自动展开所有祖先节点。
     */
    fun <T> revealSelection(
        items: List<T>,
        itemId: (T) -> String,
        children: (T) -> List<T>,
        selectedId: String? = this.selectedId,
    ) {
        items.findAncestorIds(
            selectedId = selectedId,
            itemId = itemId,
            children = children,
        ).forEach { ancestorId ->
            expandedItems[ancestorId] = true
        }
    }

    internal fun <T> ensureInitialized(
        items: List<T>,
        itemId: (T) -> String,
        children: (T) -> List<T>,
        initiallyExpanded: (T) -> Boolean = { true },
        selectable: (T) -> Boolean = { node -> children(node).isEmpty() },
    ) {
        registerInitialExpandedState(
            items = items,
            itemId = itemId,
            children = children,
            initiallyExpanded = initiallyExpanded,
        )
        if (selectedId == null) {
            selectedId = items.firstSelectableIdOrNull(
                itemId = itemId,
                children = children,
                selectable = selectable,
            )
        }
    }

    private fun <T> registerInitialExpandedState(
        items: List<T>,
        itemId: (T) -> String,
        children: (T) -> List<T>,
        initiallyExpanded: (T) -> Boolean,
    ) {
        items.forEach { item ->
            val childItems = children(item)
            if (childItems.isNotEmpty()) {
                val id = itemId(item)
                if (id !in expandedItems) {
                    expandedItems[id] = initiallyExpanded(item)
                }
                registerInitialExpandedState(
                    items = childItems,
                    itemId = itemId,
                    children = children,
                    initiallyExpanded = initiallyExpanded,
                )
            }
        }
    }

    internal companion object {
        val Saver = listSaver(
            save = { state ->
                listOf(
                    state.selectedId,
                    state.keyword,
                    state.expandedItems.entries.map { entry ->
                        listOf(entry.key, entry.value)
                    },
                )
            },
            restore = { restored ->
                val state = AppSidebarState(
                    initialSelectedId = restored[0] as String?,
                )
                state.updateKeyword(restored[1] as String)

                @Suppress("UNCHECKED_CAST")
                val expandedEntries = restored[2] as List<List<Any?>>
                expandedEntries.forEach { entry ->
                    val id = entry[0] as? String ?: return@forEach
                    val expanded = entry[1] as? Boolean ?: return@forEach
                    state.expandedItems[id] = expanded
                }
                state
            },
        )
    }
}

/**
 * 记住一个可保存的侧栏状态实例。
 */
@Composable
fun rememberAppSidebarState(
    initialSelectedId: String? = null,
): AppSidebarState = rememberSaveable(
    inputs = arrayOf(initialSelectedId),
    saver = AppSidebarState.Saver,
) {
    AppSidebarState(initialSelectedId = initialSelectedId)
}

internal fun <T> List<T>.filterSidebarItems(
    keyword: String,
    itemId: (T) -> String,
    label: (T) -> String,
    children: (T) -> List<T>,
): List<AppSidebarVisibleNode<T>> {
    val normalizedKeyword = keyword.trim()
    if (normalizedKeyword.isBlank()) {
        return map { item ->
            item.toVisibleSidebarNode(children = children)
        }
    }

    return mapNotNull { item ->
        item.filterSidebarItem(
            keyword = normalizedKeyword,
            itemId = itemId,
            label = label,
            children = children,
        )
    }
}

private fun <T> T.filterSidebarItem(
    keyword: String,
    itemId: (T) -> String,
    label: (T) -> String,
    children: (T) -> List<T>,
): AppSidebarVisibleNode<T>? {
    val childMatches = children(this).mapNotNull { child ->
        child.filterSidebarItem(
            keyword = keyword,
            itemId = itemId,
            label = label,
            children = children,
        )
    }

    val selfMatches = label(this).contains(keyword, ignoreCase = true) ||
        itemId(this).contains(keyword, ignoreCase = true)

    if (!selfMatches && childMatches.isEmpty()) {
        return null
    }

    val visibleChildren = if (selfMatches && children(this).isNotEmpty()) {
        children(this).map { child -> child.toVisibleSidebarNode(children = children) }
    } else {
        childMatches
    }

    return AppSidebarVisibleNode(
        item = this,
        children = visibleChildren,
    )
}

private fun <T> T.toVisibleSidebarNode(
    children: (T) -> List<T>,
): AppSidebarVisibleNode<T> {
    return AppSidebarVisibleNode(
        item = this,
        children = children(this).map { child ->
            child.toVisibleSidebarNode(children = children)
        },
    )
}

private fun <T> List<T>.findAncestorIds(
    selectedId: String?,
    itemId: (T) -> String,
    children: (T) -> List<T>,
): List<String> {
    if (selectedId == null) {
        return emptyList()
    }
    return asSequence()
        .mapNotNull { item ->
            item.findAncestorIds(
                selectedId = selectedId,
                itemId = itemId,
                children = children,
                ancestors = emptyList(),
            )
        }
        .firstOrNull()
        .orEmpty()
}

private fun <T> T.findAncestorIds(
    selectedId: String,
    itemId: (T) -> String,
    children: (T) -> List<T>,
    ancestors: List<String>,
): List<String>? {
    if (itemId(this) == selectedId) {
        return ancestors
    }
    return children(this).asSequence()
        .mapNotNull { child ->
            child.findAncestorIds(
                selectedId = selectedId,
                itemId = itemId,
                children = children,
                ancestors = ancestors + itemId(this),
            )
        }
        .firstOrNull()
}

private fun <T> List<T>.firstSelectableIdOrNull(
    itemId: (T) -> String,
    children: (T) -> List<T>,
    selectable: (T) -> Boolean,
): String? {
    return asSequence()
        .mapNotNull { item ->
            item.firstSelectableIdOrNull(
                itemId = itemId,
                children = children,
                selectable = selectable,
            )
        }
        .firstOrNull()
}

private fun <T> T.firstSelectableIdOrNull(
    itemId: (T) -> String,
    children: (T) -> List<T>,
    selectable: (T) -> Boolean,
): String? {
    if (selectable(this)) {
        return itemId(this)
    }
    return children(this).asSequence()
        .mapNotNull { child ->
            child.firstSelectableIdOrNull(
                itemId = itemId,
                children = children,
                selectable = selectable,
            )
        }
        .firstOrNull()
}

internal fun <T> T.containsSelection(
    selectedId: String?,
    itemId: (T) -> String,
    children: (T) -> List<T>,
): Boolean {
    if (selectedId == null) {
        return false
    }
    if (itemId(this) == selectedId) {
        return true
    }
    return children(this).any { child ->
        child.containsSelection(
            selectedId = selectedId,
            itemId = itemId,
            children = children,
        )
    }
}

internal data class AppSidebarVisibleNode<T>(
    val item: T,
    val children: List<AppSidebarVisibleNode<T>> = emptyList(),
)
