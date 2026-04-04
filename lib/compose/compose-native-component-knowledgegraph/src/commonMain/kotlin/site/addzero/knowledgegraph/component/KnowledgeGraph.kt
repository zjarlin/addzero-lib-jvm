package site.addzero.knowledgegraph.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import site.addzero.knowledgegraph.layout.ForceDirectedLayout
import site.addzero.knowledgegraph.model.*

data class NodeState(
    val node: GraphNode,
    var position: Offset,
    var velocity: Offset = Offset.Zero,
    var isDragging: Boolean = false
)

@Composable
fun KnowledgeGraph(
    graphData: GraphData,
    selectedCategories: Set<NodeCategory> = emptySet(),
    searchKeyword: String = "",
    onNodeClick: (GraphNode) -> Unit = {},
    modifier: Modifier = Modifier,
    nodeLabelProvider: (GraphNode) -> String = { it.label },
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
    var size by remember { mutableStateOf(IntSize.Zero) }
    
    val nodeStates = remember(graphData, size) {
        if (size.width > 0 && size.height > 0) {
            mutableStateMapOf<String, NodeState>().apply {
                graphData.nodes.forEach { node ->
                    put(node.id, NodeState(
                        node = node,
                        position = GraphNode.randomPosition(size.width.toFloat(), size.height.toFloat())
                    ))
                }
            }
        } else mutableStateMapOf()
    }
    
    var updateTrigger by remember { mutableStateOf(0L) }
    
    val adjacencyMap by remember(graphData.edges) {
        derivedStateOf {
            buildMap<String, Set<String>> {
                graphData.edges.forEach { edge ->
                    put(edge.source, getOrDefault(edge.source, emptySet()) + edge.target)
                    put(edge.target, getOrDefault(edge.target, emptySet()) + edge.source)
                }
            }
        }
    }
    
    val filteredNodeStates by remember(nodeStates, selectedCategories, searchKeyword, adjacencyMap, updateTrigger) {
        derivedStateOf {
            val categoryFiltered = nodeStates.values.filter { state ->
                selectedCategories.isEmpty() || state.node.category in selectedCategories
            }
            
            if (searchKeyword.isBlank()) {
                categoryFiltered
            } else {
                val matchedNodes = categoryFiltered.filter { searchPredicate(it.node, searchKeyword) }
                val bfsResult = GraphSearchTraversal.searchWithDepth(
                    starts = matchedNodes.map { it.node },
                    getNeighbors = { node -> 
                        adjacencyMap[node.id]?.mapNotNull { id -> 
                            categoryFiltered.find { it.node.id == id }?.node 
                        } ?: emptyList()
                    },
                    predicate = { true },
                    maxDepth = bfsDepth
                )
                val resultIds = bfsResult.map { it.id }.toSet()
                categoryFiltered.filter { it.node.id in resultIds }
            }
        }
    }
    
    val filteredEdges by remember(graphData.edges, filteredNodeStates) {
        derivedStateOf {
            val nodeIds = filteredNodeStates.map { it.node.id }.toSet()
            graphData.edges.filter { it.source in nodeIds && it.target in nodeIds }
        }
    }

    val layout = remember(size) {
        if (size.width > 0 && size.height > 0) {
            ForceDirectedLayout(size.width.toFloat(), size.height.toFloat())
        } else null
    }

    var isAnimating by remember { mutableStateOf(true) }
    var draggedNodeId by remember { mutableStateOf<String?>(null) }
    var dragStartPos by remember { mutableStateOf(Offset.Zero) }
    var totalDragDistance by remember { mutableStateOf(0f) }

    LaunchedEffect(layout, nodeStates.size) {
        while (isAnimating && layout != null && nodeStates.isNotEmpty()) {
            layout.stepWithStates(nodeStates)
            updateTrigger = System.currentTimeMillis()
            delay(16)
        }
    }
    
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        color = Color.White,
        fontSize = 10.sp,
        textAlign = TextAlign.Center
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size = it }
            .background(Color(0xFF0d0d1a))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(filteredNodeStates) {
                    fun findVisibleNodeAt(offset: Offset): NodeState? = 
                        filteredNodeStates.find { (it.position - offset).getDistance() < 30f }
                    
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Press -> {
                                    val pos = event.changes.first().position
                                    dragStartPos = pos
                                    totalDragDistance = 0f
                                    findVisibleNodeAt(pos)?.let { state ->
                                        draggedNodeId = state.node.id
                                        nodeStates[state.node.id]?.isDragging = true
                                    }
                                }
                                PointerEventType.Move -> {
                                    draggedNodeId?.let { id ->
                                        val change = event.changes.first()
                                        val delta = change.position - change.previousPosition
                                        totalDragDistance += delta.getDistance()
                                        nodeStates[id]?.let { state ->
                                            state.position = state.position + delta
                                            updateTrigger = System.currentTimeMillis()
                                        }
                                    }
                                }
                                PointerEventType.Release -> {
                                    val pos = event.changes.first().position
                                    if (totalDragDistance < 10f) {
                                        findVisibleNodeAt(pos)?.let { onNodeClick(it.node) }
                                    }
                                    draggedNodeId?.let { id ->
                                        nodeStates[id]?.isDragging = false
                                    }
                                    draggedNodeId = null
                                    totalDragDistance = 0f
                                }
                            }
                        }
                    }
                }
        ) {
            val stateMap = filteredNodeStates.associateBy { it.node.id }

            filteredEdges.forEach { edge ->
                val sourceState = stateMap[edge.source] ?: return@forEach
                val targetState = stateMap[edge.target] ?: return@forEach
                
                val edgeColor = getColorForEdgeType(edge.type)

                drawLine(
                    color = edgeColor.copy(alpha = 0.6f),
                    start = sourceState.position,
                    end = targetState.position,
                    strokeWidth = 2f
                )

                val angle = kotlin.math.atan2(
                    targetState.position.y - sourceState.position.y,
                    targetState.position.x - sourceState.position.x
                )
                val arrowSize = 12f
                val arrowPoint = targetState.position - Offset(
                    kotlin.math.cos(angle) * 35f, 
                    kotlin.math.sin(angle) * 35f
                )
                
                val arrowPath = Path().apply {
                    moveTo(arrowPoint.x, arrowPoint.y)
                    lineTo(
                        arrowPoint.x - arrowSize * kotlin.math.cos(angle - 0.5f),
                        arrowPoint.y - arrowSize * kotlin.math.sin(angle - 0.5f)
                    )
                    lineTo(
                        arrowPoint.x - arrowSize * kotlin.math.cos(angle + 0.5f),
                        arrowPoint.y - arrowSize * kotlin.math.sin(angle + 0.5f)
                    )
                    close()
                }
                drawPath(arrowPath, edgeColor)
                
                val edgeLabel = edgeLabelProvider(edge)
                if (edgeLabel.isNotBlank()) {
                    val midPoint = Offset(
                        (sourceState.position.x + targetState.position.x) / 2f,
                        (sourceState.position.y + targetState.position.y) / 2f
                    )
                    val edgeLabelResult = textMeasurer.measure(
                        text = edgeLabel,
                        style = labelStyle.copy(fontSize = 9.sp, color = edgeColor),
                        maxLines = 1
                    )
                    drawText(
                        textLayoutResult = edgeLabelResult,
                        topLeft = Offset(
                            midPoint.x - edgeLabelResult.size.width / 2f,
                            midPoint.y - edgeLabelResult.size.height / 2f - 8f
                        )
                    )
                }
            }

            filteredNodeStates.forEach { state ->
                val node = state.node
                val nodeColor = getColorForNodeCategory(node.category)
                val isDragged = state.isDragging
                val radius = if (isDragged) 32f else 26f

                drawCircle(
                    color = Color.Black.copy(alpha = 0.3f),
                    radius = radius + 2f,
                    center = state.position + Offset(2f, 2f)
                )

                drawCircle(
                    color = nodeColor.copy(alpha = 0.4f),
                    radius = radius + 12f,
                    center = state.position
                )

                drawCircle(
                    color = nodeColor,
                    radius = radius,
                    center = state.position
                )

                drawCircle(
                    color = Color.White.copy(alpha = 0.2f),
                    radius = radius * 0.6f,
                    center = state.position,
                    style = Stroke(width = 2f)
                )

                drawCircle(
                    color = if (isDragged) Color.White else Color.White.copy(alpha = 0.7f),
                    radius = radius,
                    center = state.position,
                    style = Stroke(width = if (isDragged) 3f else 2f)
                )

                val label = nodeLabelProvider(node)
                val displayLabel = if (label.length > 8) label.take(8) + ".." else label
                
                val textLayoutResult = textMeasurer.measure(
                    text = displayLabel,
                    style = labelStyle,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        state.position.x - textLayoutResult.size.width / 2f,
                        state.position.y - textLayoutResult.size.height / 2f
                    )
                )
            }
        }
    }
}

fun getColorForNodeCategory(category: NodeCategory): Color = when (category) {
    // Shell相关节点颜色
    NodeCategory.SHELL_VARIABLE, NodeCategory.SHELL_EXPORT -> Color(0xFF4CAF50)
    NodeCategory.SHELL_FUNCTION -> Color(0xFF2196F3)
    NodeCategory.SHELL_ALIAS -> Color(0xFFFF9800)
    NodeCategory.SHELL_SOURCE -> Color(0xFFE91E63)
    NodeCategory.SHELL_PATH -> Color(0xFF9C27B0)
    NodeCategory.SHELL_COMMAND -> Color(0xFF00BCD4)
    NodeCategory.SHELL_COMMENT -> Color(0xFF8BC34A)
    NodeCategory.SHELL_CONDITIONAL -> Color(0xFF3F51B5)
    NodeCategory.SHELL_LOOP -> Color(0xFFCDDC39)
    NodeCategory.SHELL_EVAL -> Color(0xFFF44336)
    
    // Neovim相关节点颜色
    NodeCategory.LUA_KEYMAP -> Color(0xFF00BCD4)
    NodeCategory.LUA_OPTION -> Color(0xFF8BC34A)
    NodeCategory.LUA_AUTOCMD -> Color(0xFF3F51B5)
    NodeCategory.LUA_PLUGIN -> Color(0xFFFF5722)
    NodeCategory.LUA_FUNCTION -> Color(0xFF2196F3)
    NodeCategory.LUA_REQUIRE -> Color(0xFFCDDC39)
    NodeCategory.LUA_VARIABLE -> Color(0xFF4CAF50)
    NodeCategory.LUA_TABLE -> Color(0xFF9C27B0)
    NodeCategory.LUA_COMMAND -> Color(0xFF795548)
    NodeCategory.LUA_COMMENT -> Color(0xFF607D8B)
    NodeCategory.LUA_HIGHLIGHT -> Color(0xFF009688)
    
    // Git相关节点颜色
    NodeCategory.GIT_ALIAS -> Color(0xFFF44336)
    NodeCategory.GIT_SECTION -> Color(0xFF795548)
    NodeCategory.GIT_INCLUDE -> Color(0xFFFF9800)
    NodeCategory.GIT_IGNORE -> Color(0xFF607D8B)
    
    // SSH相关节点颜色
    NodeCategory.SSH_HOST -> Color(0xFF009688)
    NodeCategory.SSH_OPTION -> Color(0xFF4CAF50)
    NodeCategory.SSH_MATCH -> Color(0xFF9C27B0)
    NodeCategory.SSH_INCLUDE -> Color(0xFFFF9800)
    NodeCategory.SSH_COMMENT -> Color(0xFF607D8B)
    
    // 默认节点颜色
    else -> Color(0xFF607D8B)
}

fun getColorForEdgeType(type: EdgeType): Color = when (type) {
    EdgeType.SOURCE -> Color(0xFF4CAF50)
    EdgeType.REQUIRE -> Color(0xFF2196F3)
    EdgeType.INCLUDE -> Color(0xFFFF9800)
    EdgeType.DEPENDS -> Color(0xFF9C27B0)
    EdgeType.SIMILAR -> Color(0xFF607D8B)
}

private object GraphSearchTraversal {

    fun <T> searchWithDepth(
        starts: Iterable<T>,
        getNeighbors: (T) -> Iterable<T>,
        predicate: (T) -> Boolean,
        maxDepth: Int = Int.MAX_VALUE,
    ): List<T> {
        val visited = mutableSetOf<T>()
        val result = mutableListOf<T>()
        val queue = ArrayDeque<Pair<T, Int>>()

        starts.filter(predicate).forEach { start ->
            if (start !in visited) {
                visited += start
                queue += start to 0
            }
        }

        while (queue.isNotEmpty()) {
            val (current, depth) = queue.removeFirst()
            result += current

            if (depth >= maxDepth) continue

            getNeighbors(current)
                .filter { it !in visited }
                .forEach { neighbor ->
                    visited += neighbor
                    queue += neighbor to (depth + 1)
                }
        }

        return result
    }
}
