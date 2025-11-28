package site.addzero.mybatis.mputil

import java.io.Serializable

/**
 * 实体操作抽象 - 解耦具体 ORM 实现
 * Java 用户可实现此接口对接任意持久层框架
 * @author zjarlin
 */
interface EntityOps<T> {
    val entityClass: Class<T>
    fun listBy(entity: T): List<T>
    fun countBy(entity: T): Long
    fun countAll(): Long
    fun saveBatch(entities: Collection<T>): Boolean
    fun updateBatchById(entities: Collection<T>): Boolean
    fun save(entity: T): Boolean
    fun updateById(entity: T): Boolean
    fun removeById(id: Serializable): Boolean
    fun removeByIds(ids: Collection<Serializable>): Boolean
    fun removeByCondition(entity: T): Boolean

    companion object {
        @JvmStatic
        fun <T> of(
            entityClass: Class<T>,
            listBy: (T) -> List<T>,
            countBy: (T) -> Long,
            countAll: () -> Long,
            saveBatch: (Collection<T>) -> Boolean,
            updateBatchById: (Collection<T>) -> Boolean,
            save: (T) -> Boolean,
            updateById: (T) -> Boolean,
            removeById: (Serializable) -> Boolean,
            removeByIds: (Collection<Serializable>) -> Boolean,
            removeByCondition: (T) -> Boolean
        ): EntityOps<T> = object : EntityOps<T> {
            override val entityClass: Class<T> = entityClass
            override fun listBy(entity: T) = listBy(entity)
            override fun countBy(entity: T) = countBy(entity)
            override fun countAll() = countAll()
            override fun saveBatch(entities: Collection<T>) = saveBatch(entities)
            override fun updateBatchById(entities: Collection<T>) = updateBatchById(entities)
            override fun save(entity: T) = save(entity)
            override fun updateById(entity: T) = updateById(entity)
            override fun removeById(id: Serializable) = removeById(id)
            override fun removeByIds(ids: Collection<Serializable>) = removeByIds(ids)
            override fun removeByCondition(entity: T) = removeByCondition(entity)
        }
    }
}

/**
 * 子表定义 - 描述一个子表与父表的关联关系
 * @param C 子表实体类型
 */
interface ChildTableDef<C> {
    val ops: EntityOps<C>
    fun setParentId(child: C, parentId: String)
    fun removeByParentId(parentId: Serializable): Boolean
    fun removeByParentIds(parentIds: Collection<Serializable>): Boolean

    companion object {
        @JvmStatic
        fun <C> of(
            ops: EntityOps<C>,
            setParentId: (C, String) -> Unit,
            removeByParentId: (Serializable) -> Boolean,
            removeByParentIds: (Collection<Serializable>) -> Boolean
        ): ChildTableDef<C> = object : ChildTableDef<C> {
            override val ops = ops
            override fun setParentId(child: C, parentId: String) = setParentId(child, parentId)
            override fun removeByParentId(parentId: Serializable) = removeByParentId(parentId)
            override fun removeByParentIds(parentIds: Collection<Serializable>) = removeByParentIds(parentIds)
        }
    }
}

/**
 * N 表级联操作抽象 - 支持任意数量子表
 * @param P 父表实体类型
 */
interface CascadeOps<P> {
    val parentOps: EntityOps<P>
    val children: List<ChildTableDef<*>>
    fun getParentId(parent: P): String

    companion object {
        @JvmStatic
        fun <P> of(
            parentOps: EntityOps<P>,
            getParentId: (P) -> String,
            vararg children: ChildTableDef<*>
        ): CascadeOps<P> = object : CascadeOps<P> {
            override val parentOps = parentOps
            override val children = children.toList()
            override fun getParentId(parent: P) = getParentId(parent)
        }

        @JvmStatic
        fun <P> of(
            parentOps: EntityOps<P>,
            getParentId: (P) -> String,
            children: List<ChildTableDef<*>>
        ): CascadeOps<P> = object : CascadeOps<P> {
            override val parentOps = parentOps
            override val children = children
            override fun getParentId(parent: P) = getParentId(parent)
        }
    }
}

/**
 * 子表数据容器 - 用于级联保存时传递子表数据
 */
data class ChildData<C>(
    val def: ChildTableDef<C>,
    val entities: MutableCollection<C>
)

@Suppress("UNCHECKED_CAST")
fun <C> childData(def: ChildTableDef<C>, entities: MutableCollection<C>): ChildData<*> = ChildData(def, entities)
