@file:JvmName("CascadeUtil")
package site.addzero.mybatis.mputil

import java.io.Serializable

/**
 * N 表级联操作工具 - 支持任意数量子表
 * @author zjarlin
 * @since 2023/2/26 09:18
 */

// ==================== 核心逻辑（通用） ====================

/**
 * 级联保存 - 保存父表并级联保存所有子表
 */
fun <P : Any> cascadeSave(
    parent: P,
    cascadeOps: CascadeOps<P>,
    childDataList: List<ChildData<*>>
): Boolean {
    val saveParent = cascadeOps.parentOps.save(parent)
    if (!saveParent) return false

    val parentId = cascadeOps.getParentId(parent)
    return saveChildren(parentId, childDataList)
}

/**
 * 保存所有子表数据
 */
@Suppress("UNCHECKED_CAST")
private fun saveChildren(parentId: String, childDataList: List<ChildData<*>>): Boolean {
    return childDataList.all { childData ->
        val def = childData.def as ChildTableDef<Any>
        val entities = childData.entities as MutableCollection<Any>
        entities.forEach { def.setParentId(it, parentId) }
        def.ops.saveBatch(entities)
    }
}

/**
 * 级联删除 - 删除父表并级联删除所有子表
 */
fun <P : Any> cascadeRemove(
    parentId: Serializable,
    cascadeOps: CascadeOps<P>
): Boolean {
    val removeChildren = cascadeOps.children.all { it.removeByParentId(parentId) }
    val removeParent = cascadeOps.parentOps.removeById(parentId)
    return removeChildren && removeParent
}

/**
 * 级联批量删除
 */
fun <P : Any> cascadeRemoveBatch(
    parentIds: Collection<Serializable>,
    cascadeOps: CascadeOps<P>
): Boolean {
    val removeChildren = cascadeOps.children.all { it.removeByParentIds(parentIds) }
    val removeParent = cascadeOps.parentOps.removeByIds(parentIds)
    return removeChildren && removeParent
}

/**
 * 级联更新 - 更新父表，删除旧子表数据，保存新子表数据
 */
fun <P : Any> cascadeUpdate(
    parent: P,
    cascadeOps: CascadeOps<P>,
    childDataList: List<ChildData<*>>
): Boolean {
    val updateParent = cascadeOps.parentOps.updateById(parent)
    if (!updateParent) return false

    val parentId = cascadeOps.getParentId(parent)

    // 删除旧的子表数据
    val removeOld = childDataList.all { childData ->
        childData.def.removeByParentId(parentId)
    }

    // 保存新的子表数据
    return removeOld && saveChildren(parentId, childDataList)
}

// ==================== 便捷构建方法 ====================

/**
 * 构建子表数据（Kotlin DSL 风格）
 */
inline fun <reified C> ChildTableDef<C>.withData(entities: MutableCollection<C>): ChildData<C> {
    return ChildData(this, entities)
}

/**
 * 批量构建子表数据
 */
fun childDataOf(vararg data: ChildData<*>): List<ChildData<*>> = data.toList()

// ==================== MP 特化便捷方法 ====================

/**
 * 级联保存（MP 特化，自动推导 IService）
 */
fun <P : Any> cascadeSave(
    parent: P,
    getParentId: (P) -> String,
    childDataList: List<ChildData<*>>
): Boolean {
    val cascadeOps = CascadeOps.of(
        parentOps = getEntityOps(parent),
        getParentId = getParentId,
        children = childDataList.map { it.def }
    )
    return cascadeSave(parent, cascadeOps, childDataList)
}

/**
 * 级联更新（MP 特化）
 */
fun <P : Any> cascadeUpdate(
    parent: P,
    getParentId: (P) -> String,
    childDataList: List<ChildData<*>>
): Boolean {
    val cascadeOps = CascadeOps.of(
        parentOps = getEntityOps(parent),
        getParentId = getParentId,
        children = childDataList.map { it.def }
    )
    return cascadeUpdate(parent, cascadeOps, childDataList)
}
