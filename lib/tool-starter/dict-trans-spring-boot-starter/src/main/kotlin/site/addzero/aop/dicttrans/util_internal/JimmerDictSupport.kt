package site.addzero.aop.dicttrans.util_internal

import org.babyfish.jimmer.meta.ImmutableProp
import org.babyfish.jimmer.runtime.ImmutableSpi
import org.springframework.core.annotation.AnnotatedElementUtils
import site.addzero.aop.dicttrans.anno.Dict
import site.addzero.aop.dicttrans.dictaop.CommonConstant
import site.addzero.aop.dicttrans.dictaop.entity.TransInfo
import site.addzero.util.ImprovedReflectUtil
import site.addzero.util.str.toCamelCase
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.IdentityHashMap
import kotlin.reflect.full.memberProperties

/**
 * Jimmer immutable entities cannot be subclassed or mutated like normal VO objects.
 *
 * For controller response translation, expose a JSON-friendly map that contains
 * loaded Jimmer properties plus extra translated dictionary fields.
 */
internal object JimmerDictSupport {
    fun isJimmerObject(value: Any?): Boolean = value is ImmutableSpi

    fun translate(value: ImmutableSpi): Map<String, Any?> {
        return translate(value, IdentityHashMap())
    }

    fun translateAll(values: List<ImmutableSpi>): List<Map<String, Any?>> {
        if (values.isEmpty()) {
            return emptyList()
        }

        val visited = IdentityHashMap<ImmutableSpi, MutableMap<String, Any?>>()
        val pairs = values.map { value -> value to createOutput(value, visited) }
        processTranslations(pairs)
        return pairs.map { it.second }
    }

    /**
     * Translate Jimmer entities nested inside ordinary response wrappers, for example:
     * Page<T>(records: List<R>) where R is a Jimmer entity.
     */
    fun translateNestedJimmerValues(root: Any?) {
        normalizeNestedValue(root, IdentityHashMap())
    }

    fun containsJimmerValue(root: Any?): Boolean {
        return containsJimmerValue(root, IdentityHashMap())
    }

    private fun translate(value: ImmutableSpi, visited: IdentityHashMap<ImmutableSpi, MutableMap<String, Any?>>): Map<String, Any?> {
        val output = createOutput(value, visited)
        processTranslations(listOf(value to output))
        return output
    }

    private fun createOutput(
        value: ImmutableSpi,
        visited: IdentityHashMap<ImmutableSpi, MutableMap<String, Any?>>,
    ): MutableMap<String, Any?> {
        visited[value]?.let { return it }

        val output = linkedMapOf<String, Any?>()
        visited[value] = output
        copyLoadedProps(value, output, visited)
        return output
    }

    private fun processTranslations(pairs: List<Pair<ImmutableSpi, MutableMap<String, Any?>>>) {
        val transInfos = pairs.flatMap { (source, output) -> collectTransInfos(source, output) }
        val grouped = transInfos.groupBy { it.classificationOfTranslation }
        TransInternalUtil.processBuiltInDictionaryTranslation(grouped)
        TransInternalUtil.processAnyTableTranslation(grouped)
    }

    private fun copyLoadedProps(
        value: ImmutableSpi,
        output: MutableMap<String, Any?>,
        visited: IdentityHashMap<ImmutableSpi, MutableMap<String, Any?>>,
    ) {
        value.__type().props.values.forEach { prop ->
            if (!isLoadedAndVisible(value, prop)) {
                return@forEach
            }
            output[prop.name] = normalizeValue(value.__get(prop.id), visited)
        }
    }

    private fun collectTransInfos(
        source: ImmutableSpi,
        output: MutableMap<String, Any?>,
    ): List<TransInfo<Dict>> {
        return source.__type().props.values.flatMap { prop ->
            if (!isLoadedAndVisible(source, prop)) {
                return@flatMap emptyList()
            }

            val rawValue = source.__get(prop.id)
            if (rawValue == null || rawValue is Collection<*> && rawValue.isEmpty()) {
                return@flatMap emptyList()
            }

            dictAnnotations(source, prop).map { anno ->
                val transInfo = TransInfo(
                    superObjectFieldTypeEnum = null,
                    superObjectFieldName = null,
                    superObject = null,
                    fieldEnum = null,
                    anno = anno,
                    translationProcess = null,
                    rootObject = output,
                    afterObject = null,
                    afterObjectClass = null,
                    translatedAttributeNames = translatedName(prop.name, anno),
                    attributeNameBeforeTranslation = prop.name,
                    valueBeforeTranslation = rawValue,
                    translatedValue = null,
                    translatedType = anno.spelValueType.java,
                    classificationOfTranslation = null,
                    rootObjectHashBsm = null,
                )
                transInfo.copy(classificationOfTranslation = TransInternalUtil.getTranslateType(transInfo))
            }
        }
    }

    private fun normalizeValue(
        value: Any?,
        visited: IdentityHashMap<ImmutableSpi, MutableMap<String, Any?>>,
    ): Any? {
        return when (value) {
            is ImmutableSpi -> translate(value, visited)
            is Iterable<*> -> value.map { item -> normalizeValue(item, visited) }
            is Array<*> -> value.map { item -> normalizeValue(item, visited) }
            else -> value
        }
    }

    private fun normalizeNestedValue(
        value: Any?,
        visitedObjects: IdentityHashMap<Any, Unit>,
    ): NestedValue {
        return when (value) {
            null -> NestedValue(null, changed = false)
            is ImmutableSpi -> NestedValue(translate(value), changed = true)
            is Map<*, *> -> normalizeMap(value, visitedObjects)
            is Iterable<*> -> normalizeIterable(value, visitedObjects)
            is Array<*> -> normalizeArray(value, visitedObjects)
            else -> {
                if (!shouldInspectObject(value) || visitedObjects.containsKey(value)) {
                    NestedValue(value, changed = false)
                } else {
                    visitedObjects[value] = Unit
                    normalizeObjectFields(value, visitedObjects)
                    NestedValue(value, changed = false)
                }
            }
        }
    }

    private fun containsJimmerValue(
        value: Any?,
        visitedObjects: IdentityHashMap<Any, Unit>,
    ): Boolean {
        return when (value) {
            null -> false
            is ImmutableSpi -> true
            is Map<*, *> -> value.any { (key, mapValue) ->
                containsJimmerValue(key, visitedObjects) || containsJimmerValue(mapValue, visitedObjects)
            }
            is Iterable<*> -> value.any { item -> containsJimmerValue(item, visitedObjects) }
            is Array<*> -> value.any { item -> containsJimmerValue(item, visitedObjects) }
            else -> {
                if (!shouldInspectObject(value) || visitedObjects.containsKey(value)) {
                    false
                } else {
                    visitedObjects[value] = Unit
                    ImprovedReflectUtil.getFields(value.javaClass).any { field ->
                        !Modifier.isStatic(field.modifiers) &&
                            containsJimmerValue(ImprovedReflectUtil.getFieldValue(value, field), visitedObjects)
                    }
                }
            }
        }
    }

    private fun shouldInspectObject(value: Any): Boolean {
        val clazz = value.javaClass
        if (clazz.isPrimitive || clazz.isEnum || clazz.isAnnotation || clazz.isArray) {
            return false
        }

        val packageName = clazz.`package`?.name.orEmpty()
        if (
            packageName.startsWith("java.") ||
            packageName.startsWith("javax.") ||
            packageName.startsWith("kotlin.") ||
            packageName.startsWith("sun.") ||
            packageName.startsWith("jdk.")
        ) {
            return false
        }

        return ImprovedReflectUtil.getFields(clazz).any { field -> !Modifier.isStatic(field.modifiers) }
    }

    private fun normalizeObjectFields(
        target: Any,
        visitedObjects: IdentityHashMap<Any, Unit>,
    ) {
        ImprovedReflectUtil.getFields(target.javaClass).forEach { field ->
            if (Modifier.isStatic(field.modifiers)) {
                return@forEach
            }

            val rawValue = ImprovedReflectUtil.getFieldValue(target, field) ?: return@forEach
            val normalized = normalizeNestedValue(rawValue, visitedObjects)
            if (!normalized.changed) {
                return@forEach
            }

            ImprovedReflectUtil.setFieldValue(target, field, normalized.value)
        }
    }

    private fun normalizeIterable(
        value: Iterable<*>,
        visitedObjects: IdentityHashMap<Any, Unit>,
    ): NestedValue {
        var changed = false
        val normalizedItems = value.map { item ->
            val normalized = normalizeNestedValue(item, visitedObjects)
            changed = changed || normalized.changed
            normalized.value
        }

        if (!changed) {
            return NestedValue(value, changed = false)
        }

        val normalizedCollection = when (value) {
            is Set<*> -> normalizedItems.toCollection(LinkedHashSet())
            else -> normalizedItems
        }
        return NestedValue(normalizedCollection, changed = true)
    }

    private fun normalizeArray(
        value: Array<*>,
        visitedObjects: IdentityHashMap<Any, Unit>,
    ): NestedValue {
        var changed = false
        val normalizedItems = value.map { item ->
            val normalized = normalizeNestedValue(item, visitedObjects)
            changed = changed || normalized.changed
            normalized.value
        }
        return if (changed) {
            NestedValue(normalizedItems.toTypedArray(), changed = true)
        } else {
            NestedValue(value, changed = false)
        }
    }

    private fun normalizeMap(
        value: Map<*, *>,
        visitedObjects: IdentityHashMap<Any, Unit>,
    ): NestedValue {
        var changed = false
        val normalizedMap = linkedMapOf<Any?, Any?>()
        value.forEach { (key, rawValue) ->
            val normalizedKey = normalizeNestedValue(key, visitedObjects)
            val normalizedValue = normalizeNestedValue(rawValue, visitedObjects)
            changed = changed || normalizedKey.changed || normalizedValue.changed
            normalizedMap[normalizedKey.value] = normalizedValue.value
        }
        return if (changed) {
            NestedValue(normalizedMap, changed = true)
        } else {
            NestedValue(value, changed = false)
        }
    }

    private fun dictAnnotations(source: ImmutableSpi, prop: ImmutableProp): List<Dict> {
        val fromJimmerMeta = runCatching {
            prop.getAnnotations(Dict::class.java).toList()
        }.getOrDefault(emptyList())
        val fromKotlinProperty = kotlinPropertyAnnotations(source, prop.name)
        val fromMethod = propertyMethods(source, prop.name).flatMap { method ->
            AnnotatedElementUtils.getMergedRepeatableAnnotations(method, Dict::class.java)
        }
        return (fromJimmerMeta + fromKotlinProperty + fromMethod).distinct()
    }

    private fun kotlinPropertyAnnotations(source: ImmutableSpi, propName: String): List<Dict> {
        return runCatching {
            val property = source.__type().getJavaClass().kotlin.memberProperties
                .firstOrNull { it.name == propName }
                ?: return@runCatching emptyList()

            property.annotations.filterIsInstance<Dict>() +
                property.getter.annotations.filterIsInstance<Dict>()
        }.getOrDefault(emptyList())
    }

    private fun propertyMethods(source: ImmutableSpi, propName: String): List<Method> {
        val entityClass = source.__type().getJavaClass()
        val capitalized = propName.replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase() else char.toString()
        }
        val candidates = setOf(propName, "get$capitalized", "is$capitalized")
        return entityClass.methods
            .asSequence()
            .filter { method -> method.parameterCount == 0 && method.name in candidates }
            .toList()
    }

    private fun translatedName(propName: String, anno: Dict): String {
        val alias = anno.serializationAlias
        val nameColumn = anno.nameColumn.toCamelCase()
        val fallback = propName + CommonConstant.DICT_TEXT_SUFFIX
        return listOf(alias, nameColumn, fallback).firstOrNull { it.isNotBlank() } ?: fallback
    }

    private fun isLoadedAndVisible(source: ImmutableSpi, prop: ImmutableProp): Boolean {
        return source.__isLoaded(prop.id) && source.__isVisible(prop.id)
    }

    private data class NestedValue(
        val value: Any?,
        val changed: Boolean,
    )
}
