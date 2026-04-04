package site.addzero.knowledgegraph.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import site.addzero.knowledgegraph.component.KnowledgeGraph
import site.addzero.knowledgegraph.model.GraphData
import site.addzero.knowledgegraph.model.GraphEdge
import site.addzero.knowledgegraph.model.GraphNode
import site.addzero.knowledgegraph.model.NodeCategory

@Composable
fun ForceDirectedGraph(
    graphData: GraphData,
    selectedCategories: Set<NodeCategory>,
    searchKeyword: String,
    onNodeClick: (GraphNode) -> Unit,
    modifier: Modifier = Modifier,
    nodeLabelProvider: (GraphNode) -> String = { it.label.ifEmpty { it.filePath?.substringAfterLast("/") ?: "Node-${it.id}" } },
    edgeLabelProvider: (GraphEdge) -> String = { it.label ?: "" },
    searchPredicate: (GraphNode, String) -> Boolean = { node, keyword ->
        keyword.isBlank() ||
        node.label.contains(keyword, ignoreCase = true) ||
        node.description?.contains(keyword, ignoreCase = true) == true ||
        node.filePath?.contains(keyword, ignoreCase = true) == true ||
        node.content?.contains(keyword, ignoreCase = true) == true
    },
    bfsDepth: Int = 1
) {
    KnowledgeGraph(
        graphData = graphData,
        selectedCategories = selectedCategories,
        searchKeyword = searchKeyword,
        onNodeClick = onNodeClick,
        modifier = modifier,
        nodeLabelProvider = nodeLabelProvider,
        edgeLabelProvider = edgeLabelProvider,
        searchPredicate = searchPredicate,
        bfsDepth = bfsDepth
    )
}