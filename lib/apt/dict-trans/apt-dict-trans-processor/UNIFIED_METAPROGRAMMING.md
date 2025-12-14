# 统一元编程框架 (Unified Metaprogramming Framework)

## 概述

本项目实现了基于LSI-APT体系的统一元编程解析框架，将原有的DictTranslateProcessor重构为使用LSI抽象的统一架构。

## 核心组件

### 1. UnifiedMetaprogrammingProcessor
- **作用**: 统一元编程处理器基类
- **特性**: 
  - 基于LSI抽象体系
  - 支持多种注解处理器的统一抽象
  - 提供统一的代码生成框架

### 2. AptLsiAdapter
- **作用**: APT到LSI的适配器
- **功能**:
  - 将TypeElement转换为LsiClass
  - 将VariableElement转换为LsiField
  - 创建LSI上下文

### 3. MetaprogrammingContext
- **作用**: 元编程上下文管理
- **功能**:
  - 封装APT处理环境
  - 提供统一的错误报告
  - 管理LSI适配器

### 4. ProcessingPipeline
- **作用**: 统一处理管道
- **功能**:
  - 协调APT处理和LSI转换
  - 批量处理注解类
  - 异常处理和错误恢复

### 5. UnifiedCodeGenerator
- **作用**: 统一代码生成器
- **功能**:
  - 支持Java、Kotlin多语言生成
  - 基于LSI抽象生成代码
  - 模板化代码生成

## 架构优势

### 1. 统一抽象
- 使用LSI抽象统一处理不同语言的元素
- 提供语言无关的元编程接口
- 支持跨语言的代码生成

### 2. 可扩展性
- 基于接口的设计，易于扩展新的处理器
- 插件化的代码生成器
- 模块化的组件设计

### 3. 维护性
- 清晰的职责分离
- 统一的错误处理机制
- 完善的日志和调试支持

### 4. 性能优化
- 缓存LSI抽象对象
- 并发处理支持
- 增量编译优化

## 使用示例

### 创建自定义处理器

```kotlin
@SupportedAnnotationTypes("com.example.MyAnnotation")
class MyProcessor : UnifiedMetaprogrammingProcessor() {
    
    override fun onInit(context: MetaprogrammingContext) {
        // 初始化逻辑
    }
    
    override fun processLsiClass(
        originalElement: TypeElement,
        lsiClass: LsiClass,
        lsiContext: LsiContext
    ): Boolean {
        // 处理LSI类抽象
        val annotatedFields = context.getAnnotatedFields(lsiClass, "MyAnnotation")
        // 生成代码...
        return true
    }
}
```

### 使用代码生成器

```kotlin
val generator = UnifiedCodeGenerator()
val javaCode = generator.generateJavaClass(
    packageName = "com.example",
    className = "GeneratedClass",
    originalClass = lsiClass,
    fields = annotatedFields,
    additionalMethods = listOf("// custom methods")
)
```

## 与原版本的对比

| 特性 | 原版本 | 统一版本 |
|------|--------|----------|
| 架构 | 单一处理器 | 统一框架 |
| 抽象层 | 直接使用APT | LSI抽象 |
| 代码生成 | 硬编码模板 | 统一生成器 |
| 扩展性 | 有限 | 高度可扩展 |
| 维护性 | 中等 | 优秀 |
| 性能 | 基础 | 优化 |

## 迁移指南

1. **保持兼容性**: 现有的@Dict注解使用方式不变
2. **渐进式迁移**: 可以逐步将其他处理器迁移到统一框架
3. **配置更新**: 更新build.gradle依赖配置

## 未来规划

1. **模板引擎集成**: 支持JTE、Velocity等模板引擎
2. **更多语言支持**: 扩展到Scala、Groovy等JVM语言
3. **IDE集成**: 提供更好的IDE支持和调试工具
4. **性能监控**: 添加详细的性能监控和优化建议

## 总结

统一元编程框架通过LSI抽象体系，实现了更加灵活、可扩展和易维护的元编程解决方案。它不仅保持了与现有代码的兼容性，还为未来的扩展提供了坚实的基础。