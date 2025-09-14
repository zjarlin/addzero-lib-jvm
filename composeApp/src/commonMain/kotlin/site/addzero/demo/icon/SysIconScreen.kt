package site.addzero.demo.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import site.addzero.annotation.Route
import site.addzero.component.tree_command.AddTreeWithCommand
import site.addzero.component.tree_command.TreeCommand
import site.addzero.compose.icons.IconMap


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
    val children: MutableList<site.addzero.demo.icon.SysIcon> = mutableListOf(),
    val imageVector: ImageVector? = null
)

/**
 * 图标管理屏幕
 */
@Composable
@Route("组件示例", "M3图标树")
fun SysIconScreen() {
    // 构建图标树结构，以iconType作为一级节点
    val iconTree = site.addzero.demo.icon.getSysIconTree()

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
private fun getSysIconTree(): MutableList<site.addzero.demo.icon.SysIcon> {
    val rootNodes = mutableListOf<site.addzero.demo.icon.SysIcon>()

    // 获取所有图标类型
    val iconTypes = IconMap.getAllTypes()

    // 为每种类型创建一级节点
    iconTypes.forEach { type ->
        val typeNode = site.addzero.demo.icon.SysIcon(
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
            val iconNode = site.addzero.demo.icon.SysIcon(
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
