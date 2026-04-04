@file:JvmName("List2TreeJavaUtil")

package site.addzero.util.data_structure.tree

import java.util.function.BiConsumer
import java.util.function.Function
import java.util.function.Predicate

/**
 * Java 方法引用友好的桥接入口。
 */
fun <T> list2Tree(
  source: List<T>,
  idFun: Function<T, Any?>,
  pidFun: Function<T, Any?>,
  getChildFun: Function<T, List<T>?>,
  setChildFun: BiConsumer<T, MutableList<T>>
): List<T> {
  return List2TreeUtil.list2Tree(
    source = source,
    idFun = idFun::apply,
    pidFun = pidFun::apply,
    getChildFun = getChildFun::apply,
    setChildFun = setChildFun::accept
  )
}

/**
 * Java 方法引用友好的桥接入口。
 */
fun <T> list2Tree(
  source: List<T>,
  isRoot: Predicate<T>,
  idFun: Function<T, Any?>,
  pidFun: Function<T, Any?>,
  getChildFun: Function<T, List<T>?>,
  setChildFun: BiConsumer<T, MutableList<T>>
): List<T> {
  return List2TreeUtil.list2Tree(
    source = source,
    isRoot = isRoot::test,
    idFun = idFun::apply,
    pidFun = pidFun::apply,
    getChildFun = getChildFun::apply,
    setChildFun = setChildFun::accept
  )
}

/**
 * Java 方法引用友好的桥接入口。
 */
fun <T> tree2List(
  treeData: List<T>,
  getChildFun: Function<T, List<T>?>,
  setChildFun: BiConsumer<T, MutableList<T>>
): List<T> {
  return List2TreeUtil.tree2List(
    treeData = treeData,
    getChildFun = getChildFun::apply,
    setChildFun = setChildFun::accept
  )
}
