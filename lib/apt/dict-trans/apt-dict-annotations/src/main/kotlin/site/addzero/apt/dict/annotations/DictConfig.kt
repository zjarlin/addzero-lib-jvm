package site.addzero.apt.dict.annotations

/**
 * 字典翻译配置注解
 * 用于配置全局的字典翻译行为
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class DictConfig(
    /**
     * 字典服务接口类
     */
    val serviceClass: String = "site.addzero.apt.dict.service.DictService",
    
    /**
     * 是否启用批量翻译优化，默认为 true
     */
    val batchTranslate: Boolean = true,
    
    /**
     * 批量翻译的最大批次大小，默认为 100
     */
    val batchSize: Int = 100,
    
    /**
     * 是否生成异步翻译方法，默认为 false
     */
    val generateAsync: Boolean = false,
    
    /**
     * 缓存策略
     */
    val cacheStrategy: CacheStrategy = CacheStrategy.LOCAL,
    
    /**
     * 缓存过期时间（秒），默认为 300 秒（5分钟）
     */
    val cacheExpireSeconds: Int = 300
)

enum class CacheStrategy {
    NONE,       // 不缓存
    LOCAL,      // 本地缓存
    REDIS,      // Redis 缓存
    CUSTOM      // 自定义缓存
}