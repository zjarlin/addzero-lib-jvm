//package site.addzero.lib_adpator
//
//import site.addzero.aop.dicttrans.inter.TransStrategy
//import site.addzero.aop.dicttrans.strategy.CollectionStrategy
//import org.babyfish.jimmer.Page
//import org.springframework.stereotype.Component
//
//@Component
//class PageStrategy(private val collectionStrategy: CollectionStrategy) : TransStrategy<Page<Any>> {
//
//    override fun trans(t: Page<Any>): Page<Any> {
//        val rows = t.rows
//        val trans = collectionStrategy.trans(rows)
//        return Page(trans.toList(), t.totalRowCount, t.totalPageCount)
//
//    }
//
//    override fun support(t: Any): Boolean {
//        return Page::class.java.isAssignableFrom(t.javaClass)
//    }
//
//}
