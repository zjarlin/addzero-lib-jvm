package site.addzero.aop.dicttrans.util_internal

import site.addzero.aop.dicttrans.util.BeanUtil
import site.addzero.aop.dicttrans.util.CollUtil
import site.addzero.aop.dicttrans.util.ReflectUtil
import net.bytebuddy.ByteBuddy
import site.addzero.aop.dicttrans.dictaop.entity.NeedAddInfo
import site.addzero.util.RefUtil
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 优化的字节码工具类
 * 
 * 核心优化：
 * 1. 预先收集所有类型的字段需求并集
 * 2. 每个类型只生成一次字节码
 * 3. 缓存生成的类，避免重复生成
 * 
 * @author zjarlin
 * @since 2025/01/01
 */
internal class OptimizedByteBuddyUtil {

    companion object {
        // 类型到增强类的缓存
        private val classCache = ConcurrentHashMap<Class<*>, Class<*>>()
        
        // 类型到字段需求的缓存
        private val fieldRequirementsCache = ConcurrentHashMap<Class<*>, Set<NeedAddInfo>>()

        /**
         * 优化的批量对象处理
         * 
         * @param objects 待处理的对象列表
         * @param getNeedAddInfoFun 获取字段需求的函数
         * @return 处理后的对象列表
         */
        fun genChildObjectsBatch(
            objects: List<Any?>,
            getNeedAddInfoFun: (Any) -> MutableList<NeedAddInfo>
        ): List<Any?> {
            if (objects.isEmpty()) return objects
            
            // 第一步：收集所有类型的字段需求
            val typeFieldRequirements = collectAllFieldRequirements(objects, getNeedAddInfoFun)
            
            // 第二步：为每个类型生成增强类（只生成一次）
            val enhancedClasses = generateEnhancedClasses(typeFieldRequirements)
            
            // 第三步：批量处理对象
            return objects.map { obj ->
                if (obj == null) return@map null
                processObjectWithEnhancedClass(obj, enhancedClasses, getNeedAddInfoFun)
            }
        }

        /**
         * 收集所有对象类型的字段需求并集
         */
        private fun collectAllFieldRequirements(
            objects: List<Any?>,
            getNeedAddInfoFun: (Any) -> MutableList<NeedAddInfo>
        ): Map<Class<*>, Set<NeedAddInfo>> {
            val typeRequirements = mutableMapOf<Class<*>, MutableSet<NeedAddInfo>>()
            val processedObjects = mutableSetOf<Any>()
            
            // 使用队列进行广度优先遍历，收集所有嵌套对象的字段需求
            val queue = LinkedList<Any>()
            objects.filterNotNull().forEach { queue.add(it) }
            
            while (queue.isNotEmpty()) {
                val obj = queue.poll()
                if (obj == null || !processedObjects.add(obj)) continue
                
                val objClass = obj.javaClass
                if (!RefUtil.isT(obj)) continue
                
                // 获取当前对象的字段需求
                val fieldRequirements = fieldRequirementsCache.computeIfAbsent(objClass) {
                    getNeedAddInfoFun(obj).toSet()
                }
                
                // 合并到类型需求中
                typeRequirements.computeIfAbsent(objClass) { mutableSetOf() }
                    .addAll(fieldRequirements)
                
                // 收集嵌套对象
                collectNestedObjects(obj, queue)
            }
            
            return typeRequirements
        }

        /**
         * 收集嵌套对象加入处理队列
         */
        private fun collectNestedObjects(obj: Any, queue: LinkedList<Any>) {
            val fields = ReflectUtil.getFields(obj.javaClass)
            
            fields.forEach { field ->
                val fieldValue = ReflectUtil.getFieldValue(obj, field)
                
                when {
                    // 处理集合字段
                    RefUtil.isCollectionField(field) && fieldValue != null -> {
                        val collection = fieldValue as? MutableCollection<*>
                        collection?.filterNotNull()?.forEach { item ->
                            if (RefUtil.isT(item)) {
                                queue.add(item)
                            }
                        }
                    }
                    // 处理嵌套对象字段
                    RefUtil.isObjectField(obj, field) && fieldValue != null -> {
                        if (RefUtil.isT(fieldValue)) {
                            queue.add(fieldValue)
                        }
                    }
                }
            }
        }

        /**
         * 为每个类型生成增强类
         */
        private fun generateEnhancedClasses(
            typeFieldRequirements: Map<Class<*>, Set<NeedAddInfo>>
        ): Map<Class<*>, Class<*>> {
            val enhancedClasses = mutableMapOf<Class<*>, Class<*>>()
            
            typeFieldRequirements.forEach { (originalClass, fieldRequirements) ->
                if (fieldRequirements.isEmpty()) {
                    // 如果没有字段需求，检查是否需要跳过转换
                    try {
                        val instance = originalClass.newInstance()
                        if (!canNotSkipTrans(instance)) {
                            enhancedClasses[originalClass] = originalClass
                            return@forEach
                        }
                    } catch (e: Exception) {
                        // 如果无法实例化（如基本类型包装类），直接跳过
                        enhancedClasses[originalClass] = originalClass
                        return@forEach
                    }
                }
                
                // 检查缓存
                val cachedClass = classCache[originalClass]
                if (cachedClass != null) {
                    enhancedClasses[originalClass] = cachedClass
                    return@forEach
                }
                
                // 生成新的增强类
                val enhancedClass = generateEnhancedClass(originalClass, fieldRequirements)
                classCache[originalClass] = enhancedClass
                enhancedClasses[originalClass] = enhancedClass
            }
            
            return enhancedClasses
        }

        /**
         * 为单个类型生成增强类
         */
        private fun generateEnhancedClass(
            originalClass: Class<*>,
            fieldRequirements: Set<NeedAddInfo>
        ): Class<*> {
            try {
                var subclass = ByteBuddy().subclass(originalClass)
                
                fieldRequirements.forEach { needAddInfo ->
                    subclass = subclass.defineProperty(needAddInfo.fieldName, needAddInfo.type)
                }
                
                return subclass.make().load(originalClass.classLoader).loaded
            } catch (e: Exception) {
                val errorMsg = """
                    字节码生成失败: ${e.message}
                    类: ${originalClass.name}
                    字段需求: ${fieldRequirements.map { "${it.fieldName}:${it.type}" }}
                    请检查class是否open，是否有无参构造函数
                """.trimIndent()
                throw RuntimeException(errorMsg, e)
            }
        }

        /**
         * 使用增强类处理单个对象
         */
        private fun processObjectWithEnhancedClass(
            obj: Any,
            enhancedClasses: Map<Class<*>, Class<*>>,
            getNeedAddInfoFun: (Any) -> MutableList<NeedAddInfo>
        ): Any {
            val originalClass = obj.javaClass
            val enhancedClass = enhancedClasses[originalClass] ?: originalClass
            
            // 如果没有增强，直接返回原对象（但仍需处理嵌套对象）
            if (enhancedClass == originalClass && !canNotSkipTrans(obj)) {
                return processNestedObjectsOnly(obj, enhancedClasses, getNeedAddInfoFun)
            }
            
            // 创建增强对象实例
            val enhancedObj = enhancedClass.newInstance()
            
            // 处理嵌套对象
            processNestedFields(obj, enhancedClasses, getNeedAddInfoFun)
            
            // 拷贝属性
            BeanUtil.copyProperties(obj, enhancedObj)
            
            return enhancedObj
        }

        /**
         * 只处理嵌套对象，不创建新实例
         */
        private fun processNestedObjectsOnly(
            obj: Any,
            enhancedClasses: Map<Class<*>, Class<*>>,
            getNeedAddInfoFun: (Any) -> MutableList<NeedAddInfo>
        ): Any {
            processNestedFields(obj, enhancedClasses, getNeedAddInfoFun)
            return obj
        }

        /**
         * 处理嵌套字段
         */
        private fun processNestedFields(
            obj: Any,
            enhancedClasses: Map<Class<*>, Class<*>>,
            getNeedAddInfoFun: (Any) -> MutableList<NeedAddInfo>
        ) {
            val fields = ReflectUtil.getFields(obj.javaClass)
            
            fields.forEach { field ->
                val fieldValue = ReflectUtil.getFieldValue(obj, field)
                
                when {
                    // 处理嵌套对象字段
                    RefUtil.isObjectField(obj, field) && fieldValue != null -> {
                        val processedValue = processObjectWithEnhancedClass(
                            fieldValue, enhancedClasses, getNeedAddInfoFun
                        )
                        ReflectUtil.setFieldValue(obj, field, processedValue)
                    }
                    // 处理集合字段
                    RefUtil.isCollectionField(field) && fieldValue != null -> {
                        val collection = fieldValue as? MutableCollection<*>
                        if (CollUtil.isNotEmpty(collection)) {
                            val processedCollection = collection?.map { item ->
                                if (item != null && RefUtil.isT(item)) {
                                    processObjectWithEnhancedClass(item, enhancedClasses, getNeedAddInfoFun)
                                } else {
                                    item
                                }
                            }
                            ReflectUtil.setFieldValue(obj, field, processedCollection)
                        }
                    }
                }
            }
        }

        /**
         * 清理缓存（用于测试或内存管理）
         */
        fun clearCache() {
            classCache.clear()
            fieldRequirementsCache.clear()
        }

        /**
         * 获取缓存统计信息
         */
        fun getCacheStats(): Map<String, Int> {
            return mapOf(
                "classCache" to classCache.size,
                "fieldRequirementsCache" to fieldRequirementsCache.size
            )
        }

        /**
         * 判断是否不能跳过转换
         * 只要有一个集合或者对象字段就不能跳过
         * 
         * @param o 入参
         * @return boolean
         */
        private fun canNotSkipTrans(o: Any): Boolean {
            val fields = ReflectUtil.getFields(o.javaClass)
            return fields.any { field ->
                val objectField = RefUtil.isObjectField(o, field)
                val collectionField = RefUtil.isCollectionField(field)
                collectionField || objectField
            }
        }
    }
}