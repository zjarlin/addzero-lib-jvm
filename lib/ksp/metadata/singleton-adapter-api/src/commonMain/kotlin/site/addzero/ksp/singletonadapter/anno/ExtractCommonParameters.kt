package site.addzero.ksp.singletonadapter.anno

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ExtractCommonParameters(
    /**
     * 指定生成的公共参数类名称。
     * 如果未指定，默认使用 原类名 + "Delegate"。
     */
    val value: String = ""
)