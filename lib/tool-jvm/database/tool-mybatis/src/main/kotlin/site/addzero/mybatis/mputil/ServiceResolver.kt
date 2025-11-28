@file:JvmName("ServiceResolver")
package site.addzero.mybatis.mputil

import com.baomidou.mybatisplus.extension.service.IService
import site.addzero.util.spring.getBean

/**
 * IService 推导工具
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
