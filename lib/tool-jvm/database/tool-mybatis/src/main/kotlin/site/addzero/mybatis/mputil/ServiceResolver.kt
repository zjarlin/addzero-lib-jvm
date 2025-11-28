@file:JvmName("ServiceResolver")
package site.addzero.mybatis.mputil

import cn.hutool.core.annotation.AnnotationUtil
import cn.hutool.core.util.ReflectUtil
import com.baomidou.mybatisplus.core.toolkit.Wrappers
import com.baomidou.mybatisplus.core.toolkit.support.SFunction
import com.baomidou.mybatisplus.extension.service.IService
import site.addzero.mybatis.auto_wrapper.AutoWhereUtil
import site.addzero.mybatis.auto_wrapper.Where
import site.addzero.util.spring.getBean
import java.io.Serializable
import java.lang.reflect.Field

/**
 * IService 推导与 EntityOps 构建工具
 * @author zjarlin
 */

@Suppress("UNCHECKED_CAST")
internal fun <T : Any> getService(entity: T): IService<T> {
    val javaClass = entity.javaClass
    return getBean(IService::class.java, javaClass) as? IService<T>
        ?: throw RuntimeException("未找到 ${javaClass.simpleName} 对应的 IService")
}

@Suppress("UNCHECKED_CAST")
internal fun <T : Any> getService(collection: MutableCollection<T>): IService<T> {
    val first = collection.firstOrNull() ?: throw RuntimeException("集合为空，无法推导 IService")
    return getService(first)
}

@Suppress("UNCHECKED_CAST")
internal fun <T : Any> getServiceByClass(clazz: Class<T>): IService<T> {
    return getBean(IService::class.java, clazz) as? IService<T>
        ?: throw RuntimeException("未找到 ${clazz.simpleName} 对应的 IService")
}

/**
 * 从 IService 构建 EntityOps（MP 特化）
 */
fun <T : Any> IService<T>.toEntityOps(): EntityOps<T> {
    val service = this
    val clazz = service.entityClass
    val byAnno = isByAnno(clazz)

    return object : EntityOps<T> {
        override val entityClass: Class<T> = clazz

        override fun listBy(entity: T): List<T> {
            val wrapper = if (byAnno) AutoWhereUtil.lambdaQueryByAnnotation(clazz, entity)
            else AutoWhereUtil.lambdaQueryByField(clazz, entity, true)
            return service.list(wrapper) ?: emptyList()
        }

        override fun countBy(entity: T): Long {
            val wrapper = if (byAnno) AutoWhereUtil.lambdaQueryByAnnotation(clazz, entity)
            else AutoWhereUtil.lambdaQueryByField(clazz, entity, true)
            return service.count(wrapper)
        }

        override fun countAll() = service.count()
        override fun saveBatch(entities: Collection<T>) = service.saveBatch(entities)
        override fun updateBatchById(entities: Collection<T>) = service.updateBatchById(entities)
        override fun save(entity: T) = service.save(entity)
        override fun updateById(entity: T) = service.updateById(entity)
        override fun removeById(id: Serializable) = service.removeById(id)
        override fun removeByIds(ids: Collection<Serializable>) = service.removeByIds(ids)

        override fun removeByCondition(entity: T): Boolean {
            val wrapper = if (byAnno) AutoWhereUtil.lambdaQueryByAnnotation(clazz, entity)
            else AutoWhereUtil.lambdaQueryByField(clazz, entity, true)
            return service.remove(wrapper)
        }
    }
}

/**
 * 从 IService 构建 ChildTableDef（MP 特化）
 * @param cs 子表 IService
 * @param cGetPidFun 获取子表外键的函数引用
 * @param cSetPidFun 设置子表外键的函数
 */
fun <C : Any> IService<C>.toChildTableDef(
    cGetPidFun: SFunction<C, String>,
    cSetPidFun: (C, String) -> Unit
): ChildTableDef<C> {
    val cs = this
    return object : ChildTableDef<C> {
        override val ops = cs.toEntityOps()
        override fun setParentId(child: C, parentId: String) = cSetPidFun(child, parentId)
        override fun removeByParentId(parentId: Serializable) = cs.lambdaUpdate().eq(cGetPidFun, parentId).remove()
        override fun removeByParentIds(parentIds: Collection<Serializable>) = cs.lambdaUpdate().`in`(cGetPidFun, parentIds).remove()
    }
}

/**
 * 从 IService 构建 CascadeOps（MP 特化）
 */
fun <P : Any> IService<P>.toCascadeOps(
    getParentId: (P) -> String,
    vararg children: ChildTableDef<*>
): CascadeOps<P> {
    return CascadeOps.of(this.toEntityOps(), getParentId, *children)
}

private fun <T> isByAnno(entityClass: Class<T>): Boolean {
    val fields: Array<Field> = ReflectUtil.getFields(entityClass)
    return fields.any { AnnotationUtil.hasAnnotation(it, Where::class.java) }
}

/**
 * 从实体自动推导 EntityOps（MP 特化便捷方法）
 */
internal fun <T : Any> getEntityOps(entity: T): EntityOps<T> = getService(entity).toEntityOps()

internal fun <T : Any> getEntityOps(collection: MutableCollection<T>): EntityOps<T> = getService(collection).toEntityOps()

internal fun <T : Any> getEntityOpsByClass(clazz: Class<T>): EntityOps<T> = getServiceByClass(clazz).toEntityOps()
