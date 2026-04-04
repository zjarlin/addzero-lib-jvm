package site.addzero.util.data_structure.tree

import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

/**
 * 保留 commonMain 顶层 facade，兼容旧调用点。
 */
fun <T> list2Tree(
  source: List<T>,
  idFun: (T) -> Any?,
  pidFun: (T) -> Any?,
  getChildFun: (T) -> List<T>?,
  setChildFun: (T, MutableList<T>) -> Unit
): List<T> {
  return List2TreeUtil.list2Tree(
    source = source,
    idFun = idFun,
    pidFun = pidFun,
    getChildFun = getChildFun,
    setChildFun = setChildFun
  )
}

/**
 * 保留 commonMain 顶层 facade，兼容旧调用点。
 */
fun <T> list2Tree(
  source: List<T>,
  isRoot: (T) -> Boolean,
  idFun: (T) -> Any?,
  pidFun: (T) -> Any?,
  getChildFun: (T) -> List<T>?,
  setChildFun: (T, MutableList<T>) -> Unit
): List<T> {
  return List2TreeUtil.list2Tree(
    source = source,
    isRoot = isRoot,
    idFun = idFun,
    pidFun = pidFun,
    getChildFun = getChildFun,
    setChildFun = setChildFun
  )
}

/**
 * 保留 commonMain 顶层 facade，兼容旧调用点。
 */
fun <T> list2Tree(
  source: List<T>,
  idProperty: KProperty1<T, Any?>,
  parentIdProperty: KProperty1<T, Any?>,
  childrenProperty: KMutableProperty1<T, MutableList<T>?>
): List<T> {
  return List2TreeUtil.list2Tree(
    source = source,
    idProperty = idProperty,
    parentIdProperty = parentIdProperty,
    childrenProperty = childrenProperty
  )
}

/**
 * 保留 commonMain 顶层 facade，兼容旧调用点。
 */
fun <T> list2Tree(
  source: List<T>,
  isRoot: (T) -> Boolean,
  idProperty: KProperty1<T, Any?>,
  parentIdProperty: KProperty1<T, Any?>,
  childrenProperty: KMutableProperty1<T, MutableList<T>?>
): List<T> {
  return List2TreeUtil.list2Tree(
    source = source,
    isRoot = isRoot,
    idProperty = idProperty,
    parentIdProperty = parentIdProperty,
    childrenProperty = childrenProperty
  )
}

/**
 * 保留 commonMain 顶层 facade，兼容旧调用点。
 */
fun <T> tree2List(
  treeData: List<T>,
  getChildFun: (T) -> List<T>?,
  setChildFun: (T, MutableList<T>) -> Unit
): List<T> {
  return List2TreeUtil.tree2List(
    treeData = treeData,
    getChildFun = getChildFun,
    setChildFun = setChildFun
  )
}

/**
 * 保留 commonMain 顶层 facade，兼容旧调用点。
 */
fun <T> tree2List(
  treeData: List<T>,
  childrenProperty: KMutableProperty1<T, MutableList<T>?>
): List<T> {
  return List2TreeUtil.tree2List(
    treeData = treeData,
    childrenProperty = childrenProperty
  )
}
