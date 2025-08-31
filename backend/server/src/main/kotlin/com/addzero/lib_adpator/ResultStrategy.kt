//package com.addzero.lib_adpator
//
//import cn.hutool.extra.spring.SpringUtil
//import com.addzero.aop.dicttrans.inter.TransStrategy
//import com.addzero.aop.dicttrans.strategy.TransStrategySelector
//import com.addzero.entity.Res
//import org.springframework.stereotype.Component
//
//@Component
//class ResultStrategy : TransStrategy<Res<Any>> {
//    override fun trans(t: Res<Any>): Res<Any> {
//        val data = t.data ?: return t
//        val transStrategySelector = SpringUtil.getBean(TransStrategySelector::class.java)
//        val strategy = transStrategySelector.getStrategy(data)
//        val trans = strategy?.trans(data)
//        val success = Res.success(trans)
//        return success
//    }
//
//    override fun support(t: Any): Boolean {
//        return Res::class.java.isAssignableFrom(t.javaClass)
//    }
//
//
//}
