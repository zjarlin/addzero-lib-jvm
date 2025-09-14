package site.addzero.component.tree.selection

/**
 * 🎯 树节点选择状态枚举
 */
enum class SelectionState {
    /** 未选中 */
    UNSELECTED,

    /** 半选中（部分子节点选中） */
    INDETERMINATE,

    /** 全选中 */
    SELECTED
}

/**
 * 🎯 树节点选择信息
 */
data class TreeNodeSelection(
    val nodeId: Any,
    val state: site.addzero.component.tree.selection.SelectionState,
    val isLeaf: Boolean,
    val parentId: Any? = null,
    val childrenIds: Set<Any> = emptySet()
)

/**
 * 🎯 选择状态变化事件
 */
sealed class SelectionEvent {
    data class NodeClicked(val nodeId: Any) : site.addzero.component.tree.selection.SelectionEvent()
    data class NodeToggled(val nodeId: Any, val newState: site.addzero.component.tree.selection.SelectionState) : site.addzero.component.tree.selection.SelectionEvent()
    object ClearAll : site.addzero.component.tree.selection.SelectionEvent()
    data class SelectAll(val rootIds: Set<Any>) : site.addzero.component.tree.selection.SelectionEvent()
}

/**
 * 🎯 选择状态变化结果
 */
data class SelectionResult(
    val updatedNodes: Map<Any, site.addzero.component.tree.selection.SelectionState>,
    val selectedLeafNodes: Set<Any>,
    val affectedParents: Set<Any>
)

/**
 * 🎯 完整选择结果（包含推导的父节点）
 */
data class CompleteSelectionResult(
    val directSelectedNodes: Set<Any>,      // 直接选中的节点ID
    val indirectSelectedNodes: Set<Any>,    // 间接选中的父节点ID
    val completeSelectedNodes: Set<Any>,    // 完整的选中节点ID（直接+间接）
    val selectedNodeData: List<Any>         // 选中节点的完整数据
)
