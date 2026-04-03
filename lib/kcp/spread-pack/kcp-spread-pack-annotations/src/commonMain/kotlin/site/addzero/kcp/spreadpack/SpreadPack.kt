package site.addzero.kcp.spreadpack

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
annotation class SpreadPack(
    val exclude: Array<String> = [],
    val selector: SpreadPackSelector = SpreadPackSelector.PROPS,
)
