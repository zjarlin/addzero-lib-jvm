//package site.addzero.ioc.annotation
//
//import org.koin.core.KoinApplication
//import org.koin.core.annotation.Single
//import org.koin.core.context.KoinContext
//import org.koin.dsl.module
//
//// 库代码（不调用 startKoin()）
//interface PaymentStrategy {
//    fun pay(amount: Double)
//}
//// 临时方案（弊端：强耦合 startKoin()，易与用户的 Koin 冲突）
//inline fun <reified T> injectList(): List<T> = KoinContext()
//
//fun main() {
//    val injectList = injectList<PaymentStrategy>()
//    println()
//}
//
//// 库内部逻辑：获取所有策略实现
//object PaymentProcessor {
//    fun processAllPayments(amount: Double) {
//
//    }
//}
//
//// 用户代码（注册自定义策略）
//@Single
//class AliPayStrategy : PaymentStrategy {
//    override fun pay(amount: Double) = println("支付宝支付: $amount")
//}
//
//@Single
//class WechatPayStrategy : PaymentStrategy {
//    override fun pay(amount: Double) = println("支付宝支付: $amount")
//}
//
//// 用户通过 Koin Module 注册（无需调用 startKoin()）