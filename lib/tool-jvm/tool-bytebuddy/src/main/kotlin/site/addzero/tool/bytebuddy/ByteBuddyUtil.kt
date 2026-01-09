package site.addzero.tool.bytebuddy

import net.bytebuddy.ByteBuddy
import site.addzero.util.ImprovedReflectUtil
import site.addzero.util.RefUtil
import java.util.LinkedList
import java.util.concurrent.ConcurrentHashMap

/**
 * 通用的 ByteBuddy 对象增强工具。
 *
 * 调用方需要提供字段需求解析函数，工具内部负责：
 * 1. 广度优先收集所有嵌套对象
 * 2. 为每种类型只生成一次增强类并缓存
 * 3. 复制原对象属性并填充新增字段
 */
object ByteBuddyUtil {

    data class DynamicFieldDefinition(val fieldName: String, val fieldType: Class<*>)

    private val classCache = ConcurrentHashMap<Class<*>, Class<*>>()
    private val fieldRequirementsCache = ConcurrentHashMap<Class<*>, Set<DynamicFieldDefinition>>()

    fun genChildObjectsBatch(
        objects: List<Any?>,
        fieldResolver: (Any) -> Collection<DynamicFieldDefinition>,
    ): List<Any?> {
        if (objects.isEmpty()) return objects

        val typeFieldRequirements = collectAllFieldRequirements(objects, fieldResolver)
        val enhancedClasses = generateEnhancedClasses(typeFieldRequirements)

        return objects.map { obj ->
            obj?.let { processObjectWithEnhancedClass(it, enhancedClasses, fieldResolver) }
        }
    }

    private fun collectAllFieldRequirements(
        objects: List<Any?>,
        fieldResolver: (Any) -> Collection<DynamicFieldDefinition>,
    ): Map<Class<*>, Set<DynamicFieldDefinition>> {
        val requirements = mutableMapOf<Class<*>, MutableSet<DynamicFieldDefinition>>()
        val visitedObjects = mutableSetOf<Any>()
        val queue = LinkedList<Any>()
        objects.filterNotNull().forEach(queue::add)

        while (queue.isNotEmpty()) {
            val current = queue.poll() ?: continue
            if (!visitedObjects.add(current)) continue
            if (!RefUtil.isT(current)) continue

            val clazz = current.javaClass
            val fieldDefs = fieldRequirementsCache.computeIfAbsent(clazz) {
                fieldResolver(current).toSet()
            }

            requirements.computeIfAbsent(clazz) { mutableSetOf() }.addAll(fieldDefs)
            collectNestedObjects(current, queue)
        }
        return requirements
    }

    private fun collectNestedObjects(source: Any, queue: LinkedList<Any>) {
        ImprovedReflectUtil.getFields(source.javaClass).forEach { field ->
            val value = ImprovedReflectUtil.getFieldValue(source, field)
            when {
                value == null -> Unit
                RefUtil.isObjectField(source, field) && RefUtil.isT(value) -> queue.add(value)
                RefUtil.isCollectionField(field) -> {
                    val collection = value as? Iterable<*> ?: return@forEach
                    collection.filterNotNull().filter { RefUtil.isT(it) }.forEach(queue::add)
                }
            }
        }
    }

    private fun generateEnhancedClasses(
        typeFieldRequirements: Map<Class<*>, Set<DynamicFieldDefinition>>,
    ): Map<Class<*>, Class<*>> {
        val enhanced = mutableMapOf<Class<*>, Class<*>>()
        typeFieldRequirements.forEach { (clazz, definitions) ->
            val cached = classCache[clazz]
            if (cached != null) {
                enhanced[clazz] = cached
                return@forEach
            }
            if (definitions.isEmpty()) {
                enhanced[clazz] = clazz
                return@forEach
            }
            val generated = generateEnhancedClass(clazz, definitions)
            classCache[clazz] = generated
            enhanced[clazz] = generated
        }
        return enhanced
    }

    private fun generateEnhancedClass(
        originalClass: Class<*>,
        fieldDefinitions: Set<DynamicFieldDefinition>,
    ): Class<*> {
        return try {
            var subclass = ByteBuddy().subclass(originalClass)
            fieldDefinitions.forEach { def ->
                subclass = subclass.defineProperty(def.fieldName, def.fieldType)
            }
            subclass.make().load(originalClass.classLoader).loaded
        } catch (e: Exception) {
            throw IllegalStateException("Failed to generate enhanced class for ${originalClass.name}", e)
        }
    }

    private fun processObjectWithEnhancedClass(
        obj: Any,
        enhancedClasses: Map<Class<*>, Class<*>>,
        fieldResolver: (Any) -> Collection<DynamicFieldDefinition>,
    ): Any {
        val originalClass = obj.javaClass
        val enhancedClass = enhancedClasses[originalClass] ?: originalClass

        processNestedFields(obj, enhancedClasses, fieldResolver)

        if (enhancedClass === originalClass || !hasExtraFields(enhancedClass)) {
            return obj
        }

        val enhancedObj = enhancedClass.getDeclaredConstructor().newInstance()
        copyProperties(obj, enhancedObj)
        return enhancedObj
    }

    private fun processNestedFields(
        obj: Any,
        enhancedClasses: Map<Class<*>, Class<*>>,
        fieldResolver: (Any) -> Collection<DynamicFieldDefinition>,
    ) {
        ImprovedReflectUtil.getFields(obj.javaClass).forEach { field ->
            val fieldValue = ImprovedReflectUtil.getFieldValue(obj, field) ?: return@forEach
            when {
                RefUtil.isObjectField(obj, field) -> {
                    val processed = processObjectWithEnhancedClass(fieldValue, enhancedClasses, fieldResolver)
                    ImprovedReflectUtil.setFieldValue(obj, field, processed)
                }
                RefUtil.isCollectionField(field) -> {
                    val collection = fieldValue as? Collection<*>
                    if (!collection.isNullOrEmpty()) {
                        val processed = collection.map { item ->
                            if (item != null && RefUtil.isT(item)) {
                                processObjectWithEnhancedClass(item, enhancedClasses, fieldResolver)
                            } else {
                                item
                            }
                        }
                        ImprovedReflectUtil.setFieldValue(obj, field, processed)
                    }
                }
            }
        }
    }

    private fun hasExtraFields(enhancedClass: Class<*>): Boolean {
        val original = enhancedClass.superclass ?: return false
        val enhancedFieldNames = ImprovedReflectUtil.getFields(enhancedClass).map { it.name }.toSet()
        val originalFieldNames = ImprovedReflectUtil.getFields(original).map { it.name }.toSet()
        return enhancedFieldNames.size > originalFieldNames.size
    }

    private fun copyProperties(source: Any, target: Any) {
        val sourceFields = ImprovedReflectUtil.getFields(source.javaClass)
        val targetFields = ImprovedReflectUtil.getFields(target.javaClass).associateBy { it.name }
        sourceFields.forEach { field ->
            val targetField = targetFields[field.name] ?: return@forEach
            val value = ImprovedReflectUtil.getFieldValue(source, field)
            ImprovedReflectUtil.setFieldValue(target, targetField, value)
        }
    }

    fun clearCache() {
        classCache.clear()
        fieldRequirementsCache.clear()
    }

    fun cacheStats(): Map<String, Int> = mapOf(
        "classCache" to classCache.size,
        "fieldRequirementsCache" to fieldRequirementsCache.size,
    )
}
