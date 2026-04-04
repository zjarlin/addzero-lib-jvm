package site.addzero.util.data_structure.tree

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class List2TreeUtilJvmInteropTest {

  @Test
  fun `java method references should compile and run`() {
    val tree = List2TreeUtilJavaInteropSmoke.buildTree(List2TreeUtilJavaInteropSmoke.sampleNodes())

    assertEquals(listOf(1), tree.map { it.id })
    assertEquals(listOf(2, 3), tree.first().children.map { it.id })

    val flat = List2TreeUtilJavaInteropSmoke.flattenTree(tree)
    assertEquals(listOf(1, 2, 3), flat.map { it.id })
    assertTrue(tree.first().children.isEmpty())
  }
}
