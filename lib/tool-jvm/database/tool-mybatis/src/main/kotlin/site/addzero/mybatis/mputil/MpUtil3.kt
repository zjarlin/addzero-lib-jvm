@file:JvmName("MpUtil3")
package site.addzero.mybatis.mputil

import com.baomidou.mybatisplus.core.toolkit.Wrappers
import com.baomidou.mybatisplus.core.toolkit.support.SFunction
import java.io.Serializable
import java.util.function.BiConsumer
import java.util.function.Function

/**
 * 3表级联操作
 * p是主表c是子表
 *
 * @author zjarlin
 * @since 2023/2/26 09:18
 */

fun <P : Any, C : Any, C1 : Any> pSave3(
    p: P,
    childColl: MutableList<C>,
    childColl1: MutableList<C1>,
    getPidFun: Function<P, String>,
    cSetPkFun: BiConsumer<C, String>,
    c1SetPkFun: BiConsumer<C1, String>
): Boolean {
    val tConsumer = BiConsumer { child: C, par: P -> cSetPkFun.accept(child, getPidFun.apply(par)) }
    val tConsumer1 = BiConsumer { child1: C1, par: P -> c1SetPkFun.accept(child1, getPidFun.apply(par)) }
    return try {
        pSave3(p, childColl, childColl1, tConsumer, tConsumer1)
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

/**
 * 级联新增
 */
fun <P : Any, C : Any, C1 : Any> pSave3(
    p: P,
    childColl: MutableCollection<C>,
    childColl1: MutableCollection<C1>,
    cSetFun: BiConsumer<C, P>,
    c1SetFun: BiConsumer<C1, P>
): Boolean {
    val ps = getService(p)
    val save = ps.save(p)
    return save && cSave3(p, childColl, childColl1, cSetFun, c1SetFun)
}

/**
 * 子表新增
 */
fun <P : Any, C : Any, C1 : Any> cSave3(
    p: P,
    childColl: MutableCollection<C>,
    childColl1: MutableCollection<C1>,
    setFun: BiConsumer<C, P>,
    c1SetFun: BiConsumer<C1, P>
): Boolean {
    childColl.forEach { child -> setFun.accept(child, p) }
    childColl1.forEach { child -> c1SetFun.accept(child, p) }
    val cs = getService(childColl)
    val cs1 = getService(childColl1)
    val b = cs.saveBatch(childColl)
    val b1 = cs1.saveBatch(childColl1)
    return b && b1
}

fun <P : Any, C : Any, C1 : Any> pRemove3(
    pid: Serializable,
    pClass: Class<P>,
    cGetPidFun: SFunction<C, String>,
    cClass: Class<C>,
    c1GetPidFun: SFunction<C1, String>,
    c1Class: Class<C1>
): Boolean {
    val ps = getServiceByClass(pClass)
    val b = ps.removeById(pid)
    val b1 = cRemove3(pid, cGetPidFun, cClass, c1GetPidFun, c1Class)
    return b && b1
}

fun <P : Any, C : Any, C1 : Any> pRemoveBatch3(
    pid: MutableCollection<out Serializable>,
    pClass: Class<P>,
    cGetPidFun: SFunction<C, String>,
    cClass: Class<C>,
    c1GetPidFun: SFunction<C1, String>,
    c1Class: Class<C1>
): Boolean {
    val ps = getServiceByClass(pClass)
    val b1 = ps.removeByIds(pid)
    val b = cRemoveBatch3(pid, cGetPidFun, cClass, c1GetPidFun, c1Class)
    return b1 && b
}

fun <C : Any, C1 : Any> cRemove3(
    pid: Serializable,
    cGetPidFun: SFunction<C, String>,
    cClass: Class<C>,
    c1GetPidFun: SFunction<C1, String>,
    c1Class: Class<C1>
): Boolean {
    val cs = getServiceByClass(cClass)
    val cs1 = getServiceByClass(c1Class)
    val eq = Wrappers.lambdaQuery<C>().eq(cGetPidFun, pid)
    val eq1 = Wrappers.lambdaQuery<C1>().eq(c1GetPidFun, pid)
    val remove = cs.remove(eq)
    val remove1 = cs1.remove(eq1)
    return remove && remove1
}

fun <C : Any, C1 : Any> cRemoveBatch3(
    pid: MutableCollection<out Serializable>,
    cGetPidFun: SFunction<C, String>,
    cClass: Class<C>,
    c1GetPidFun: SFunction<C1, String>,
    c1Class: Class<C1>
): Boolean {
    val cs = getServiceByClass(cClass)
    val cs1 = getServiceByClass(c1Class)
    val remove2 = cs.lambdaUpdate().`in`(cGetPidFun, pid).remove()
    val remove3 = cs1.lambdaUpdate().`in`(c1GetPidFun, pid).remove()
    return remove2 && remove3
}

fun <P : Any, C : Any, C1 : Any> pUpdate3(
    p: P,
    childColl: MutableCollection<C>,
    childColl1: MutableCollection<C1>,
    pgetPidFun: Function<P, String>,
    cgetPidFun: SFunction<C, String>,
    c1GetPidFun: SFunction<C1, String>,
    csetPkFun: BiConsumer<C, String>,
    c1SetPkFun: BiConsumer<C1, String>
): Boolean {
    val ps = getService(p)
    val cs = getService(childColl)
    val cs1 = getService(childColl1)
    val b = ps.updateById(p)
    val apply = pgetPidFun.apply(p)
    val eq = Wrappers.lambdaQuery<C>().eq(cgetPidFun, apply)
    val eq1 = Wrappers.lambdaQuery<C1>().eq(c1GetPidFun, apply)
    val b2 = cs.remove(eq) && cs1.remove(eq1)
    val b3 = cSave3ByFun(p, childColl, childColl1, pgetPidFun, csetPkFun, c1SetPkFun)
    return b && b2 && b3
}

/**
 * c保存
 */
fun <P : Any, C : Any, C1 : Any> cSave3ByFun(
    p: P,
    childColl: MutableCollection<C>,
    childColl1: MutableCollection<C1>,
    getPidFun: Function<P, String>,
    setPkFun: BiConsumer<C, String>,
    c1SetPkFun: BiConsumer<C1, String>
): Boolean {
    val tConsumer = BiConsumer { child: C, par: P -> setPkFun.accept(child, getPidFun.apply(par)) }
    val t1Consumer = BiConsumer { child: C1, par: P -> c1SetPkFun.accept(child, getPidFun.apply(par)) }
    return cSave3(p, childColl, childColl1, tConsumer, t1Consumer)
}
