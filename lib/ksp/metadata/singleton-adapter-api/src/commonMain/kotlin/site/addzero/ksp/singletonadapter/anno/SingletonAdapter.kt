package site.addzero.ksp.singletonadapter.anno

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class SingletonAdapter(
    /**
     * 指定生成的单例对象名称。
     * 如果未指定，默认使用类名去掉 "Client" 后缀（如果存在），否则为类名 + "Adapter"。
     */
    val singletonName: String = "",
    
    /**
     * 指定构造函数参数的注入策略。
     * 简单的键值对，格式为 "paramName=env:ENV_VAR_KEY" 或 "paramName=const:VALUE"。
     * 
     * 例如：
     * apiKey=env:SUNO_API_TOKEN
     * baseUrl=const:https://api.vectorengine.ai
     */
    val inject: Array<String> = []
)
