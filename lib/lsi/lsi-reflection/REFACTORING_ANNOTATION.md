# LSI Reflection 注解重构总结

## 重构目标

将反射模块中分散的注解相关代码统一向上抽象到 LSI 层，使注解处理更加规范和统一。

## 重构前的问题

1. **目录结构混乱**: `ClazzLsiAnnotation` 位于 `kt/anno/` 而非根目录的 `anno/`
2. **扩展方法分散**: 注解扩展方法分散在 `RefAnnoExt.kt` 和 `kt/anno/AnnoExt.kt` 两个文件中
3. **缺少LSI转换**: 没有提供 `Annotation` 到 `LsiAnnotation` 的统一转换方法
4. **命名不一致**: `AnnoExt.kt` 命名不规范

## 重构内容

### 1. 目录结构重组 ✅

**移动前**:
```
lsi-reflection/src/main/kotlin/site/addzero/util/lsi_impl/impl/reflection/
├── anno/
│   └── RefAnnoExt.kt
└── kt/
    └── anno/
        ├── AnnoExt.kt
        └── ClazzLsiAnnotation.kt
```

**移动后**:
```
lsi-reflection/src/main/kotlin/site/addzero/util/lsi_impl/impl/reflection/
└── anno/
    ├── AnnotationExt.kt        (✅ 合并重命名)
    └── ClazzLsiAnnotation.kt   (✅ 移动到正确位置)
```

### 2. 文件整合

#### `ClazzLsiAnnotation.kt`

```kotlin
package site.addzero.util.lsi_impl.impl.reflection.anno

import site.addzero.util.lsi.anno.LsiAnnotation

/**
 * 基于 Java Annotation 反射的 LsiAnnotation 实现
 */
class ClazzLsiAnnotation(private val annotation: Annotation) : LsiAnnotation {
    override val simpleName: String?
        get() = annotation.annotationClass.simpleName

    override val qualifiedName: String?
        get() = annotation.annotationClass.qualifiedName

    override val attributes: Map<String, Any?>
        get() = annotation.attributes()

    override fun getAttribute(name: String): Any? {
        return attributes[name]
    }

    override fun hasAttribute(name: String): Boolean {
        return attributes.containsKey(name)
    }
}
```

#### `AnnotationExt.kt`

合并了 `RefAnnoExt.kt` 和 `kt/anno/AnnoExt.kt` 的所有功能，并按功能分类组织：

```kotlin
package site.addzero.util.lsi_impl.impl.reflection.anno

/**
 * Java Annotation 反射扩展函数
 */

// ==================== LSI 转换 ====================

/**
 * 将 Java Annotation 转换为 LsiAnnotation
 */
fun Annotation.toLsiAnnotation(): LsiAnnotation = ClazzLsiAnnotation(this)

/**
 * 将 Annotation 数组转换为 LsiAnnotation 列表
 */
fun Array<Annotation>.toLsiAnnotations(): List<LsiAnnotation> = map { it.toLsiAnnotation() }

// ==================== 注解属性访问 ====================

fun Annotation.getArg(methodName: String): String?
fun Annotation.getArg(): String?
fun Annotation.attributes(): Map<String, Any?>

// ==================== 字段注释提取 ====================

fun Array<Annotation>.fieldComment(): String?

// ==================== 注解匹配 ====================

fun Annotation.isTargetAnnotation(targetFqName: String): Boolean
fun Annotation.isTargetAnnotationBySimpleName(simpleName: String): Boolean

// ==================== 注解名称访问 ====================

fun Annotation.qualifiedName(): String
fun Annotation.simpleName(): String
```

### 3. 更新所有引用

修复了以下文件的 import 语句：

| 文件 | 修改 |
|------|------|
| `ClazzLsiClass.kt` | `kt.anno.ClazzLsiAnnotation` → `anno.ClazzLsiAnnotation` |
| `ClazzLsiField.kt` | `kt.anno.ClazzLsiAnnotation` → `anno.ClazzLsiAnnotation` |
| `ClazzLsiType.kt` | `kt.anno.ClazzLsiAnnotation` → `anno.ClazzLsiAnnotation` |
| `ClazzLsiMethod.kt` | `kt.anno.ClazzLsiAnnotation` → `anno.ClazzLsiAnnotation` |
| `ClazzExt.kt` | `kt.anno.getArg` → `anno.getArg` |
| `FieldExt.kt` | `kt.anno.getArg` → `anno.getArg` |
| `FieldExt.kt` | 添加 `anno.fieldComment` import |
| `MethodExt.kt` | `kt.anno.fieldComment` → `anno.fieldComment` |

### 4. 新增功能

#### 统一的 LSI 转换入口

```kotlin
// 单个注解转换
val annotation: Annotation = ...
val lsiAnnotation: LsiAnnotation = annotation.toLsiAnnotation()

// 批量转换
val annotations: Array<Annotation> = ...
val lsiAnnotations: List<LsiAnnotation> = annotations.toLsiAnnotations()
```

#### 按简单名称匹配注解

```kotlin
// 原来只能按全限定名匹配
annotation.isTargetAnnotation("org.springframework.web.bind.annotation.RestController")

// 现在可以按简单名称匹配（用于 JSpecify 等）
annotation.isTargetAnnotationBySimpleName("Nullable")
annotation.isTargetAnnotationBySimpleName("NonNull")
```

## 重构后的优势

### 1. 结构清晰

- ✅ 注解相关代码统一在 `anno/` 目录
- ✅ 文件命名规范（`AnnotationExt.kt`）
- ✅ 目录层级合理

### 2. 功能完整

- ✅ 提供了 `Annotation` → `LsiAnnotation` 的统一转换
- ✅ 合并了分散的扩展方法
- ✅ 按功能分类，代码组织清晰

### 3. 易于使用

```kotlin
// 在其他 LSI 实现（如 ClazzLsiField）中使用
override val annotations: List<LsiAnnotation>
    get() = clazzField.annotations.map { ClazzLsiAnnotation(it) }

// 或者使用扩展方法
override val annotations: List<LsiAnnotation>
    get() = clazzField.annotations.toLsiAnnotations()
```

### 4. 可扩展性

- 统一的注解处理入口，便于添加新功能
- 清晰的分类（转换、属性访问、匹配、名称访问）
- 符合 LSI 的抽象设计理念

## 验证

### 编译验证

```bash
./gradlew :checkouts:lsi:lsi-reflection:compileKotlin
```

结果：✅ **BUILD SUCCESSFUL**

### 使用验证

所有使用 `ClazzLsiAnnotation` 的地方（`ClazzLsiClass`、`ClazzLsiField`、`ClazzLsiType`、`ClazzLsiMethod`）都正常工作。

## 相关文件

### 创建的文件

- `lsi-reflection/src/main/kotlin/site/addzero/util/lsi_impl/impl/reflection/anno/AnnotationExt.kt`
- `lsi-reflection/REFACTORING_ANNOTATION.md` (本文档)

### 移动的文件

- `ClazzLsiAnnotation.kt`: `kt/anno/` → `anno/`
- `AnnoExt.kt` → `AnnotationExt.kt` (重命名并合并)

### 删除的文件

- `lsi-reflection/src/main/kotlin/site/addzero/util/lsi_impl/impl/reflection/anno/RefAnnoExt.kt` (内容已合并)
- `lsi-reflection/src/main/kotlin/site/addzero/util/lsi_impl/impl/reflection/kt/` (空目录删除)

## 后续建议

### 1. 统一使用 `toLsiAnnotation()`

建议在所有 LSI 实现类中使用扩展方法，而不是直接实例化：

```kotlin
// 推荐
override val annotations: List<LsiAnnotation>
    get() = field.annotations.toLsiAnnotations()

// 不推荐（虽然也可以）
override val annotations: List<LsiAnnotation>
    get() = field.annotations.map { ClazzLsiAnnotation(it) }
```

### 2. 考虑添加缓存

如果注解访问频繁，可以考虑在 `ClazzLsiAnnotation` 中缓存 `attributes`：

```kotlin
class ClazzLsiAnnotation(private val annotation: Annotation) : LsiAnnotation {
    private val _attributes: Map<String, Any?> by lazy {
        annotation.attributes()
    }
    
    override val attributes: Map<String, Any?>
        get() = _attributes
}
```

### 3. 完善注解匹配

可以考虑添加更多匹配方式：

```kotlin
// 按包名前缀匹配
fun Annotation.isInPackage(packagePrefix: String): Boolean

// 按多个可能的全限定名匹配
fun Annotation.isAnyOf(vararg fqNames: String): Boolean
```

## 总结

本次重构成功地将 LSI Reflection 模块的注解相关代码进行了统一抽象和规范化：

1. ✅ **目录结构更清晰**: 注解代码统一在 `anno/` 目录
2. ✅ **功能更完整**: 提供了 LSI 转换、属性访问、匹配、名称访问等完整功能
3. ✅ **代码更规范**: 统一命名、分类组织、注释完整
4. ✅ **易于扩展**: 为后续功能扩展打下良好基础

这使得 LSI Reflection 的注解处理与其他 LSI 实现（PSI、Kotlin PSI）保持了一致的抽象层次和使用方式。
