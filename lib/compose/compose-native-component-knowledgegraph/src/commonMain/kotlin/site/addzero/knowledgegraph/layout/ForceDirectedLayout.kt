package site.addzero.knowledgegraph.layout

import androidx.compose.ui.geometry.Offset
import site.addzero.knowledgegraph.component.NodeState
import site.addzero.knowledgegraph.model.GraphEdge
import site.addzero.knowledgegraph.model.GraphNode

class ForceDirectedLayout(
    private val width: Float,
    private val height: Float,
    private val repulsionStrength: Float = 5000f,
    private val attractionStrength: Float = 0.01f,
    private val damping: Float = 0.85f,
    private val centerGravity: Float = 0.01f
) {
    fun stepWithStates(nodeStates: Map<String, NodeState>) {
        if (nodeStates.isEmpty()) return
        
        val states = nodeStates.values.toList()
        val forces = states.associate { it.node.id to Offset.Zero }.toMutableMap()
        
        for (i in states.indices) {
            for (j in i + 1 until states.size) {
                val stateA = states[i]
                val stateB = states[j]
                if (stateA.isDragging || stateB.isDragging) continue
                
                val delta = stateA.position - stateB.position
                val distance = delta.getDistance().coerceAtLeast(1f)
                val force = delta / distance * (repulsionStrength / (distance * distance))
                
                forces[stateA.node.id] = forces[stateA.node.id]!! + force
                forces[stateB.node.id] = forces[stateB.node.id]!! - force
            }
        }
        
        val center = Offset(width / 2, height / 2)
        for (state in states) {
            if (state.isDragging) continue
            val toCenter = center - state.position
            forces[state.node.id] = forces[state.node.id]!! + toCenter * centerGravity
        }
        
        for (state in states) {
            if (state.isDragging) continue
            
            val force = forces[state.node.id] ?: Offset.Zero
            state.velocity = (state.velocity + force) * damping
            state.position = state.position + state.velocity
            
            state.position = Offset(
                state.position.x.coerceIn(50f, width - 50f),
                state.position.y.coerceIn(50f, height - 50f)
            )
        }
    }

    fun step(nodes: List<GraphNode>, edges: List<GraphEdge>) {
        if (nodes.isEmpty()) return
        
        val forces = nodes.associate { it.id to Offset.Zero }.toMutableMap()
        
        for (i in nodes.indices) {
            for (j in i + 1 until nodes.size) {
                val nodeA = nodes[i]
                val nodeB = nodes[j]
                if (nodeA.isDragging || nodeB.isDragging) continue
                
                val delta = nodeA.position - nodeB.position
                val distance = delta.getDistance().coerceAtLeast(1f)
                val force = delta / distance * (repulsionStrength / (distance * distance))
                
                forces[nodeA.id] = forces[nodeA.id]!! + force
                forces[nodeB.id] = forces[nodeB.id]!! - force
            }
        }
        
        val nodeMap = nodes.associateBy { it.id }
        for (edge in edges) {
            val source = nodeMap[edge.source] ?: continue
            val target = nodeMap[edge.target] ?: continue
            if (source.isDragging || target.isDragging) continue
            
            val delta = target.position - source.position
            val distance = delta.getDistance().coerceAtLeast(1f)
            val force = delta * attractionStrength * distance
            
            forces[source.id] = forces[source.id]!! + force
            forces[target.id] = forces[target.id]!! - force
        }
        
        val center = Offset(width / 2, height / 2)
        for (node in nodes) {
            if (node.isDragging) continue
            val toCenter = center - node.position
            forces[node.id] = forces[node.id]!! + toCenter * centerGravity
        }
        
        for (node in nodes) {
            if (node.isDragging) continue
            
            val force = forces[node.id] ?: Offset.Zero
            node.velocity = (node.velocity + force) * damping
            node.position = node.position + node.velocity
            
            node.position = Offset(
                node.position.x.coerceIn(50f, width - 50f),
                node.position.y.coerceIn(50f, height - 50f)
            )
        }
    }
}