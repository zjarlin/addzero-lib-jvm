# AddZero Kaleidoscope Spec

# Kaleidoscope 统一元数据抽取规范

## 🚀 核心设计

### 核心思想
- **KSP**: 使用 `KspKldResolver(resolver, environment)` 进行元数据抽取
- **APT**: 使用 `AptKldResolver(processingEnv, roundEnv)` 进行元数据抽取
- **统一接口**: 所有元数据都从 `KldResolver` 获取
- **专注元数据抽取**: 简化架构，直接使用适配器，只关心元数据获取

### 🔧 跨平台兼容性

#### ✅ 完全支持的功能 (95%的常见用例)
- 注解元素获取: `getElementsAnnotatedWith()`
- 类声明获取: `getClassDeclaration()` 
- 源文件生成: `createSourceFile()`
- 日志记录: `info()`, `warn()`, `error()`
- 编译器选项: `getOptions()`
- 基本元素操作: 注解、修饰符、层次结构等

#### ⚠️ 平台特定功能 (会根据平台抛出异常)
- **KSP不支持**: `getPackageDeclaration()` - 抛出 `UnsupportedOperationException`
- **APT不支持**: `getAllFiles()` - 抛出 `UnsupportedOperationException`
- **类型系统高级操作**: 部分方法在KSP中不支持

### 架构图
```
┌─────────────────┐    ┌─────────────────────────────┐
│   KSP Resolver  │    │  APT ProcessingEnvironment  │
└─────────┬───────┘    └─────────────┬───────────────┘
          │                          │
          ▼                          ▼
┌─────────────────┐    ┌─────────────────────────────┐
│ KspKldResolver  │    │    AptKldResolver           │
└─────────┬───────┘    └─────────────┬───────────────┘
          │                          │
          └──────────┬─────────────────┘
                     ▼
          ┌─────────────────────┐
          │     KldResolver     │
          │   统一元数据接口     │
          └─────────────────────┘
                     ▼
          ┌─────────────────────┐
          │    元数据抽取逻辑    │
          └─────────────────────┘
```

### 使用示例

#### KSP处理器
```kotlin
class MyKspProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {
    
    override fun process(resolver: Resolver): List<KSAnnotated> {
        // 直接创建KldResolver
        val kldResolver = KspKldResolver(resolver, environment)
        
        // 元数据抽取
        kldResolver.getElementsAnnotatedWith("com.example.Entity").forEach { element ->
            processEntity(element, kldResolver)
        }
        
        return emptyList()
    }
    
    private fun processEntity(element: KldElement, kldResolver: KldResolver) {
        // 元数据抽取
        val className = element.simpleName
        val packageName = element.packageName
        
        // 代码生成
        val writer = kldResolver.createSourceFile(packageName ?: "", "${className}DTO", element)
        writer.use {
            it.write("// Generated DTO for $className")
        }
    }
}
```

#### APT处理器  
```kotlin
class MyAptProcessor : AbstractProcessor() {
    
    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        // 直接创建KldResolver
        val kldResolver = AptKldResolver(processingEnv, roundEnv)
        
        // 完全相同的处理逻辑
        kldResolver.getElementsAnnotatedWith("com.example.Entity").forEach { element ->
            processEntity(element, kldResolver)
        }
        
        return true
    }
    
    private fun processEntity(element: KldElement, kldResolver: KldResolver) {
        // 与KSP完全相同的处理逻辑
        val className = element.simpleName
        val packageName = element.packageName
        
        val writer = kldResolver.createSourceFile(packageName ?: "", "${className}DTO", element)
        writer.use {
            it.write("// Generated DTO for $className")
        }
    }
}
```

## 📊 跨平台兼容性详细说明

### ✅ 完全支持的功能 (95%的用例)

Kaleidoscope规范的核心功能在KSP和APT之间能够完美兼容：

```kotlin
// ✅ 注解元素获取 - 100%兼容
val entities = kldResolver.getElementsAnnotatedWith("javax.persistence.Entity")

// ✅ 类声明获取 - 100%兼容  
val userClass = kldResolver.getClassDeclaration("com.example.User")

// ✅ 源文件生成 - 100%兼容
val writer = kldResolver.createSourceFile("com.example", "UserDTO", element)

// ✅ 日志记录 - 100%兼容
kldResolver.info("处理完成")
kldResolver.warn("注意事项")
kldResolver.error("错误信息")

// ✅ 编译器选项 - 100%兼容
val options = kldResolver.getOptions()
```

### ⚠️ 平台特定功能 (会抛出异常)

根据用户要求，无法支持的功能会明确抛出`UnsupportedOperationException`：

#### KSP平台限制

```kotlin
// ❌ KSP不支持包声明获取
try {
    val pkg = kldResolver.getPackageDeclaration("com.example")
} catch (e: UnsupportedOperationException) {
    // KSP平台不支持包元素概念，会抛出异常
    kldResolver.warn("KSP平台不支持包声明获取: ${e.message}")
}
```

#### APT平台限制

```kotlin
// ❌ APT不支持获取所有文件
try {
    val files = kldResolver.getAllFiles()
} catch (e: UnsupportedOperationException) {
    // APT无法直接访问所有源文件，会抛出异常
    kldResolver.warn("APT平台不支持文件列表获取: ${e.message}")
}
```

### 🛡️ 安全使用模式

#### 1. 使用兼容性工具类

```kotlin
// 检测平台类型
val platform = kldResolver.detectPlatform()
kldResolver.info("当前平台: $platform")

// 检查功能兼容性
val compatibility = kldResolver.checkCompatibility(KldCompatibility.Feature.GET_PACKAGE_DECLARATION)
if (compatibility == KldCompatibility.CompatibilityLevel.FULL_SUPPORT) {
    // 安全执行
}
```

#### 2. 使用安全执行函数

```kotlin
// 安全执行可能抛异常的操作
val result = kldResolver.safeExecute(
    operation = { kldResolver.getAllFiles().count() },
    onUnsupported = { errorMsg -> 
        kldResolver.warn("功能不支持: $errorMsg")
        0 // 返回默认值
    }
)
```

#### 3. 使用平台特定的安全方法

```kotlin
// 安全的包声明获取
KldCompatibility.safeGetPackageDeclaration(
    resolver = kldResolver,
    qualifiedName = "com.example",
    onSuccess = { packageElement ->
        // APT平台：正常处理包信息
        processPackage(packageElement)
    },
    onNotSupported = {
        // KSP平台：提供降级方案
        processWithoutPackageInfo()
    }
)
```

### 📋 完整兼容性矩阵

| 功能 | KSP | APT | 说明 |
|------|-----|-----|------|
| `getElementsAnnotatedWith()` | ✅ | ✅ | 核心功能，完全兼容 |
| `getClassDeclaration()` | ✅ | ✅ | 类型查找，完全兼容 |
| `createSourceFile()` | ✅ | ✅ | 代码生成，完全兼容 |
| `info/warn/error()` | ✅ | ✅ | 日志记录，完全兼容 |
| `getOptions()` | ✅ | ✅ | 编译选项，完全兼容 |
| `getPackageDeclaration()` | ❌ | ✅ | KSP抛异常 |
| `getAllFiles()` | ✅ | ❌ | APT抛异常 |
| `isProcessingOver` | ⚠️ | ✅ | KSP固定返回false |

### 🎯 使用建议

1. **优先使用核心功能**: 95%的元编程需求都能通过核心功能满足
2. **合理处理异常**: 对平台特定功能使用try-catch或安全执行函数
3. **提供降级方案**: 为不支持的功能准备备选实现
4. **明确文档说明**: 在自己的处理器中标明平台兼容性要求

### 📚 更多信息

- 查看 [`COMPATIBILITY_ANALYSIS.md`](./COMPATIBILITY_ANALYSIS.md) 了解详细的兼容性分析
- 查看 [`CrossPlatformProcessorExample.kt`](./src/commonMain/kotlin/com/addzero/kmp/kaleidoscope/examples/CrossPlatformProcessorExample.kt) 了解完整的跨平台使用示例
- 使用 `KldCompatibility` 工具类进行平台检测和安全操作

---

通过这种方式，Kaleidoscope元编程规范确保了KSP和APT的可靠兼容，对于可以兼容的部分提供统一API，对于不能兼容的部分明确抛出异常，满足了用户的设计要求。专注于元数据抽取，简化了架构设计。

## 贡献指南

欢迎提交问题和Pull Request！在贡献代码时，请确保：

1. 遵循现有的代码风格
2. 添加适当的测试用例
3. 更新相关文档
4. 确保向后兼容性

## 许可证

Apache License 2.0