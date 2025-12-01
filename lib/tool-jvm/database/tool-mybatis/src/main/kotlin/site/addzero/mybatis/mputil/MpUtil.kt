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

/**
 * MpUtil 配置
 */
object MpUtilConfig {
    @Volatile
    var idName: String = "id"
}

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
                val notId = MpUtilConfig.idName != field.name
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
                val notId = MpUtilConfig.idName != field.name
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
 * @return 操作成功返回实体，失败返回 null
 */
fun <P : Any> compareSaveOrUpdate(p: P, ops: EntityOps<P>): P? {
    val list = mutableListOf(p)
    val result = compareSaveOrUpdate(list, ops)
    if (!result.anySuccess) return null
    return result.toUpdate?.firstOrNull() ?: result.toInsert?.firstOrNull()
}

fun <P : Any> compareSaveOrUpdate(collection: MutableCollection<P>, ops: EntityOps<P>): CompareSaveOrUpdateResult<P> {
    if (collection.isEmpty()) return CompareSaveOrUpdateResult.empty()

    val (withId, withoutId) = collection.partition { entity ->
        val field = ReflectUtil.getField(entity.javaClass, MpUtilConfig.idName)
        field?.isAccessible = true
        val idValue = field?.get(entity)
        idValue != null && idValue.toString().isNotEmpty()
    }

    val toUpdate = mutableListOf<P>()
    val toInsert = mutableListOf<P>()
    var insertSuccess = true
    var updateSuccess = true

    if (withId.isNotEmpty()) {
        toUpdate.addAll(withId)
        updateSuccess = ops.updateBatchById(withId)
    }

    if (withoutId.isNotEmpty()) {
        val diffAndInterResult = diffAndInter(withoutId.toMutableList(), ops)
        val diff = diffAndInterResult.diff
        val inter = diffAndInterResult.inter

        if (diff.isNotEmpty()) {
            diff.forEach {
                val field = ReflectUtil.getField(it.javaClass, MpUtilConfig.idName)
                field?.isAccessible = true
                ReflectUtil.setFieldValue(it, field, null)
            }
            insertSuccess = ops.saveBatch(diff)
            toInsert.addAll(diff)
        }

        if (inter.isNotEmpty()) {
            val interSize = inter.size
            val withoutIdSize = withoutId.size
            if (interSize <= withoutIdSize) {
                val interUpdateSuccess = ops.updateBatchById(inter)
                updateSuccess = updateSuccess && interUpdateSuccess
                toUpdate.addAll(inter)
            } else {
                val count = ops.countAll()
                val equals: Boolean = NumberUtil.equals(count, interSize)
                if (equals) {
                    throw RuntimeException("请检查唯一性校验注解,查询出交集应当修改的行数大于输入的行数,可能会误修改数据,因此中断更新!")
                }
            }
        }
    }

    return CompareSaveOrUpdateResult(
        toInsert = toInsert.ifEmpty { mutableListOf() }.toMutableList(),
        toUpdate = toUpdate.ifEmpty { mutableListOf() }.toMutableList(),
        insertSuccess = insertSuccess,
        updateSuccess = updateSuccess
    )
}

/**
 * 过滤出数据库中不存在的数据（通用版本）
 */
fun <P : Any> filterNotExists(collection: MutableCollection<P>, ops: EntityOps<P>): MutableCollection<P> {
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
fun <P : Any> upsert(p: P): P? = compareSaveOrUpdate(p, getEntityOps(p))

fun <P : Any> upsert(collection: MutableCollection<P>): CompareSaveOrUpdateResult<P> {
    if (collection.isEmpty()) return CompareSaveOrUpdateResult.empty()
    return compareSaveOrUpdate(collection, getEntityOps(collection))
}

/**
 * 过滤出数据库中不存在的数据（MP 特化）
 */
fun <P : Any> filterNotExists(collection: MutableCollection<P>): MutableCollection<P> {
    if (collection.isEmpty()) return mutableListOf()
    return filterNotExists(collection, getEntityOps(collection))
}
