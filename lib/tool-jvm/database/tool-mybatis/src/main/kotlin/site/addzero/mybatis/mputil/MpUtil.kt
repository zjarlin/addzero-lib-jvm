@file:JvmName("MpUtil")
package site.addzero.mybatis.mputil

import cn.hutool.core.bean.BeanUtil.copyProperties
import cn.hutool.core.bean.copier.CopyOptions
import cn.hutool.core.util.NumberUtil
import cn.hutool.core.util.ReflectUtil

/**
 * 级联操作工具 - 通用版本（不依赖具体 ORM）
 * @author zjarlin
 * @since 2023/2/26 09:18
 */

var idName: String = "id"

// ==================== 核心逻辑（通用，接收 EntityOps） ====================

/**
 * 前端和数据库的差集和交集（通用版本）
 */
fun <P : Any> diffAndInter(collection: MutableCollection<P>, ops: EntityOps<P>): DiffAndInterResult<P> {
    val ret = DiffAndInterResult<P>(mutableListOf(), mutableListOf())
    if (collection.isEmpty()) return ret

    collection.forEach { e: P ->
        val list = ops.listBy(e)
        if (list.isEmpty()) {
            ret.diff.add(e)
        } else {
            val copyOptions: CopyOptions = CopyOptions.create()
            copyOptions.setPropertiesFilter { field, value ->
                val notId = idName != field.name
                val notEmpty = value != null && value.toString().isNotEmpty()
                notId && notEmpty
            }
            list.forEach { copyProperties(e, it, copyOptions) }
            ret.inter.addAll(list)
        }
    }
    return ret
}

/**
 * 前端和数据库的差集和交集 - 含配对（通用版本）
 */
fun <P : Any> diffPairAndInter(collection: MutableCollection<P>, ops: EntityOps<P>): DiffPairAndInterResult<P> {
    val ret = DiffPairAndInterResult<P>(mutableListOf(), mutableListOf())
    if (collection.isEmpty()) return ret

    collection.forEach { e: P ->
        val list = ops.listBy(e)
        if (list.isEmpty()) {
            ret.diff.add(e)
        } else {
            val copyOptions = CopyOptions.create()
            copyOptions.setPropertiesFilter { field, value ->
                val notId = idName != field.name
                val notEmpty = value != null && value.toString().isNotEmpty()
                notId && notEmpty
            }
            val collect = list.map { sjk -> InterPair(e, sjk) }
            ret.interPairs.addAll(collect)
        }
    }
    return ret
}

/**
 * 比较并保存或更新（通用版本）
 */
fun <P : Any> compareSaveOrUpdate(p: P, ops: EntityOps<P>): P? {
    val list = mutableListOf(p)
    val result = compareSaveOrUpdate(list, ops)
    val toUpdate = result.toUpdate
    if (!toUpdate.isNullOrEmpty()) {
        return toUpdate[0]
    }
    val toInsert = result.toInsert
    if (!toInsert.isNullOrEmpty()) {
        return toInsert[0]
    }
    return p
}

fun <P : Any> compareSaveOrUpdate(collection: MutableCollection<P>, ops: EntityOps<P>): CompareSaveOrUpdateResult<P> {
    if (collection.isEmpty()) return CompareSaveOrUpdateResult.empty()

    val diffAndInterResult = diffAndInter(collection, ops)
    val diff = diffAndInterResult.diff

    var insertSuccess = false
    if (diff.isNotEmpty()) {
        diff.forEach {
            val javaClass = it.javaClass
            val field = ReflectUtil.getField(javaClass, idName)
            field?.isAccessible = true
            ReflectUtil.setFieldValue(it, field, null)
        }
        insertSuccess = ops.saveBatch(diff)
    }

    val inter = diffAndInterResult.inter

    if (inter.isEmpty()) {
        return CompareSaveOrUpdateResult(
            toInsert = diff,
            toUpdate = mutableListOf(),
            insertSuccess = insertSuccess,
            updateSuccess = true
        )
    }

    var updateSuccess = false
    val interSize = inter.size
    val voSize = collection.size

    if (interSize <= voSize) {
        updateSuccess = ops.updateBatchById(inter)
    } else {
        val count = ops.countAll()
        val equals: Boolean = NumberUtil.equals(count, interSize)
        if (equals) {
            throw RuntimeException("请检查唯一性校验注解,查询出交集应当修改的行数大于输入的行数,可能会误修改数据,因此中断更新!")
        }
    }

    return CompareSaveOrUpdateResult(
        toInsert = diff,
        toUpdate = inter,
        insertSuccess = insertSuccess,
        updateSuccess = updateSuccess
    )
}

/**
 * 只要数据库不存在的（通用版本）
 */
fun <P : Any> checkExists(collection: MutableCollection<P>, ops: EntityOps<P>): MutableCollection<P> {
    if (collection.isEmpty()) return mutableListOf()
    return collection.filter { ops.countBy(it) <= 0 }.toMutableSet()
}

// ==================== MP 特化便捷方法（自动推导 IService） ====================

/**
 * 前端和数据库的差集和交集（MP 特化）
 */
fun <P : Any> diffAndInter(collection: MutableCollection<P>): DiffAndInterResult<P> {
    if (collection.isEmpty()) return DiffAndInterResult.empty()
    return diffAndInter(collection, getEntityOps(collection))
}

/**
 * 前端和数据库的差集和交集 - 含配对（MP 特化）
 */
fun <P : Any> diffPairAndInter(collection: MutableCollection<P>): DiffPairAndInterResult<P> {
    if (collection.isEmpty()) return DiffPairAndInterResult.empty()
    return diffPairAndInter(collection, getEntityOps(collection))
}

/**
 * 比较并保存或更新（MP 特化）
 */
fun <P : Any> voidcompareSaveOrUpdate(p: P): P? = compareSaveOrUpdate(p, getEntityOps(p))

fun <P : Any> voidcompareSaveOrUpdate(collection: MutableCollection<P>): CompareSaveOrUpdateResult<P> {
    if (collection.isEmpty()) return CompareSaveOrUpdateResult.empty()
    return compareSaveOrUpdate(collection, getEntityOps(collection))
}

/**
 * 只要数据库不存在的（MP 特化）
 */
fun <P : Any> checkExists(collection: MutableCollection<P>): MutableCollection<P> {
    if (collection.isEmpty()) return mutableListOf()
    return checkExists(collection, getEntityOps(collection))
}
