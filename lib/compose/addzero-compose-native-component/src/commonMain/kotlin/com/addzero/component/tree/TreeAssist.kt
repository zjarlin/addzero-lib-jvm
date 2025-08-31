package com.addzero.component.tree

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

class TreeAssist

/**
 * 获取节点类型对应的图标
 */
@Composable
fun getNodeTypeIcon(nodeType: com.addzero.component.tree.NodeType, isExpanded: Boolean = false): ImageVector {
    return nodeType.getIcon(isExpanded)
}

/**
 * 获取节点类型对应的图标颜色
 */
@Composable
fun getNodeTypeColor(nodeType: com.addzero.component.tree.NodeType): Color {
    return nodeType.getColor()
}

fun <T> getDefaultNodeType(
    node: T, getLabel: (T) -> String, getId: (T) -> Any, getChildren: (T) -> List<T>
): com.addzero.component.tree.NodeType {
    val label = getLabel(node)
    val hasChildren = getChildren(node).isNotEmpty()
    val id = getId(node).toString()

    // 特殊ID处理
    if (id == "dept_1") {
        return _root_ide_package_.com.addzero.component.tree.NodeType.COMPANY
    }

    // 使用NodeType提供的猜测功能
    return _root_ide_package_.com.addzero.component.tree.NodeType.Companion.guess(label, hasChildren)
}
