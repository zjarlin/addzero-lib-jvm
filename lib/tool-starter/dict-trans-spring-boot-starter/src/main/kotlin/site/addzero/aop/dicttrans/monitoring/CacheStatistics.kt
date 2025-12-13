package site.addzero.aop.dicttrans.monitoring

/**
 * Cache statistics data class for monitoring cache performance
 *
 * @author zjarlin
 * @since 2025/01/12
 */
data class CacheStatistics(
    val hitCount: Long,
    val missCount: Long,
    val hitRate: Double,
    val evictionCount: Long,
    val size: Long,
    val maxSize: Long
) {
    val totalRequests: Long = hitCount + missCount
    val missRate: Double = if (totalRequests > 0) missCount.toDouble() / totalRequests else 0.0
    val utilizationRate: Double = if (maxSize > 0) size.toDouble() / maxSize else 0.0
    
    companion object {
        fun empty(): CacheStatistics = CacheStatistics(
            hitCount = 0,
            missCount = 0,
            hitRate = 0.0,
            evictionCount = 0,
            size = 0,
            maxSize = 0
        )
    }
}