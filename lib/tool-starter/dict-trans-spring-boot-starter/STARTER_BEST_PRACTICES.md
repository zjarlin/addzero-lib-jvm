# Spring Boot Starter 最佳实践

## 概述

本文档总结了在开发Spring Boot Starter时应该遵循的最佳实践，特别是关于Bean注册和冲突避免的经验。

## 核心原则

### 1. 非侵入性
Starter应该提供合理的默认配置，但不应该强制覆盖用户的配置。

### 2. 条件化配置
使用Spring Boot的条件注解来智能地注册Bean，避免冲突。

### 3. 配置优先级
用户配置 > Starter配置 > 默认配置

## Bean注册最佳实践

### 1. 配置属性类

#### ✅ 推荐方式
```kotlin
// 配置属性类
@ConfigurationProperties(prefix = "app.feature")
class FeatureProperties(
    var enabled: Boolean = true,
    var timeout: Duration = Duration.ofSeconds(30)
)

// 配置类
@Configuration
@EnableConfigurationProperties(FeatureProperties::class)
class FeatureAutoConfiguration {
    // ...
}
```

#### ❌ 避免方式
```kotlin
// 错误：会导致重复注册
@Component
@ConfigurationProperties(prefix = "app.feature")
class FeatureProperties(...)

@Configuration
@EnableConfigurationProperties(FeatureProperties::class)  // 重复注册
class FeatureAutoConfiguration {
    // ...
}
```

### 2. 默认实现Bean

#### ✅ 推荐方式
```kotlin
@Component
@ConditionalOnMissingBean(SomeInterface::class)
class DefaultSomeImplementation : SomeInterface {
    // 默认实现
}
```

#### ❌ 避免方式
```kotlin
// 错误：会与用户实现冲突
@Component
class DefaultSomeImplementation : SomeInterface {
    // 默认实现
}
```

### 3. 自动配置类

#### ✅ 推荐方式
```kotlin
@Configuration
@ConditionalOnClass(SomeLibraryClass::class)
@ConditionalOnProperty(prefix = "app.feature", name = ["enabled"], havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(FeatureProperties::class)
class FeatureAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun featureService(properties: FeatureProperties): FeatureService {
        return DefaultFeatureService(properties)
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "app.feature", name = ["advanced"], havingValue = "true")
    fun advancedFeatureService(): AdvancedFeatureService {
        return DefaultAdvancedFeatureService()
    }
}
```

## 常用条件注解

### 1. 基于类存在的条件
```kotlin
@ConditionalOnClass(SomeClass::class)          // 类路径中存在指定类
@ConditionalOnMissingClass("com.example.SomeClass")  // 类路径中不存在指定类
```

### 2. 基于Bean的条件
```kotlin
@ConditionalOnBean(SomeService::class)         // 容器中存在指定类型的Bean
@ConditionalOnMissingBean(SomeService::class)  // 容器中不存在指定类型的Bean
```

### 3. 基于属性的条件
```kotlin
@ConditionalOnProperty(
    prefix = "app.feature",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true  // 属性不存在时的默认行为
)
```

### 4. 基于Web环境的条件
```kotlin
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnNotWebApplication
```

## 配置优先级设计

### 1. 属性配置优先级
```yaml
# application.yml (用户配置，最高优先级)
app:
  feature:
    enabled: true
    timeout: 60s
    custom-setting: user-value

# starter默认配置 (最低优先级)
app:
  feature:
    enabled: true
    timeout: 30s
```

### 2. Bean配置优先级
```kotlin
@Configuration
class FeatureAutoConfiguration {

    // 用户可以通过定义同名Bean来覆盖
    @Bean
    @ConditionalOnMissingBean(name = ["featureService"])
    fun featureService(): FeatureService {
        return DefaultFeatureService()
    }

    // 用户可以通过实现接口来覆盖
    @Bean
    @ConditionalOnMissingBean(FeatureService::class)
    fun defaultFeatureService(): FeatureService {
        return DefaultFeatureService()
    }
}
```

## 错误处理和诊断

### 1. 提供清晰的错误信息
```kotlin
@Configuration
@ConditionalOnClass(RequiredLibrary::class)
class FeatureAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun featureService(): FeatureService {
        try {
            return DefaultFeatureService()
        } catch (e: Exception) {
            throw IllegalStateException(
                "Failed to create FeatureService. " +
                "Please check your configuration or provide a custom implementation.", e
            )
        }
    }
}
```

### 2. 配置验证
```kotlin
@ConfigurationProperties(prefix = "app.feature")
@Validated
class FeatureProperties(
    @field:NotBlank
    var endpoint: String = "",
    
    @field:Min(1)
    @field:Max(3600)
    var timeout: Int = 30
)
```

## 文档和示例

### 1. README.md 结构
```markdown
# Feature Starter

## 快速开始
### 添加依赖
### 基本配置
### 使用示例

## 配置参考
### 配置属性
### 高级配置

## 自定义扩展
### 自定义实现
### 自定义配置

## 故障排除
### 常见问题
### 调试技巧
```

### 2. 配置示例
```kotlin
// 提供完整的配置示例
@TestConfiguration
class FeatureTestConfiguration {
    
    @Bean
    @Primary
    fun testFeatureService(): FeatureService {
        return MockFeatureService()
    }
}
```

## 测试策略

### 1. 自动配置测试
```kotlin
@SpringBootTest
@TestPropertySource(properties = [
    "app.feature.enabled=true",
    "app.feature.timeout=60"
])
class FeatureAutoConfigurationTest {

    @Autowired
    private lateinit var featureService: FeatureService

    @Test
    fun `should auto configure feature service`() {
        assertThat(featureService).isNotNull
        assertThat(featureService).isInstanceOf(DefaultFeatureService::class.java)
    }
}
```

### 2. 条件配置测试
```kotlin
@SpringBootTest
@TestPropertySource(properties = ["app.feature.enabled=false"])
class FeatureDisabledTest {

    @Test
    fun `should not create feature service when disabled`() {
        assertThat(applicationContext.getBeanNamesForType(FeatureService::class.java))
            .isEmpty()
    }
}
```

## 版本兼容性

### 1. 向后兼容
- 不要删除已有的配置属性
- 使用 `@Deprecated` 标记过时的配置
- 提供迁移指南

### 2. 版本策略
```kotlin
@ConfigurationProperties(prefix = "app.feature")
class FeatureProperties(
    var endpoint: String = "",
    
    @Deprecated("Use 'timeout' instead")
    var timeoutSeconds: Int? = null,
    
    var timeout: Duration = Duration.ofSeconds(30)
) {
    @PostConstruct
    fun migrate() {
        if (timeoutSeconds != null) {
            timeout = Duration.ofSeconds(timeoutSeconds!!.toLong())
            log.warn("Property 'timeoutSeconds' is deprecated, use 'timeout' instead")
        }
    }
}
```

## 总结

遵循这些最佳实践可以帮助你创建：
- 🔧 易于使用的Starter
- 🚀 无冲突的自动配置
- 📚 清晰的文档和示例
- 🧪 完善的测试覆盖
- 🔄 良好的版本兼容性

记住：好的Starter应该让用户感觉不到它的存在，直到他们需要自定义配置时。