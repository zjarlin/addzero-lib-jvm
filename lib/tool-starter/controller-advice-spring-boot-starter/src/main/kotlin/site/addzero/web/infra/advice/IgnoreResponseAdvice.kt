package site.addzero.web.infra.advice

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(
    AnnotationRetention.RUNTIME
) //排除响应处理注解
annotation class IgnoreResponseAdvice
