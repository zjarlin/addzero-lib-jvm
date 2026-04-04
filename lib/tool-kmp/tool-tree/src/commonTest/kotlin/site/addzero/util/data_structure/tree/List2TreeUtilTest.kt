package site.addzero.util.data_structure.tree

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class List2TreeUtilTest {

  @Test
  fun `list2Tree should rebuild child lists in source order`() {
    val staleNode = PropertyNode(99, 2)
    val root = PropertyNode(1, null)
    val left = PropertyNode(2, 1, mutableListOf(staleNode))
    val right = PropertyNode(3, 1)
    val orphanRoot = PropertyNode(4, null)

    val tree = List2TreeUtil.list2Tree(
      source = listOf(root, left, right, orphanRoot),
      idProperty = PropertyNode::id,
      parentIdProperty = PropertyNode::parentId,
      childrenProperty = PropertyNode::children
    )

    assertEquals(listOf(1, 4), tree.map { it.id })
    assertEquals(listOf(2, 3), tree.first().children?.map { it.id })
    assertTrue(left.children.isNullOrEmpty())
    assertTrue(right.children.isNullOrEmpty())
  }

  @Test
  fun `list2Tree should support Kotlin method references`() {
    val root = MethodNode(1, null)
    val child = MethodNode(2, 1)

    val tree = List2TreeUtil.list2Tree(
      source = listOf(root, child),
      idFun = MethodNode::getId,
      pidFun = MethodNode::getParentId,
      getChildFun = MethodNode::getChildren,
      setChildFun = MethodNode::setChildren
    )

    assertEquals(listOf(1), tree.map { it.getId() })
    assertEquals(listOf(2), tree.first().getChildren().map { it.getId() })
  }

  @Test
  fun `tree2List should flatten preorder and clear built tree`() {
    val left = PropertyNode(2, 1)
    val right = PropertyNode(3, 1)
    val root = PropertyNode(1, null, mutableListOf(left, right))

    val flat = List2TreeUtil.tree2List(
      treeData = listOf(root),
      childrenProperty = PropertyNode::children
    )

    assertEquals(listOf(1, 2, 3), flat.map { it.id })
    assertTrue(root.children.isNullOrEmpty())
  }

  @Test
  fun `tree2List should handle deep chain without recursion`() {
    var current: PropertyNode? = null
    for (id in 5000 downTo 1) {
      val node = PropertyNode(id, if (id == 1) null else id - 1)
      if (current != null) {
        node.children = mutableListOf(current)
      }
      current = node
    }

    val root = assertNotNull(current)
    val flat = List2TreeUtil.tree2List(
      treeData = listOf(root),
      childrenProperty = PropertyNode::children
    )

    assertEquals(5000, flat.size)
    assertEquals(1, flat.first().id)
    assertEquals(5000, flat.last().id)
    assertTrue(root.children.isNullOrEmpty())
  }

  @Test
  fun `breadcrumb overloads should preserve root to target order`() {
    val root = PropertyNode(1, null)
    val mid = PropertyNode(2, 1)
    val leaf = PropertyNode(3, 2)
    val allNodes = listOf(root, mid, leaf)

    val flatPath = List2TreeUtil.getBreadcrumbList(
      list = allNodes,
      targetId = 3,
      getId = PropertyNode::id,
      getParentId = PropertyNode::parentId
    )
    val treePath = List2TreeUtil.getBreadcrumbList(
      list = allNodes,
      targetId = 3,
      getId = PropertyNode::id,
      getParentId = PropertyNode::parentId,
      childrenProperty = PropertyNode::children
    )

    assertEquals(listOf(1, 2, 3), flatPath.map { it.id })
    assertEquals(listOf(1), treePath.map { it.id })
    assertEquals(listOf(2), treePath.first().children?.map { it.id })
    assertEquals(listOf(3), treePath.first().children?.first()?.children?.map { it.id })
  }

  private data class PropertyNode(
    val id: Int,
    val parentId: Int?,
    var children: MutableList<PropertyNode>? = null
  )

  private class MethodNode(
    private val id: Int,
    private val parentId: Int?,
    private var children: MutableList<MethodNode> = mutableListOf()
  ) {
    fun getId(): Int {
      return id
    }

    fun getParentId(): Int? {
      return parentId
    }

    fun getChildren(): MutableList<MethodNode> {
      return children
    }

    fun setChildren(children: MutableList<MethodNode>) {
      this.children = children
    }
  }
}
