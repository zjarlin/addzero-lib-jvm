package site.addzero.apt.dict.nested

import site.addzero.apt.dict.context.TranslationContext
import site.addzero.apt.dict.engine.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * Recursive translation processor for nested object structures
 * 
 * This processor handles complex nested object translation with the following features:
 * - Recursive translation logic generator for nested objects
 * - Collection iteration and translation code generation
 * - Circular reference detection and prevention mechanisms
 * - Dynamic lookup code for runtime-dependent translations
 * - Performance optimization for deep object hierarchies
 */
class RecursiveTranslationProcessor(
    private val maxDepth: Int = 10,
    private val circularReferenceDetection: Boolean = true
) {
    
    private val processedObjects = ConcurrentHashMap<Any, Int>()
    private val currentDepth = AtomicInteger(0)
    
    /**
     * Processes nested object translation recursively
     * 
     * @param rootObject The root object to process
     * @param metadata Translation metadata for the root object
     * @param context Translation context with pre-loaded data
     * @return Processed object with all nested translations applied
     */
    fun <T : Any> processNestedTranslation(
        rootObject: T,
        metadata: EntityMetadata,
        context: TranslationContext
    ): T {
        
        // Reset processing state
        processedObjects.clear()
        currentDepth.set(0)
        
        return processObjectRecursively(rootObject, metadata, context, mutableSetOf())
    }
    
    /**
     * Processes a collection of nested objects
     * 
     * @param objects Collection of objects to process
     * @param metadata Translation metadata
     * @param context Translation context
     * @return Collection of processed objects
     */
    fun <T : Any> processNestedCollection(
        objects: Collection<T>,
        metadata: EntityMetadata,
        context: TranslationContext
    ): List<T> {
        
        return objects.map { obj ->
            processNestedTranslation(obj, metadata, context)
        }
    }
    
    /**
     * Core recursive processing logic
     */
    private fun <T : Any> processObjectRecursively(
        obj: T,
        metadata: EntityMetadata,
        context: TranslationContext,
        visitedObjects: MutableSet<Any>
    ): T {
        
        // Check depth limit
        if (currentDepth.get() >= maxDepth) {
            println("Warning: Maximum recursion depth ($maxDepth) reached, stopping recursion")
            return obj
        }
        
        // Check for circular references
        if (circularReferenceDetection && visitedObjects.contains(obj)) {
            println("Warning: Circular reference detected, skipping object")
            return obj
        }
        
        visitedObjects.add(obj)
        currentDepth.incrementAndGet()
        
        try {
            // Process current object's translation fields
            processCurrentObjectFields(obj, metadata, context)
            
            // Process nested fields
            metadata.nestedFields.forEach { nestedField ->
                processNestedField(obj, nestedField, context, visitedObjects)
            }
            
            return obj
            
        } finally {
            currentDepth.decrementAndGet()
            visitedObjects.remove(obj)
        }
    }
    
    /**
     * Processes translation fields of the current object
     */
    private fun processCurrentObjectFields(
        obj: Any,
        metadata: EntityMetadata,
        context: TranslationContext
    ) {
        
        val systemTranslator = SystemDictTranslator()
        val tableTranslator = TableDictTranslator()
        val spelTranslator = SpelTranslator()
        
        // Process system dictionary fields
        metadata.systemDictFields.forEach { field ->
            try {
                systemTranslator.translateField(obj, field, context)
            } catch (e: Exception) {
                println("Warning: Failed to translate system dict field ${field.sourceField}: ${e.message}")
            }
        }
        
        // Process table dictionary fields
        metadata.tableDictFields.forEach { field ->
            try {
                tableTranslator.translateField(obj, field, context)
            } catch (e: Exception) {
                println("Warning: Failed to translate table dict field ${field.sourceField}: ${e.message}")
            }
        }
        
        // Process SPEL fields
        metadata.spelFields.forEach { field ->
            try {
                spelTranslator.translateField(obj, field, context)
            } catch (e: Exception) {
                println("Warning: Failed to translate SPEL field ${field.targetField}: ${e.message}")
            }
        }
    }
    
    /**
     * Processes a nested field (single object or collection)
     */
    private fun processNestedField(
        parentObj: Any,
        nestedField: NestedFieldInfo,
        context: TranslationContext,
        visitedObjects: MutableSet<Any>
    ) {
        
        try {
            val nestedValue = getFieldValue(parentObj, nestedField.fieldName)
            
            when {
                nestedValue == null -> {
                    // Nothing to process
                }
                
                nestedValue is Collection<*> -> {
                    // Process collection of nested objects
                    processNestedCollection(nestedValue, nestedField, context, visitedObjects)
                }
                
                nestedValue is Array<*> -> {
                    // Process array of nested objects
                    processNestedArray(nestedValue, nestedField, context, visitedObjects)
                }
                
                else -> {
                    // Process single nested object
                    processObjectRecursively(nestedValue, nestedField.nestedMetadata, context, visitedObjects)
                }
            }
            
        } catch (e: Exception) {
            println("Warning: Failed to process nested field ${nestedField.fieldName}: ${e.message}")
        }
    }
    
    /**
     * Processes a collection of nested objects
     */
    private fun processNestedCollection(
        collection: Collection<*>,
        nestedField: NestedFieldInfo,
        context: TranslationContext,
        visitedObjects: MutableSet<Any>
    ) {
        
        collection.filterNotNull().forEach { item ->
            processObjectRecursively(item, nestedField.nestedMetadata, context, visitedObjects)
        }
    }
    
    /**
     * Processes an array of nested objects
     */
    private fun processNestedArray(
        array: Array<*>,
        nestedField: NestedFieldInfo,
        context: TranslationContext,
        visitedObjects: MutableSet<Any>
    ) {
        
        array.filterNotNull().forEach { item ->
            processObjectRecursively(item, nestedField.nestedMetadata, context, visitedObjects)
        }
    }
    
    /**
     * Gets field value using reflection
     */
    private fun getFieldValue(obj: Any, fieldName: String): Any? {
        return try {
            val field = obj.javaClass.getDeclaredField(fieldName)
            field.isAccessible = true
            field.get(obj)
        } catch (e: NoSuchFieldException) {
            // Try with Kotlin reflection for properties
            try {
                val kClass = obj::class
                val property = kClass.memberProperties.find { it.name == fieldName }
                property?.let {
                    it.isAccessible = true
                    it.getter.call(obj)
                }
            } catch (e2: Exception) {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Gets processing statistics
     */
    fun getProcessingStatistics(): NestedProcessingStatistics {
        return NestedProcessingStatistics(
            processedObjectCount = processedObjects.size,
            maxDepthReached = currentDepth.get(),
            maxDepthLimit = maxDepth,
            circularReferenceDetectionEnabled = circularReferenceDetection
        )
    }
    
    /**
     * Resets the processor state
     */
    fun reset() {
        processedObjects.clear()
        currentDepth.set(0)
    }
}

/**
 * Circular reference detector for preventing infinite recursion
 */
class CircularReferenceDetector {
    
    private val objectGraph = mutableMapOf<Any, MutableSet<Any>>()
    
    /**
     * Checks if adding a reference would create a circular dependency
     */
    fun wouldCreateCircle(from: Any, to: Any): Boolean {
        return hasPath(to, from)
    }
    
    /**
     * Adds a reference to the object graph
     */
    fun addReference(from: Any, to: Any) {
        objectGraph.getOrPut(from) { mutableSetOf() }.add(to)
    }
    
    /**
     * Removes a reference from the object graph
     */
    fun removeReference(from: Any, to: Any) {
        objectGraph[from]?.remove(to)
        if (objectGraph[from]?.isEmpty() == true) {
            objectGraph.remove(from)
        }
    }
    
    /**
     * Checks if there's a path from source to target
     */
    private fun hasPath(source: Any, target: Any, visited: MutableSet<Any> = mutableSetOf()): Boolean {
        if (source == target) return true
        if (visited.contains(source)) return false
        
        visited.add(source)
        
        val references = objectGraph[source] ?: return false
        return references.any { hasPath(it, target, visited) }
    }
    
    /**
     * Clears the object graph
     */
    fun clear() {
        objectGraph.clear()
    }
    
    /**
     * Gets the current object graph size
     */
    fun getGraphSize(): Int = objectGraph.size
}

/**
 * Dynamic lookup generator for runtime-dependent translations
 */
class DynamicLookupGenerator {
    
    /**
     * Generates dynamic lookup code for conditional translations
     * 
     * @param condition The condition expression
     * @param trueTranslation Translation to apply when condition is true
     * @param falseTranslation Translation to apply when condition is false
     * @return Generated lookup function
     */
    fun generateConditionalLookup(
        condition: String,
        trueTranslation: TranslationRule,
        falseTranslation: TranslationRule? = null
    ): (Any, TranslationContext) -> String? {
        
        return { entity, context ->
            try {
                val conditionResult = evaluateCondition(condition, entity, context)
                
                if (conditionResult) {
                    applyTranslationRule(trueTranslation, entity, context)
                } else {
                    falseTranslation?.let { applyTranslationRule(it, entity, context) }
                }
            } catch (e: Exception) {
                println("Warning: Dynamic lookup failed for condition '$condition': ${e.message}")
                null
            }
        }
    }
    
    /**
     * Generates lookup code for multi-condition scenarios
     */
    fun generateMultiConditionLookup(
        conditions: List<ConditionalTranslation>
    ): (Any, TranslationContext) -> String? {
        
        return { entity, context ->
            var result: String? = null
            
            for (conditionalTranslation in conditions) {
                try {
                    if (evaluateCondition(conditionalTranslation.condition, entity, context)) {
                        result = applyTranslationRule(conditionalTranslation.translation, entity, context)
                        break
                    }
                } catch (e: Exception) {
                    println("Warning: Condition evaluation failed: ${e.message}")
                }
            }
            
            result
        }
    }
    
    /**
     * Evaluates a condition expression
     */
    private fun evaluateCondition(condition: String, entity: Any, context: TranslationContext): Boolean {
        // Simplified condition evaluation - in real implementation, this would use SPEL or similar
        return when {
            condition.contains("!=") -> {
                val parts = condition.split("!=").map { it.trim() }
                if (parts.size == 2) {
                    val leftValue = getValueFromExpression(parts[0], entity)
                    val rightValue = parts[1].removeSurrounding("'", "'")
                    leftValue != rightValue
                } else false
            }
            condition.contains("==") -> {
                val parts = condition.split("==").map { it.trim() }
                if (parts.size == 2) {
                    val leftValue = getValueFromExpression(parts[0], entity)
                    val rightValue = parts[1].removeSurrounding("'", "'")
                    leftValue == rightValue
                } else false
            }
            else -> false
        }
    }
    
    /**
     * Gets value from a simple expression
     */
    private fun getValueFromExpression(expression: String, entity: Any): String? {
        return try {
            val fieldName = expression.removePrefix("entity.").trim()
            val field = entity.javaClass.getDeclaredField(fieldName)
            field.isAccessible = true
            field.get(entity)?.toString()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Applies a translation rule
     */
    private fun applyTranslationRule(rule: TranslationRule, entity: Any, context: TranslationContext): String? {
        return when (rule.type) {
            TranslationRuleType.SYSTEM_DICT -> {
                val sourceValue = getValueFromExpression(rule.sourceField, entity)
                context.getSystemDictTranslation(rule.dictCode!!, sourceValue)
            }
            TranslationRuleType.TABLE_DICT -> {
                val sourceValue = getValueFromExpression(rule.sourceField, entity)
                context.getTableDictTranslation(rule.table!!, rule.codeColumn!!, rule.nameColumn!!, sourceValue)
            }
            TranslationRuleType.LITERAL -> {
                rule.literalValue
            }
            TranslationRuleType.SPEL -> {
                context.getSpelTranslation(rule.spelExpression!!, entity)
            }
        }
    }
}

/**
 * Collection iteration processor for handling various collection types
 */
class CollectionIterationProcessor {
    
    /**
     * Processes different types of collections
     */
    fun processCollection(
        collection: Any,
        processor: (Any) -> Unit
    ) {
        when (collection) {
            is Collection<*> -> {
                collection.filterNotNull().forEach(processor)
            }
            is Array<*> -> {
                collection.filterNotNull().forEach(processor)
            }
            is Map<*, *> -> {
                collection.values.filterNotNull().forEach(processor)
            }
            is Iterable<*> -> {
                collection.filterNotNull().forEach(processor)
            }
            else -> {
                println("Warning: Unsupported collection type: ${collection.javaClass}")
            }
        }
    }
    
    /**
     * Gets collection size safely
     */
    fun getCollectionSize(collection: Any): Int {
        return when (collection) {
            is Collection<*> -> collection.size
            is Array<*> -> collection.size
            is Map<*, *> -> collection.size
            else -> 0
        }
    }
    
    /**
     * Checks if object is a collection type
     */
    fun isCollection(obj: Any): Boolean {
        return obj is Collection<*> || obj is Array<*> || obj is Map<*, *> || obj is Iterable<*>
    }
}

/**
 * Statistics for nested processing
 */
data class NestedProcessingStatistics(
    val processedObjectCount: Int,
    val maxDepthReached: Int,
    val maxDepthLimit: Int,
    val circularReferenceDetectionEnabled: Boolean
) {
    override fun toString(): String {
        return "NestedProcessingStatistics(" +
                "processedObjectCount=$processedObjectCount, " +
                "maxDepthReached=$maxDepthReached, " +
                "maxDepthLimit=$maxDepthLimit, " +
                "circularReferenceDetectionEnabled=$circularReferenceDetectionEnabled" +
                ")"
    }
}

/**
 * Translation rule for dynamic lookups
 */
data class TranslationRule(
    val type: TranslationRuleType,
    val sourceField: String,
    val dictCode: String? = null,
    val table: String? = null,
    val codeColumn: String? = null,
    val nameColumn: String? = null,
    val literalValue: String? = null,
    val spelExpression: String? = null
)

/**
 * Translation rule types
 */
enum class TranslationRuleType {
    SYSTEM_DICT,
    TABLE_DICT,
    LITERAL,
    SPEL
}

/**
 * Conditional translation configuration
 */
data class ConditionalTranslation(
    val condition: String,
    val translation: TranslationRule
)