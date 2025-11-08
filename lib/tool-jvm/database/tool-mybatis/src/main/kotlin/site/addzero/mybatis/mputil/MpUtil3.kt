package site.addzero.mybatis.mputil

import com.baomidou.mybatisplus.core.toolkit.Wrappers
import com.baomidou.mybatisplus.core.toolkit.support.SFunction
import com.baomidou.mybatisplus.extension.service.IService
import java.io.Serializable
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Function

/**
 * 3表级联操作
 * p是主表c是子表
 *
 * @author zjarlin
 * @since 2023/2/26 09:18
 */
class MpUtil3<P, C, C1>(

    private val ps: IService<P>,

    private val cs: IService<C>,

    private val cs1: IService<C1>
) {

    fun pSave(
        p: P,
        childColl: MutableList<C>,
        childColl1: MutableList<C1>,
        getPidFun: Function<P, String>,
        cSetPkFun: BiConsumer<C, String>,
        c1SetPkFun: BiConsumer<C1, String>
    ): Boolean {
        val tConsumer = BiConsumer { child: C, par: P -> cSetPkFun.accept(child, getPidFun.apply(par).toString()) }
        val tConsumer1 = BiConsumer { child1: C1, par: P -> c1SetPkFun.accept(child1, getPidFun.apply(par).toString()) }
        val b: Boolean
        try {
            b = pSave(p, childColl, childColl1, tConsumer, tConsumer1)
        } catch (e: Exception) {
            return false
        }
        return b
    }

    /**
     * 级联新增
     *
     * @param p
     * @param childColl  子表
     * @param childColl1 孩子coll1
     * @param cSetFun    c组有趣
     * @param c1SetFun   c1组有趣 入参
     * @return boolean
     * @author zjarlin
     * @since 2023/03/16
     */
    fun pSave(
        p: P,
        childColl: MutableCollection<C>,
        childColl1: MutableCollection<C1>,
        cSetFun: BiConsumer<C, P>,
        c1SetFun: BiConsumer<C1, P>
    ): Boolean {
        val save = ps.save(p)
        return save && cSave(p, childColl, childColl1, cSetFun, c1SetFun)
    }

    /**
     * 子表新增
     *
     * @param p         p
     * @param childColl 孩子科尔
     * @param setFun    设置有趣 入参
     * @return boolean
     * @author zjarlin
     * @since 2023/03/01
     */
    fun cSave(
        p: P,
        childColl: MutableCollection<C>,
        childColl1: MutableCollection<C1>,
        setFun: BiConsumer<C, P>,
        c1SetFun: BiConsumer<C1, P>
    ): Boolean {
        val cConsumer = Consumer { child: C -> setFun.accept(child, p) }
        childColl.forEach(cConsumer)
        val c1Consumer = Consumer { child: C1 -> c1SetFun.accept(child, p) }
        childColl.forEach(cConsumer)
        childColl1.forEach(c1Consumer)
        val b = cs.saveBatch(childColl)
        val b1 = cs1.saveBatch(childColl1)
        return b && b1
    }

    fun pRemove(
        pid: Serializable, cGetPidFun: SFunction<C, String>, c1GetPidFun: SFunction<C1, String>
    ): Boolean {
        val b = ps.removeById(pid)
        val b1 = cRemove(pid, cGetPidFun, c1GetPidFun)
        return b && b1
    }

    fun pRemoveBatch(
        pid: MutableCollection<out Serializable>, cGetPidFun: SFunction<C, String>, c1GetPidFun: SFunction<C1, String>
    ): Boolean {
        val b1 = ps.removeByIds(pid)
        val b = cRemoveBatch(pid, cGetPidFun, c1GetPidFun)
        return b1 && b
    }

    fun cRemove(
        pid: Serializable, cGetPidFun: SFunction<C, String>, c1GetPidFun: SFunction<C1, String>
    ): Boolean {
        val eq = Wrappers.lambdaQuery<C>().eq(cGetPidFun, pid)
        val eq1 = Wrappers.lambdaQuery<C1>().eq(c1GetPidFun, pid)
        val remove = cs.remove(eq)
        val remove1 = cs1.remove(eq1)
        return remove && remove1
    }

    fun cRemoveBatch(
        pid: MutableCollection<out Serializable>, cGetPidFun: SFunction<C, String>, c1GetPidFun: SFunction<C1, String>
    ): Boolean {
        val remove2 = cs.lambdaUpdate().`in`(cGetPidFun, pid).remove()
        val remove3 = cs1.lambdaUpdate().`in`(c1GetPidFun, pid).remove()
        return remove2 && remove3
    }

    fun pUpdate(
        p: P,
        childColl: MutableCollection<C>,
        childColl1: MutableCollection<C1>,
        pgetPidFun: Function<P, String>,
        cgetPidFun: SFunction<C, String>,
        c1GetPidFun: SFunction<C1, String>,
        csetPkFun: BiConsumer<C, String>,
        c1SetPkFun: BiConsumer<C1, String>
    ): Boolean {
        val b = ps.updateById(p)
        val apply = pgetPidFun.apply(p)
        val b2 = cRemove(apply, cgetPidFun, c1GetPidFun)
        val b3 = cSave(p, childColl, childColl1, pgetPidFun, csetPkFun, c1SetPkFun)
        return b && b2 && b3
    }

    /**
     * c保存
     *
     * @param p         p
     * @param childColl 孩子科尔
     * @param getPidFun 得到pid有趣
     * @param setPkFun  组pk有趣 入参
     * @return boolean
     * @author zjarlin
     * @since 2023/03/01
     */
    fun cSave(
        p: P,
        childColl: MutableCollection<C>,
        childColl1: MutableCollection<C1>,
        getPidFun: Function<P, String>,
        setPkFun: BiConsumer<C, String>,
        c1SetPkFun: BiConsumer<C1, String>
    ): Boolean {
        val tConsumer = BiConsumer { child: C, par: P -> setPkFun.accept(child, getPidFun.apply(par)) }
        val t1Consumer = BiConsumer { child: C1, par: P -> c1SetPkFun.accept(child, getPidFun.apply(par)) }
        return cSave(p, childColl, childColl1, tConsumer, t1Consumer)
    }

    companion object {
        fun <P, C, C1> of(ps: IService<P>, cs: IService<C>, cs1: IService<C1>): MpUtil3<P, C, C1> {
            return MpUtil3<P, C, C1>(ps, cs, cs1)
        }
    }
}
