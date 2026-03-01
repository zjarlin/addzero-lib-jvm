package site.addzero.lsi.k2.ext

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.psi.KtClass
import site.addzero.lsi.assist.TypeChecker
import site.addzero.lsi.assist.getDefaultAnyValueForType
import site.addzero.lsi.assist.getDefaultValueForType
import site.addzero.util.str.toUnderLineCase

private const val MAX_RECURSION_DEPTH = 3

fun KtClass.guessTableNameK2(): String {
    return name?.toUnderLineCase() ?: ""
}

/**
 * 使用 K2 Analysis API 将 KtClass 转换为 JsonObject
 */
fun KtClass.toJsonObjectK2(depth: Int = 0): JsonObject {
    if (depth > MAX_RECURSION_DEPTH) return JsonObject()

    return analyze(this) {
        val jsonObject = JsonObject()
        val classSymbol = this@toJsonObjectK2.symbol

        getProperties().forEach { property ->
            val propertyName = property.name ?: return@forEach
            val propertySymbol = property.symbol
            val returnType = propertySymbol.returnType

            when {
                returnType is KaClassType -> {
                    val fqName = returnType.classId.asFqNameString()
                    when {
                        fqName.startsWith("kotlin.collections.List") ||
                        fqName.startsWith("java.util.List") -> {
                            val elementType = returnType.typeArguments.firstOrNull()
                            val jsonArray = JsonArray()
                            if (elementType != null) {
                                val elementFqName = (elementType.type as? KaClassType)?.classId?.asFqNameString()
                                if (elementFqName != null && TypeChecker.isCustomObjectType(elementFqName)) {
                                    val elementClass = (elementType.type as? KaClassType)?.symbol?.psi as? KtClass
                                    elementClass?.let { jsonArray.add(it.toJsonObjectK2(depth + 1)) }
                                } else {
                                    jsonArray.add(getDefaultValueForType(elementFqName ?: "Any"))
                                }
                            }
                            jsonObject.add(propertyName, jsonArray)
                        }
                        TypeChecker.isCustomObjectType(fqName) -> {
                            val nestedClass = returnType.symbol.psi as? KtClass
                            nestedClass?.let { jsonObject.add(propertyName, it.toJsonObjectK2(depth + 1)) }
                                ?: jsonObject.addProperty(propertyName, fqName)
                        }
                        else -> {
                            jsonObject.addProperty(propertyName, getDefaultValueForType(fqName))
                        }
                    }
                }
                else -> {
                    val typeName = property.typeReference?.text ?: "Any"
                    jsonObject.addProperty(propertyName, getDefaultValueForType(typeName))
                }
            }
        }
        jsonObject
    }
}

/**
 * 使用 K2 Analysis API 将 KtClass 转换为 Map
 */
fun KtClass.toMapK2(depth: Int = 0): Map<String, Any?> {
    if (depth > MAX_RECURSION_DEPTH) return emptyMap()

    return analyze(this) {
        val outputMap = LinkedHashMap<String, Any?>()

        getProperties().forEach { property ->
            val propertyName = property.name ?: return@forEach
            val propertySymbol = property.symbol
            val returnType = propertySymbol.returnType

            when {
                returnType is KaClassType -> {
                    val fqName = returnType.classId.asFqNameString()
                    when {
                        fqName.startsWith("kotlin.collections.List") ||
                        fqName.startsWith("java.util.List") -> {
                            val elementType = returnType.typeArguments.firstOrNull()
                            val elementFqName = (elementType?.type as? KaClassType)?.classId?.asFqNameString()
                            if (elementFqName != null && TypeChecker.isCustomObjectType(elementFqName)) {
                                val elementClass = (elementType.type as? KaClassType)?.symbol?.psi as? KtClass
                                outputMap[propertyName] = listOf(elementClass?.toMapK2(depth + 1) ?: mapOf("type" to elementFqName))
                            } else {
                                outputMap[propertyName] = listOf(getDefaultAnyValueForType(elementFqName ?: "Any"))
                            }
                        }
                        TypeChecker.isCustomObjectType(fqName) -> {
                            val nestedClass = returnType.symbol.psi as? KtClass
                            outputMap[propertyName] = nestedClass?.toMapK2(depth + 1) ?: mapOf("type" to fqName)
                        }
                        else -> {
                            outputMap[propertyName] = getDefaultAnyValueForType(fqName)
                        }
                    }
                }
                else -> {
                    val typeName = property.typeReference?.text ?: "Any"
                    outputMap[propertyName] = getDefaultAnyValueForType(typeName)
                }
            }
        }
        outputMap
    }
}
