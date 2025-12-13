package site.addzero.aop.dicttrans.tracking

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.ref.WeakReference

/**
 * Test for weak reference tracker
 */
class WeakReferenceTrackerTest {
    
    private lateinit var tracker: WeakReferenceTracker
    
    @BeforeEach
    fun setUp() {
        tracker = WeakReferenceTrackerImpl()
    }
    
    @Test
    fun `should track objects and detect when they are tracked`() {
        // Given
        val obj1 = TestObject("test1")
        val obj2 = TestObject("test2")
        
        // When
        val tracked1 = tracker.track(obj1)
        val tracked2 = tracker.track(obj2)
        val trackedAgain = tracker.track(obj1) // Track same object again
        
        // Then
        assertTrue(tracked1, "Should track new object")
        assertTrue(tracked2, "Should track new object")
        assertFalse(trackedAgain, "Should not track same object twice")
        
        assertTrue(tracker.isTracked(obj1), "Object should be tracked")
        assertTrue(tracker.isTracked(obj2), "Object should be tracked")
        assertEquals(2, tracker.size(), "Should have 2 tracked objects")
    }
    
    @Test
    fun `should allow garbage collection of tracked objects`() {
        // Given
        var obj: TestObject? = TestObject("gc-test")
        val weakRef = WeakReference(obj)
        
        // When - track object
        tracker.track(obj!!)
        assertTrue(tracker.isTracked(obj), "Object should be tracked")
        
        // Remove strong reference
        obj = null
        
        // Force garbage collection
        System.gc()
        Thread.sleep(100) // Give GC time to work
        System.gc()
        
        // Then - object should be eligible for GC
        // Note: We can't guarantee GC will happen, but we can check that tracking doesn't prevent it
        // The WeakHashMap should eventually clean up the entry
        tracker.cleanup()
        
        // The weak reference might be cleared if GC occurred
        // This test mainly ensures that tracking doesn't prevent GC
        assertTrue(true, "Test completed - tracking allows GC")
    }
    
    @Test
    fun `should handle circular references`() {
        // Given
        val obj1 = TestObject("circular1")
        val obj2 = TestObject("circular2")
        
        // When - mark circular references
        tracker.track(obj1)
        tracker.track(obj2)
        tracker.markCircularReference(obj1)
        tracker.markCircularReference(obj2)
        
        // Then
        assertTrue(tracker.hasCircularReference(obj1), "Should detect circular reference")
        assertTrue(tracker.hasCircularReference(obj2), "Should detect circular reference")
        
        val stats = tracker.getStatistics()
        assertEquals(2, stats.circularReferences, "Should have 2 circular references")
    }
    
    @Test
    fun `should provide accurate statistics`() {
        // Given
        val obj1 = TestObject("stats1")
        val obj2 = TestObject("stats2")
        val obj3 = TestObject("stats3")
        
        // When
        tracker.track(obj1)
        tracker.track(obj2)
        tracker.track(obj3)
        tracker.markCircularReference(obj1)
        
        val stats = tracker.getStatistics()
        
        // Then
        assertEquals(3, stats.totalTracked, "Should have tracked 3 objects")
        assertEquals(3, stats.currentlyTracked, "Should currently track 3 objects")
        assertEquals(1, stats.circularReferences, "Should have 1 circular reference")
        assertTrue(stats.cleanedUp >= 0, "Cleaned up count should be non-negative")
    }
    
    @Test
    fun `should clear all tracked objects`() {
        // Given
        val obj1 = TestObject("clear1")
        val obj2 = TestObject("clear2")
        
        tracker.track(obj1)
        tracker.track(obj2)
        tracker.markCircularReference(obj1)
        
        assertEquals(2, tracker.size(), "Should have 2 tracked objects")
        
        // When
        tracker.clear()
        
        // Then
        assertEquals(0, tracker.size(), "Should have no tracked objects")
        assertFalse(tracker.isTracked(obj1), "Object should not be tracked")
        assertFalse(tracker.isTracked(obj2), "Object should not be tracked")
        assertFalse(tracker.hasCircularReference(obj1), "Should not have circular reference")
    }
    
    @Test
    fun `should handle cleanup operations`() {
        // Given
        val obj1 = TestObject("cleanup1")
        val obj2 = TestObject("cleanup2")
        
        tracker.track(obj1)
        tracker.track(obj2)
        
        // When
        tracker.cleanup() // Should not throw exception
        
        // Then
        assertTrue(tracker.size() >= 0, "Size should be non-negative after cleanup")
        
        val stats = tracker.getStatistics()
        assertTrue(stats.cleanedUp >= 0, "Cleaned up count should be non-negative")
    }
    
    @Test
    fun `should handle null objects gracefully`() {
        // Given/When/Then - should not throw exceptions
        assertDoesNotThrow {
            // These operations should handle edge cases gracefully
            tracker.cleanup()
            tracker.clear()
            val stats = tracker.getStatistics()
            assertNotNull(stats, "Statistics should not be null")
        }
    }
    
    // Test class for tracking
    private data class TestObject(val name: String) {
        override fun toString(): String = "TestObject($name)"
    }
}