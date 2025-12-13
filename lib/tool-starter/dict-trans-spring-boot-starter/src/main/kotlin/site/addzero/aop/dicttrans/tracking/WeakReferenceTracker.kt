package site.addzero.aop.dicttrans.tracking

/**
 * Weak reference tracker interface for tracking processed objects without preventing garbage collection
 *
 * @author zjarlin
 * @since 2025/01/12
 */
interface WeakReferenceTracker {
    
    /**
     * Track an object using weak reference
     * 
     * @param obj The object to track
     * @return true if object was newly tracked, false if already tracked
     */
    fun track(obj: Any): Boolean
    
    /**
     * Check if an object is currently tracked
     * 
     * @param obj The object to check
     * @return true if object is tracked, false otherwise
     */
    fun isTracked(obj: Any): Boolean
    
    /**
     * Manually trigger cleanup of stale weak references
     */
    fun cleanup()
    
    /**
     * Get current number of tracked objects
     * 
     * @return Number of objects currently tracked
     */
    fun size(): Int
    
    /**
     * Clear all tracked objects
     */
    fun clear()
    
    /**
     * Get statistics about the tracker
     * 
     * @return Tracking statistics
     */
    fun getStatistics(): TrackingStatistics
    
    /**
     * Check if circular references are detected for an object
     * 
     * @param obj The object to check
     * @return true if circular reference detected
     */
    fun hasCircularReference(obj: Any): Boolean
    
    /**
     * Mark an object as having circular references
     * 
     * @param obj The object with circular references
     */
    fun markCircularReference(obj: Any)
}

/**
 * Statistics for weak reference tracking
 */
data class TrackingStatistics(
    val totalTracked: Long,
    val currentlyTracked: Int,
    val cleanedUp: Long,
    val circularReferences: Int,
    val memoryPressureCleanups: Long
)