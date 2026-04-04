# compose-native-component-knowledgegraph

可复用的 Compose 知识图谱组件，适合配置关系、依赖关系、文件节点关系等可视化场景。

- Maven 坐标：`site.addzero:compose-native-component-knowledgegraph`
- 本地模块路径：`lib/compose/compose-native-component-knowledgegraph`

## 提供能力

- 力导向图布局
- 节点分类着色
- 按类别筛选与关键词过滤
- BFS 邻域扩散搜索
- JVM 端代码抽屉编辑器

## 最小示例

```kotlin
val graphData = GraphData(
    nodes = listOf(
        GraphNode(
            id = "1",
            label = "init.lua",
            category = NodeCategory.LUA_PLUGIN,
            filePath = "~/.config/nvim/init.lua",
            content = "require(\"plugins\")",
        ),
    ),
    edges = emptyList(),
)

ForceDirectedGraph(
    graphData = graphData,
    selectedCategories = emptySet(),
    searchKeyword = "",
    onNodeClick = { node ->
        println(node.label)
    },
)
```

## 约束

- 图谱主体位于 `commonMain`
- `CodeEditorDrawer` 依赖 `java.awt`，仅放在 `jvmMain`
- 当前模块只负责 UI 与布局，不负责图数据采集

