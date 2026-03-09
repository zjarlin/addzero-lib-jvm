package site.addzero.lsi.processor

/**
 * 处理器编排所需的 SPI。
 *
 * [dependsOn] 用于声明执行依赖关系。
 * 支持多轮收集（[onRound]）和收尾生成（[onFinish]）两阶段生命周期。
 */
interface ProcessorSpi<T, R> {
    /** 处理器唯一标识，默认使用实现类全限定名。 */
    val id: String
        get() = this::class.qualifiedName ?: this::class.java.name

    /** 当前处理器依赖的上游处理器 ID。 */
    val dependsOn: Set<String>
        get() = emptySet()

    /** 由编排器在调用 [onRound]/[onFinish] 之前注入共享上下文。 */
    var ctx: T

    /**
     * 多轮阶段回调（如 KSP process / APT 每轮 process）。
     * 默认无操作。
     */
    fun onRound() {}

    /**
     * 收尾阶段回调（如 KSP finish / APT processingOver）。
     * 默认复用旧 [process] 逻辑。
     */
    @Suppress("DEPRECATION")
    fun onFinish(): R = process()

    /**
     * 兼容旧实现的入口。
     * 新实现建议覆写 [onRound] / [onFinish]。
     */
    @Deprecated("Use onRound/onFinish instead")
    fun process(): R {
        throw UnsupportedOperationException(
            "Processor '$id' must override onFinish() or process()"
        )
    }
}
