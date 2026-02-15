# 方法特化生成器 (Method Semanticizer)

1. **`method-semanticizer-api`**: 
   - 职责：提供 `@SemanticVariation` 注解。
   - 依赖：无。适用于业务 API 接口定义层。

2. **`method-semanticizer-spi`**: 
   - 职责：提供 SPI 接口和核心数据模型。
   - 依赖：无。

3. **`method-semanticizer-spi-impl`**: 
   - 职责：提供元数据生成的工具类（如枚举叉乘、笛卡尔积展开）。
   - 依赖：`method-semanticizer-spi`。适用于 SPI 实现方。

4. **`method-semanticizer-processor`**: 
   - 职责：KSP 处理器，负责加载元数据并利用 KotlinPoet 生成代码。
   - 依赖：`api` + `spi`。


## 示例：如何实现复杂的叉乘展开
在你的 `spi-impl` 实现中，你可以利用工具类快速生成：
```kotlin
class MyJimmerProvider : SemanticMappingProvider {
    override fun getMappings(qualifiedName: String) = SemanticTable().method("save") {
        // 逻辑：将枚举叉乘后的结果填充进映射表
        val modes = SemanticHelper.expandEnum(...)
        val assocModes = SemanticHelper.expandEnum(...)
        // 笛卡尔积展开
        val cartesian = SemanticHelper.combine(modes, assocModes)
        addAll(cartesian)
    }.build()
}
```
## 核心价值
通过这种分层，我们实现了：
- **最小化依赖**：如果不使用自动展开逻辑，实现方只需依赖轻量的 `spi`。
- **逻辑复用**：叉乘展开等繁琐逻辑被封装在 `spi-impl` 中，一处编写，随处复用。
- **无感增强**：处理器逻辑保持高度稳定，通过 SPI 灵活注入元数据。
