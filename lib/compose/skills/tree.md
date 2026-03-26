# 树与命令树组件索引

## 覆盖模块

- `compose-native-component-tree`
- `compose-native-component-searchbar`
- `compose-native-component-button`

## 适合场景

- 左侧导航树、资源树、目录树
- 带搜索和批量命令的资源管理面板
- 多选树、级联勾选树、平铺树列表

## 依赖入口

```kotlin
implementation(projects.lib.compose.composeNativeComponentTree)
```

## 组件速查

### 展示入口

- `AddTree<T>`：标准树
- `AddFlatTree<T>`：平铺树 / 扁平树
- `AddTreeWithCommand<T>`：树 + 命令工具条
- `CommandToolbar`：命令条

### 状态与选择

- `TreeViewModel<T>`
- `rememberTreeViewModel<T>()`
- `TreeSelectionManager<T>`
- `rememberTreeSelectionManager<T>()`
- `TreeNodeHierarchy<T>`
- `CascadingSelectionStrategy`

### 辅助

- `getNodeTypeIcon`
- `getNodeTypeColor`
- `getDefaultNodeType`

## 最小组合示例

```kotlin
@Composable
fun ResourceTree(nodes: List<ResourceNode>) {
    AddTree(
        items = nodes,
        getId = { it.id },
        getLabel = { it.name },
        getChildren = { it.children }
    )
}
```

```kotlin
@Composable
fun CommandTree(nodes: List<ResourceNode>) {
    AddTreeWithCommand(
        items = nodes,
        getId = { it.id },
        getLabel = { it.name },
        getChildren = { it.children },
        commands = listOf(
            TreeCommand(label = "刷新", onClick = {})
        )
    )
}
```

## 使用原则

- 资源树、屏幕树、菜单树优先复用这里，不要重新定义一套树数据结构
- 需要多选或级联勾选时，把逻辑放进 `TreeSelectionManager`
- 需要工具条和命令动作时，直接从 `AddTreeWithCommand` 起步
