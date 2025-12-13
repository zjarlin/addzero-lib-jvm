package site.addzero.aop.dicttrans.tracking

import org.slf4j.LoggerFactory
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.annotation.PreDestroy

/**
 * Weak reference tracker implementation using WeakHashMap and ReferenceQueue
 *
 * @author zjarlin
 * @since 2025/01/12
 */
class WeakReferenceTrackerImpl : WeakReferenceTracker {
    
    private val logger = LoggerFactory.getLogger(WeakReferenceTrackerImpl::class.java)
    
    // Use WeakHashMap for automatic cleanup when objects are garbage collected
    private val trackedObjects = Collections.synchronizedMap(WeakHashMap<Any, TrackingInfo>())
    
    // Reference queue for immediate cleanup notification
    private val referenceQueue = ReferenceQueue<Any>()
    
    // Map to track weak references and their metadata
    private val weakReferences = ConcurrentHashMap<WeakReference<Any>, String>()
    
    // Set to track objects with circular references
    private val circularReferences = Collections.synchronizedSet(mutableSetOf<Any>())
    
    // Statistics
    private val totalTrackedCount = AtomicLong(0)
    private val cleanedUpCount = AtomicLong(0)
    private val memoryPressureCleanupCount = AtomicLong(0)
    
    // Cleanup thread for processing reference queue
    private val cleanupThread = Thread(this::processReferenceQueue, "WeakReferenceTracker-Cleanup")
    
    init {
        cleanupThread.isDaemon = true
        cleanupThread.start()
        logger.info("WeakReferenceTracker initialized with cleanup thread")
    }
    
    override fun track(obj: Any): Boolean {
        // Check if already tracked
        if (trackedObjects.containsKey(obj)) {
            return false
        }
        
        // Create tracking info
        val trackingInfo = TrackingInfo(
            objectClass = obj.javaClass.name,
            trackingTime = System.currentTimeMillis(),
            objectHashCode = System.identityHashCode(obj)
        )
        
        // Track with weak reference
        trackedObjects[obj] = trackingInfo
        
        // Create weak reference with reference queue for cleanup notification
        val weakRef = WeakReference(obj, referenceQueue)
        weakReferences[weakRef] = trackingInfo.objectClass
        
        totalTrackedCount.incrementAndGet()
        
        logger.trace("Tracking object: {} (hash: {})", trackingInfo.objectClass, trackingInfo.objectHashCode)
        return true
    }
    
    override fun isTracked(obj: Any): Boolean {
        return trackedObjects.containsKey(obj)
    }
    
    override fun cleanup() {
        logger.debug("Manual cleanup triggered, current tracked objects: {}", trackedObjects.size)
        
        // Process any pending references in the queue
        processReferenceQueueOnce()
        
        // Force cleanup of WeakHashMap (this will remove entries for GC'd objects)
        val sizeBefore = trackedObjects.size
        trackedObjects.size // This triggers cleanup in WeakHashMap
        val sizeAfter = trackedObjects.size
        
        if (sizeBefore != sizeAfter) {
            val cleaned = sizeBefore - sizeAfter
            cleanedUpCount.addAndGet(cleaned.toLong())
            logger.debug("Manual cleanup removed {} stale references", cleaned)
        }
    }
    
    override fun size(): Int {
        return trackedObjects.size
    }
    
    override fun clear() {
        logger.info("Clearing all tracked objects, current size: {}", trackedObjects.size)
        trackedObjects.clear()
        circularReferences.clear()
        weakReferences.clear()
        logger.info("All tracked objects cleared")
    }
    
    override fun getStatistics(): TrackingStatistics {
        return TrackingStatistics(
            totalTracked = totalTrackedCount.get(),
            currentlyTracked = trackedObjects.size,
            cleanedUp = cleanedUpCount.get(),
            circularReferences = circularReferences.size,
            memoryPressureCleanups = memoryPressureCleanupCount.get()
        )
    }
    
    override fun hasCircularReference(obj: Any): Boolean {
        return circularReferences.contains(obj)
    }
    
    override fun markCircularReference(obj: Any) {
        circularReferences.add(obj)
        logger.debug("Marked object with circular reference: {} (hash: {})", 
            obj.javaClass.name, System.identityHashCode(obj))
    }
    
    /**
     * Process reference queue to clean up stale references
     */
    private fun processReferenceQueue() {
        logger.debug("Reference queue processing thread started")
        
        try {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    // Wait for a reference to be enqueued (blocking call)
                    val ref = referenceQueue.remove() as? WeakReference<Any>
                    if (ref != null) {
                        val objectClass = weakReferences.remove(ref)
                        cleanedUpCount.incrementAndGet()
                        logger.trace("Cleaned up weak reference for object: {}", objectClass)
                    }
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    break
                } catch (e: Exception) {
                    logger.error("Error processing reference queue", e)
                }
            }
        } finally {
            logger.debug("Reference queue processing thread stopped")
        }
    }
    
    /**
     * Process reference queue once (non-blocking)
     */
    private fun processReferenceQueueOnce() {
        var processed = 0
        while (true) {
            val ref = referenceQueue.poll() as? WeakReference<Any> ?: break
            val objectClass = weakReferences.remove(ref)
            cleanedUpCount.incrementAndGet()
            processed++
            logger.trace("Processed weak reference for object: {}", objectClass)
        }
        
        if (processed > 0) {
            logger.debug("Processed {} references from queue", processed)
        }
    }
    
    /**
     * Trigger cleanup under memory pressure
     */
    fun triggerMemoryPressureCleanup() {
        logger.info("Triggering memory pressure cleanup")
        
        // Process reference queue
        processReferenceQueueOnce()
        
        // Clear circular references (they might be preventing GC)
        val circularCount = circularReferences.size
        circularReferences.clear()
        
        // Force WeakHashMap cleanup
        cleanup()
        
        memoryPressureCleanupCount.incrementAndGet()
        
        logger.info("Memory pressure cleanup completed, cleared {} circular references", circularCount)
    }
    
    @PreDestroy
    fun shutdown() {
        logger.info("Shutting down WeakReferenceTracker")
        cleanupThread.interrupt()
        
        try {
            cleanupThread.join(1000) // Wait up to 1 second
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        
        clear()
        logger.info("WeakReferenceTracker shutdown completed")
    }
    
    /**
     * Tracking information for each object
     */
    private data class TrackingInfo(
        val objectClass: String,
        val trackingTime: Long,
        val objectHashCode: Int
    )
}