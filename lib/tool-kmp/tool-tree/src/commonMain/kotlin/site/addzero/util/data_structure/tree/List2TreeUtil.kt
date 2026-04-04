package site.addzero.util.data_structure.tree

import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

object List2TreeUtil {

  /**
   * 将列表转换为树形结构。
   */
  fun <T> list2Tree(
    source: List<T>,
    idFun: (T) -> Any?,
    pidFun: (T) -> Any?,
    getChildFun: (T) -> List<T>?,
    setChildFun: (T, MutableList<T>) -> Unit
  ): List<T> {
    return list2TreeInternal(
      source = source,
      isRoot = null,
      idFun = idFun,
      pidFun = pidFun,
      getChildFun = getChildFun,
      setChildFun = setChildFun
    )
  }

  /**
   * 将列表转换为树形结构。
   */
  fun <T> list2Tree(
    source: List<T>,
    isRoot: (T) -> Boolean,
    idFun: (T) -> Any?,
    pidFun: (T) -> Any?,
    getChildFun: (T) -> List<T>?,
    setChildFun: (T, MutableList<T>) -> Unit
  ): List<T> {
    return list2TreeInternal(
      source = source,
      isRoot = isRoot,
      idFun = idFun,
      pidFun = pidFun,
      getChildFun = getChildFun,
      setChildFun = setChildFun
    )
  }

  /**
   * 使用属性引用将列表转换为树形结构。
   */
  fun <T> list2Tree(
    source: List<T>,
    idProperty: KProperty1<T, Any?>,
    parentIdProperty: KProperty1<T, Any?>,
    childrenProperty: KMutableProperty1<T, MutableList<T>?>
  ): List<T> {
    return list2TreeInternal(
      source = source,
      isRoot = null,
      idFun = idProperty::get,
      pidFun = parentIdProperty::get,
      getChildFun = childrenProperty::get,
      setChildFun = childrenProperty::set
    )
  }

  /**
   * 使用属性引用将列表转换为树形结构。
   */
  fun <T> list2Tree(
    source: List<T>,
    isRoot: (T) -> Boolean,
    idProperty: KProperty1<T, Any?>,
    parentIdProperty: KProperty1<T, Any?>,
    childrenProperty: KMutableProperty1<T, MutableList<T>?>
  ): List<T> {
    return list2TreeInternal(
      source = source,
      isRoot = isRoot,
      idFun = idProperty::get,
      pidFun = parentIdProperty::get,
      getChildFun = childrenProperty::get,
      setChildFun = childrenProperty::set
    )
  }

  /**
   * 将树形结构转换为列表。
   */
  fun <T> tree2List(
    treeData: List<T>,
    getChildFun: (T) -> List<T>?,
    setChildFun: (T, MutableList<T>) -> Unit
  ): List<T> {
    return tree2ListInternal(
      treeData = treeData,
      getChildFun = getChildFun,
      setChildFun = setChildFun
    )
  }

  /**
   * 使用属性引用将树形结构转换为列表。
   */
  fun <T> tree2List(
    treeData: List<T>,
    childrenProperty: KMutableProperty1<T, MutableList<T>?>
  ): List<T> {
    return tree2ListInternal(
      treeData = treeData,
      getChildFun = childrenProperty::get,
      setChildFun = childrenProperty::set
    )
  }

  /**
   * 获取节点到根的路径。
   */
  fun <T> getBreadcrumbList(
    list: List<T>,
    targetId: Any,
    getId: KProperty1<T, Any>,
    getParentId: KProperty1<T, Any?>
  ): List<T> {
    if (list.isEmpty()) {
      return emptyList()
    }

    val nodeMap = HashMap<Any, T>(mapCapacity(list.size))
    for (node in list) {
      nodeMap[getId.get(node)] = node
    }

    val path = ArrayDeque<T>()
    var currentId: Any? = targetId
    while (currentId != null) {
      val node = nodeMap[currentId] ?: break
      path.addFirst(node)
      currentId = getParentId.get(node)
    }
    return path.toList()
  }

  /**
   * 获取节点到根的树形路径(面包屑 )。
   */
  fun <T> getBreadcrumbList(
    list: List<T>,
    targetId: Any,
    getId: KProperty1<T, Any>,
    getParentId: KProperty1<T, Any?>,
    childrenProperty: KMutableProperty1<T, MutableList<T>?>
  ): List<T> {
    val pathNodes = getBreadcrumbList(list, targetId, getId, getParentId)
    return buildBreadcrumbTree(
      pathNodes = pathNodes,
      getChildren = childrenProperty::get,
      setChildren = childrenProperty::set
    )
  }

  /**
   * 获取节点到根的树形路径getBreadcrumbList。
   */
  fun <T> getBreadcrumbList(
    list: List<T>,
    targetId: Any,
    getId: KProperty1<T, Any>,
    getParentId: KProperty1<T, Any?>,
    getChildren: KMutableProperty1<T, MutableList<T>>,
    setChildren: (T, MutableList<T>) -> Unit
  ): List<T> {
    val pathNodes = getBreadcrumbList(list, targetId, getId, getParentId)
    return buildBreadcrumbTree(
      pathNodes = pathNodes,
      getChildren = getChildren::get,
      setChildren = setChildren
    )
  }

  private fun <T> list2TreeInternal(
    source: List<T>,
    isRoot: ((T) -> Boolean)?,
    idFun: (T) -> Any?,
    pidFun: (T) -> Any?,
    getChildFun: (T) -> List<T>?,
    setChildFun: (T, MutableList<T>) -> Unit
  ): List<T> {
    if (source.isEmpty()) {
      return emptyList()
    }

    val capacity = mapCapacity(source.size)
    val nodeMap = HashMap<Any?, T>(capacity)
    val childBuckets = HashMap<Any?, MutableList<T>>(capacity)
    val roots = ArrayList<T>(source.size)

    for (node in source) {
      val nodeId = idFun(node)
      val parentId = pidFun(node)
      nodeMap[nodeId] = node

      if (parentId == null || isRoot?.invoke(node) == true) {
        roots.add(node)
      }
    }

    for (node in source) {
      val parentId = pidFun(node) ?: continue
      if (!nodeMap.containsKey(parentId)) {
        continue
      }

      val children = childBuckets.getOrPut(parentId) {
        mutableListOf()
      }
      children.add(node)
    }

    for (node in source) {
      val nodeId = idFun(node)
      val children = childBuckets[nodeId]
      if (children != null) {
        setChildFun(node, children)
        continue
      }

      clearChildrenIfNeeded(
        node = node,
        getChildFun = getChildFun,
        setChildFun = setChildFun
      )
    }

    return roots
  }

  private fun <T> tree2ListInternal(
    treeData: List<T>,
    getChildFun: (T) -> List<T>?,
    setChildFun: (T, MutableList<T>) -> Unit
  ): List<T> {
    if (treeData.isEmpty()) {
      return emptyList()
    }

    val result = ArrayList<T>()
    val stack = ArrayDeque<T>(treeData.size)

    for (index in treeData.lastIndex downTo 0) {
      stack.addLast(treeData[index])
    }

    while (stack.isNotEmpty()) {
      val node = stack.removeLast()
      result.add(node)

      val children = getChildFun(node)
      if (children.isNullOrEmpty()) {
        continue
      }

      for (index in children.lastIndex downTo 0) {
        stack.addLast(children[index])
      }

      setChildFun(node, children.clearToMutableBuffer())
    }

    return result
  }

  private fun <T> buildBreadcrumbTree(
    pathNodes: List<T>,
    getChildren: (T) -> MutableList<T>?,
    setChildren: (T, MutableList<T>) -> Unit
  ): List<T> {
    if (pathNodes.isEmpty()) {
      return emptyList()
    }

    for (node in pathNodes) {
      setChildren(node, mutableListOf())
    }

    for (index in 0 until pathNodes.lastIndex) {
      val parent = pathNodes[index]
      val child = pathNodes[index + 1]
      val children = getChildren(parent)
      if (children == null) {
        setChildren(parent, mutableListOf(child))
        continue
      }
      children.add(child)
    }

    return listOf(pathNodes.first())
  }

  private fun <T> clearChildrenIfNeeded(
    node: T,
    getChildFun: (T) -> List<T>?,
    setChildFun: (T, MutableList<T>) -> Unit
  ) {
    val currentChildren = getChildFun(node)
    if (currentChildren.isNullOrEmpty()) {
      return
    }
    setChildFun(node, currentChildren.clearToMutableBuffer())
  }

  private fun <T> List<T>.clearToMutableBuffer(): MutableList<T> {
    if (this is MutableList<T>) {
      clear()
      return this
    }
    return mutableListOf()
  }

  private fun mapCapacity(expectedSize: Int): Int {
    return when {
      expectedSize < 3 -> expectedSize + 1
      expectedSize < Int.MAX_VALUE / 2 -> expectedSize + expectedSize / 3
      else -> Int.MAX_VALUE
    }
  }
}
