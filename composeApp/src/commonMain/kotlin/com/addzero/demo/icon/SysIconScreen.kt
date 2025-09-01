package com.addzero.demo.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.addzero.annotation.Route
import com.addzero.component.tree_command.AddTreeWithCommand
import com.addzero.component.tree_command.TreeCommand
import com.addzero.compose.icons.IconMap


/**
 * 图标元数据数据类
 */
data class SysIcon(
    val id: String?,
    val parentId: String?,
    val name: String,
    val fullName: String?,
    val chineseName: String?,
    val iconType: String?,
    val nodeType: String = "图标",
    val children: MutableList<com.addzero.demo.icon.SysIcon> = mutableListOf(),
    val imageVector: ImageVector? = null
)

/**
 * 图标管理屏幕
 */
@Composable
@Route("组件示例", "M3图标树")
fun SysIconScreen() {
    // 构建图标树结构，以iconType作为一级节点
    val iconTree = com.addzero.demo.icon.getSysIconTree()

    AddTreeWithCommand(
        commands = setOf(TreeCommand.SEARCH),
        getId = { it.id!! },
        getLabel = { it.name },
        getChildren = { it.children },

        items = iconTree,
    )

//    AddTree(
//        items = iconTree,
//        getId = { it.id!! },
//        getLabel = { it.name },
//        getChildren = { it.children },
//        getIcon = { it.imageVector },
//    )


}

@Composable
private fun getSysIconTree(): MutableList<com.addzero.demo.icon.SysIcon> {
    val rootNodes = mutableListOf<com.addzero.demo.icon.SysIcon>()

    // 获取所有图标类型
    val iconTypes = IconMap.getAllTypes()

    // 为每种类型创建一级节点
    iconTypes.forEach { type ->
        val typeNode = com.addzero.demo.icon.SysIcon(
            id = type,
            parentId = null,
            name = type,
            fullName = type,
            chineseName = null,
            iconType = type,
            nodeType = "图标种类",
        )

        // 获取该类型下的所有图标
        val iconsOfType = IconMap.getByType(type)

        // 为每个图标创建二级节点
        iconsOfType.forEach { iconName ->
            val iconNode = com.addzero.demo.icon.SysIcon(
                id = "$type:$iconName",
                parentId = type,
                name = iconName,
                fullName = "${IconMap.allIcons.find { it.iconKey == iconName && it.iconType == type }?.vector?.name}",
                chineseName = null,
                iconType = type,
                nodeType = "图标",
                imageVector = IconMap[iconName].vector
            )
            typeNode.children.add(iconNode)
        }

        rootNodes.add(typeNode)
    }

    return rootNodes
}
