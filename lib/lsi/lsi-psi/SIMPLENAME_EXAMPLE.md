# PsiAnnotation.simpleName 扩展属性使用示例

## 新增扩展属性

在 `PsiAnnoExt.kt` 中新增了两个便捷的扩展属性：

### 1. `simpleName: String?`

获取注解的简单名称（不含包名）。

```kotlin
val PsiAnnotation.simpleName: String?
    get() = nameReferenceElement?.referenceName 
        ?: qualifiedName?.substringAfterLast('.')
```

### 2. `simpleNameOrEmpty: String`

获取注解的简单名称，如果为 null 则返回空字符串。

```kotlin
val PsiAnnotation.simpleNameOrEmpty: String
    get() = simpleName ?: ""
```

## 使用示例

### 示例 1：基本用法

```kotlin
import com.intellij.psi.PsiAnnotation
import site.addzero.lsi.psi.anno.simpleName

fun processAnnotation(annotation: PsiAnnotation) {
    // 旧方式（繁琐）
    val oldWay = annotation.qualifiedName?.substringAfterLast('.')
    
    // 新方式（简洁）
    val newWay = annotation.simpleName
    
    println("注解简单名称: $newWay")
    // 例如：对于 @org.springframework.stereotype.Service
    // 输出：注解简单名称: Service
}
```

### 示例 2：检查注解类型

```kotlin
fun isServiceAnnotation(annotation: PsiAnnotation): Boolean {
    return annotation.simpleName == "Service"
}

fun isEntityAnnotation(annotation: PsiAnnotation): Boolean {
    return annotation.simpleName in setOf("Entity", "Table")
}
```

### 示例 3：过滤注解

```kotlin
fun filterSwaggerAnnotations(annotations: Array<PsiAnnotation>): List<PsiAnnotation> {
    return annotations.filter { anno ->
        anno.simpleName in setOf("ApiModelProperty", "Schema", "Api", "ApiOperation")
    }
}
```

### 示例 4：提取注解信息

```kotlin
fun extractAnnotationInfo(annotation: PsiAnnotation): String {
    val name = annotation.simpleName ?: "Unknown"
    val value = annotation.getArg("value") ?: ""
    return "$name: $value"
}

// 使用 simpleNameOrEmpty 避免 null 检查
fun extractAnnotationInfoSafe(annotation: PsiAnnotation): String {
    val name = annotation.simpleNameOrEmpty  // 不会是 null
    val value = annotation.getArg("value") ?: ""
    return "$name: $value"
}
```

### 示例 5：在现有代码中简化

**之前的代码**：
```kotlin
fun hasShortName(annotation: PsiAnnotation, vararg shortNames: String): Boolean {
    val shortName = annotation.qualifiedName?.substringAfterLast('.') ?: return false
    return shortName in shortNames
}
```

**现在可以简化为**：
```kotlin
fun hasShortName(annotation: PsiAnnotation, vararg shortNames: String): Boolean {
    return annotation.simpleName in shortNames
}
```

### 示例 6：注解分组

```kotlin
fun groupAnnotationsByType(annotations: Array<PsiAnnotation>): Map<String, List<PsiAnnotation>> {
    return annotations.groupBy { it.simpleNameOrEmpty }
}

// 使用
val grouped = groupAnnotationsByType(field.annotations)
println("Swagger 注解: ${grouped["ApiModelProperty"]?.size ?: 0} 个")
println("JPA 注解: ${grouped["Column"]?.size ?: 0} 个")
```

### 示例 7：在 DDL Generator 中使用

```kotlin
import site.addzero.lsi.psi.anno.simpleName

fun extractColumnComment(annotations: Array<PsiAnnotation>): String? {
    for (annotation in annotations) {
        when (annotation.simpleName) {
            "ApiModelProperty", "Schema" -> {
                return annotation.getArg("value")
            }
            "ExcelProperty" -> {
                return annotation.getArg("value")
            }
            "Column" -> {
                // 继续检查其他注解
            }
        }
    }
    return null
}
```

### 示例 8：调试和日志

```kotlin
fun logAnnotations(annotations: Array<PsiAnnotation>) {
    annotations.forEach { anno ->
        println("注解: ${anno.simpleNameOrEmpty}")
        println("  完全限定名: ${anno.qualifiedName}")
        println("  属性: ${anno.getArg("value")}")
    }
}
```

## 优势对比

### 之前的方式

```kotlin
// 方式 1：使用 qualifiedName
val shortName = annotation.qualifiedName?.substringAfterLast('.')

// 方式 2：使用 nameReferenceElement
val shortName = annotation.nameReferenceElement?.referenceName

// 需要手动选择哪种方式，或者写回退逻辑
```

### 现在的方式

```kotlin
// 统一、简洁的 API
val shortName = annotation.simpleName

// 或者使用非空版本
val shortName = annotation.simpleNameOrEmpty
```

## 实现细节

- **优先级**：优先使用 `nameReferenceElement?.referenceName`，失败时回退到 `qualifiedName?.substringAfterLast('.')`
- **性能**：扩展属性是内联的，没有额外开销
- **兼容性**：与现有代码完全兼容，不影响任何现有功能
- **一致性**：与 LSI 体系的 `LsiAnnotation.simpleName` 保持一致

## 相关文件

- 实现位置：`lsi-psi/src/main/kotlin/site/addzero/util/lsi_impl/impl/psi/anno/PsiAnnoExt.kt`
- LSI 接口：`lsi-core/src/main/kotlin/site/addzero/util/lsi/anno/LsiAnnotation.kt`

## 注意事项

1. `simpleName` 可能返回 `null`（如果注解没有名称）
2. 如果需要非空值，使用 `simpleNameOrEmpty`
3. 简单名称不含包名，如果需要完整名称使用 `qualifiedName`

---

**添加时间**: 2025-11-23  
**作者**: Droid (Factory AI)
