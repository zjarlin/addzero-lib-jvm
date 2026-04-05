package site.addzero.tool.coll

/**
 * 拓扑Spi接口
 * @author zjarlin
 * @date 2026/03/29
 * @constructor 创建[TopologicalSpi]
 */
interface TopologicalSpi {
  val key
    get() = this::class.simpleName!!
  val order
    get() = Int.MAX_VALUE
  val dependsOn: String?
    get() = null
}


/**
 * 基于 dependsOn 关系进行拓扑分层排序。
 *
 * 返回的每一层都满足可并发执行：同层节点互不依赖，层与层之间按顺序执行。
 */
fun <T, K> topoSort(
  items: Collection<T>,
  idSelector: (T) -> K,
  dependsOnSelector: (T) -> Collection<K>,
  ignoreMissingDependency: Boolean = false
): List<List<T>> {
  if (items.isEmpty()) {
    return emptyList()
  }

  val itemById = linkedMapOf<K, T>()
  items.forEach { item ->
    val id = idSelector(item)
    require(id !in itemById) { "Duplicate id: $id" }
    itemById[id] = item
  }

  val indegree = itemById.keys.associateWith { 0 }.toMutableMap()
  val outEdges = linkedMapOf<K, LinkedHashSet<K>>()

  itemById.forEach { (itemId, item) ->
    dependsOnSelector(item).forEach { dependencyId ->
      if (dependencyId !in itemById) {
        if (ignoreMissingDependency) return@forEach
        error("Missing dependency '$dependencyId' for '$itemId'")
      }
      val dependents = outEdges.getOrPut(dependencyId) { linkedSetOf() }
      if (dependents.add(itemId)) {
        indegree[itemId] = indegree.getValue(itemId) + 1
      }
    }
  }

  val currentLayerIds = ArrayDeque(indegree.filterValues { it == 0 }.keys)
  val layers = mutableListOf<List<T>>()
  var processedCount = 0

  while (currentLayerIds.isNotEmpty()) {
    val layerIds = mutableListOf<K>()
    while (currentLayerIds.isNotEmpty()) {
      layerIds += currentLayerIds.removeFirst()
    }

    val nextLayerIds = linkedSetOf<K>()
    val layerItems = mutableListOf<T>()

    layerIds.forEach { id ->
      val item = itemById.getValue(id)
      layerItems += item
      processedCount++

      outEdges[id].orEmpty().forEach { dependentId ->
        val newDegree = indegree.getValue(dependentId) - 1
        indegree[dependentId] = newDegree
        if (newDegree == 0) {
          nextLayerIds += dependentId
        }
      }
    }

    layers += layerItems
    nextLayerIds.forEach { currentLayerIds.addLast(it) }
  }

  if (processedCount != itemById.size) {
    val cycleNodes = indegree.filterValues { it > 0 }.keys
    error("Cycle detected in dependsOn graph: $cycleNodes")
  }

  return layers
}

/**
 * 基于 dependsOn 关系进行拓扑排序（单序列）。
 */
fun <T, K> topoSortByDependsOn(
  items: Collection<T>,
  idSelector: (T) -> K,
  dependsOnSelector: (T) -> Collection<K>,
  ignoreMissingDependency: Boolean = false
): List<T> {
  return topoSort(
    items = items,
    idSelector = idSelector,
    dependsOnSelector = dependsOnSelector,
    ignoreMissingDependency = ignoreMissingDependency
  ).flatten()
}

