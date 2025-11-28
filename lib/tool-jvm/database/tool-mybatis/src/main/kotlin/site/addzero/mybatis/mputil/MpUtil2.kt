@file:JvmName("MpUtil2")
package site.addzero.mybatis.mputil

import com.baomidou.mybatisplus.core.toolkit.support.SFunction
import java.io.Serializable
import java.util.function.BiConsumer
import java.util.function.Function

/**
 * 2表级联操作
 * p是主表c是子表
 *
 * @author zjarlin
 * @since 2023/2/26 09:18
 */

fun <P : Any, C : Any> pSave2(po: P, collection: MutableCollection<C>, consumer: BiConsumer<C, P>) {
    val ps = getService(po)
    ps.save(po)
    cSave2(po, collection, consumer)
}

fun <P : Any, C : Any> pSave2(
    po: P,
    collection: MutableCollection<C>,
    pgetIdFun: Function<P, String>,
    csetPidCon: BiConsumer<C, String>
) {
    val consumer = BiConsumer { c: C, p: P -> csetPidCon.accept(c, pgetIdFun.apply(po)) }
    val ps = getService(po)
    ps.save(po)
    cSave2(po, collection, consumer)
}

private fun <P : Any, C : Any> cSave2(po: P, collection: MutableCollection<C>, consumer: BiConsumer<C, P>) {
    collection.forEach { c -> consumer.accept(c, po) }
    val cs = getService(collection)
    cs.saveBatch(collection)
}

fun <P : Any, C : Any> pRemove2(pClass: Class<P>, id: Serializable, cgetPidFun: SFunction<C, String>, cClass: Class<C>) {
    cRemove2(id, cgetPidFun, cClass)
    val ps = getServiceByClass(pClass)
    ps.removeById(id)
}

private fun <C : Any> cRemove2(id: Serializable, cgetPidFun: SFunction<C, String>, cClass: Class<C>): Boolean {
    val cs = getServiceByClass(cClass)
    return cs.lambdaUpdate().eq(cgetPidFun, id).remove()
}

fun <P : Any, C : Any> pRemoveBatch2(
    ids: MutableCollection<Serializable>,
    pgetIdFun: SFunction<P, String>,
    cgetPidFun: SFunction<C, String>,
    pClass: Class<P>,
    cClass: Class<C>
) {
    val ps = getServiceByClass(pClass)
    val cs = getServiceByClass(cClass)
    cs.lambdaUpdate().`in`(cgetPidFun, ids).remove()
    ps.lambdaUpdate().`in`(pgetIdFun, ids).remove()
}

fun <P : Any, C : Any> pUpdate2(
    po: P,
    collection: MutableCollection<C>,
    pgetIdFun: SFunction<P, String>,
    cgetPidFun: SFunction<C, String>,
    consumer: BiConsumer<C, P>
) {
    val ps = getService(po)
    ps.updateById(po)
    val pid: String = pgetIdFun.apply(po)
    val cs = getService(collection)
    cs.lambdaUpdate().eq(cgetPidFun, pid).remove()
    cSave2(po, collection, consumer)
}
