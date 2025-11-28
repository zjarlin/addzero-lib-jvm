@file:JvmName("MpUtil")
package site.addzero.mybatis.mputil

import cn.hutool.core.annotation.AnnotationUtil
import cn.hutool.core.bean.BeanUtil.copyProperties
import cn.hutool.core.bean.copier.CopyOptions
import cn.hutool.core.util.NumberUtil
import cn.hutool.core.util.ReflectUtil
import com.baomidou.mybatisplus.extension.service.IService
import site.addzero.mybatis.auto_wrapper.AutoWhereUtil
import site.addzero.mybatis.auto_wrapper.Where
import java.lang.reflect.Field

/**
 * 级联操作工具
 * @author zjarlin
 * @since 2023/2/26 09:18
 */

var idName: String = "id"

private fun <P : Any> isByAnno(ps: IService<P>): Boolean {
    val entityClass = ps.entityClass
    val fields: Array<Field> = ReflectUtil.getFields(entityClass)
    return fields.any { AnnotationUtil.hasAnnotation(it, Where::class.java) }
}

/**
 * 前端和数据库的差集和交集
 */
fun <P : Any> diffAndInter(collection: MutableCollection<P>): DiffAndInterResult<P> {
    if (collection.isEmpty()) return DiffAndInterResult.empty()
    val ps = getService(collection)
    val byAnno = isByAnno(ps)
    return diffAndInter(collection, byAnno, ps)
}

fun <P : Any> diffAndInter(collection: MutableCollection<P>, byAnno: Boolean, ps: IService<P>): DiffAndInterResult<P> {
    val ret = DiffAndInterResult<P>(mutableListOf(), mutableListOf())
    if (collection.isEmpty()) return ret

    val aClass = ps.entityClass

    collection.forEach { e: P ->
        val pLambdaQueryWrapper =
            if (byAnno) AutoWhereUtil.lambdaQueryByAnnotation(aClass, e)
            else AutoWhereUtil.lambdaQueryByField(aClass, e, true)
        val list = ps.list(pLambdaQueryWrapper)
        if (list.isNullOrEmpty()) {
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
 * 前端和数据库的差集和交集（含配对）
 */
fun <P : Any> diffPairAndInter(collection: MutableCollection<P>): DiffPairAndInterResult<P> {
    if (collection.isEmpty()) return DiffPairAndInterResult.empty()
    val ps = getService(collection)
    val byAnno = isByAnno(ps)
    return diffPairAndInter(collection, byAnno, ps)
}

fun <P : Any> diffPairAndInter(collection: MutableCollection<P>, byAnno: Boolean, ps: IService<P>): DiffPairAndInterResult<P> {
    val ret = DiffPairAndInterResult<P>(mutableListOf(), mutableListOf())
    if (collection.isEmpty()) return ret

    val aClass = ps.entityClass

    collection.forEach { e: P ->
        val pLambdaQueryWrapper =
            if (byAnno) AutoWhereUtil.lambdaQueryByAnnotation(aClass, e)
            else AutoWhereUtil.lambdaQueryByField(aClass, e, true)
        val list = ps.list(pLambdaQueryWrapper)
        if (list.isNullOrEmpty()) {
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

fun <P : Any> voidcompareSaveOrUpdate(p: P): P? {
    val list = mutableListOf(p)
    val result = voidcompareSaveOrUpdate(list)
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

fun <P : Any> voidcompareSaveOrUpdate(collection: MutableCollection<P>): CompareSaveOrUpdateResult<P> {
    if (collection.isEmpty()) return CompareSaveOrUpdateResult.empty()

    val ps = getService(collection)
    val diffAndInterResult = diffAndInter(collection)

    val diff = diffAndInterResult.diff

    var insertSuccess = false
    if (diff.isNotEmpty()) {
        diff.forEach {
            val javaClass = it.javaClass
            val field = ReflectUtil.getField(javaClass, idName)
            field?.isAccessible = true
            ReflectUtil.setFieldValue(it, field, null)
        }
        insertSuccess = ps.saveBatch(diff)
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
        updateSuccess = ps.updateBatchById(inter)
    } else {
        val count = ps.count()
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
 * 只要数据库不存在的
 */
fun <P : Any> checkExists(collection: MutableCollection<P>): MutableCollection<P> {
    if (collection.isEmpty()) return mutableListOf()
    val ps = getService(collection)
    val byAnno = isByAnno(ps)
    return checkExists(collection, byAnno, ps)
}

fun <P : Any> checkExists(collection: MutableCollection<P>, byAnno: Boolean, ps: IService<P>): MutableCollection<P> {
    if (collection.isEmpty()) return mutableListOf()

    val aClass = ps.entityClass
    return collection.filter {
        val pLambdaQueryWrapper =
            if (byAnno) AutoWhereUtil.lambdaQueryByAnnotation(aClass, it)
            else AutoWhereUtil.lambdaQueryByField(aClass, it, true)
        val count = ps.count(pLambdaQueryWrapper)
        count <= 0
    }.toMutableSet()
}
