package site.addzero.mybatis.mputil

import com.baomidou.mybatisplus.core.toolkit.support.SFunction
import com.baomidou.mybatisplus.extension.service.IService
import java.io.Serializable
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Function

/**
 * 2表级联操作
 * p是主表c是子表
 *
 * @author zjarlin
 * @since 2023/2/26 09:18
 */
class MpUtil2<P, C> (private val ps: IService<P>,private val cs: IService<C>) {


    fun pSave(po: P, collection: MutableCollection<C>, consumer: BiConsumer<C, P>) {
        ps.save(po)
        cSave(po, collection, consumer)
    }

    fun pSave(
        po: P,
        collection: MutableCollection<C>,
        pgetIdFun: Function<P, String>,
        csetPidCon: BiConsumer<C, String>
    ) {
        val consumer = BiConsumer { c: C, p: P -> csetPidCon.accept(c, pgetIdFun.apply(po)) }
        ps.save(po)
        cSave(po, collection, consumer)
    }

    private fun cSave(po: P, collection: MutableCollection<C>, consumer: BiConsumer<C, P>) {
        collection.forEach(Consumer { c: C ->
            consumer.accept(c, po)
        })
        cs.saveBatch(collection)
    }

    fun pRemove(id: Serializable, cgetPidFun: SFunction<C, String>) {
        cRemove(id, cgetPidFun)
        ps.removeById(id)
    }

    private fun cRemove(id: Serializable, cgetPidFun: SFunction<C, String>): Boolean {
        return cs.lambdaUpdate().eq(cgetPidFun, id).remove()
    }

    fun pRemoveBatch(
        ids: MutableCollection<Serializable>,
        pgetIdFun: SFunction<P, String>,
        cgetPidFun: SFunction<C, String>
    ) {
        cs.lambdaUpdate().`in`(cgetPidFun, ids).remove()
        ps.lambdaUpdate().`in`(pgetIdFun, ids).remove()
    }

    fun pUpdate(
        po: P,
        collection: MutableCollection<C>,
        pgetIdFun: SFunction<P, String>,
        cgetPidFun: SFunction<C, String>,
        consumer: BiConsumer<C, P>
    ) {
        ps.updateById(po)
        val pid: String = pgetIdFun.apply(po)
        cRemove(pid, cgetPidFun)
        cSave(po, collection, consumer)
    }


    companion object {
        fun <P, C> of(ps: IService<P>, cs: IService<C>): MpUtil2<P, C> {
            return MpUtil2(ps, cs)
        }
    }
}
