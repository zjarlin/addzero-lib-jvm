# IOC APT Processor

这是一个 APT (Annotation Processing Tool) 版本的 IOC 处理器，用于替代 KSP 版本，支持 `@Component` 和 `@Bean` 注解。

## 功能特性

- **@Component 注解**：自动注册和管理策略类
- **@Bean 注解**：自动收集函数和类的初始化逻辑
- **支持多种类型**：
  - 顶层函数
  - 无参构造函数的类实例
  - 对象实例
  - 伴生对象中的函数
- **接口继承关系**：自动检测并注册从抽象类继承的接口实现关系
- **类型安全**：使用 KClass 作为键，避免字符串错误

## 使用方法

### 1. 添加依赖

```kotlin
// 在 build.gradle.kts 中添加依赖
implementation("site.addzero:ioc-core:+")
annotationProcessor("site.addzero:ioc-apt:+")
```

### 2. 添加注解

```kotlin
import site.addzero.ioc.annotation.Component

@Component
class AlipayStrategy : PaymentStrategy {
    override fun pay(amount: Double): String {
        return "使用支付宝支付 ¥$amount"
    }
}

@Component
class WechatPayStrategy : PaymentStrategy {
    override fun pay(amount: Double): String {
        return "使用微信支付 ¥$amount"
    }
}
```

### 3. 使用生成的类

编译时会自动生成：

- **AutoBeanRegistry**：包含所有 @Component 注解的类
- **IocContainer**：包含所有 @Bean 标记的初始化方法

```kotlin
// 获取策略实例
val paymentStrategy = AutoBeanRegistry.getBean<PaymentStrategy>()

// 获取所有实现
val allPaymentStrategies = AutoBeanRegistry.injectList<PaymentStrategy>()

// 执行初始化
IocContainer.iocAllStart()
```

## 与 KSP 版本的对比

| 特性 | APT 版本 | KSP 版本 |
|------|----------|----------|
| 编译时 | 源代码阶段 | KSP 阶段 |
| 性能 | 较快，基于 Java API | 较慢，基于 Kotlin API |
| 依赖 | 只需 Kotlin stdlib | 需要 KSP 插件 |
| 跨平台 | 仅 JVM | 支持 KMP |
| 类型安全 | 支持，但较弱 | 更强的类型安全 |

## 注意事项

1. **仅支持 JVM 平台**
2. **需要 Java 8 或更高版本**
3. **生成的代码在 `site.addzero.ioc.generated` 包下**
4. **组件名称在同一个项目中应该保持唯一**
5. **确保策略类有无参构造函数**

## 支持的注解

### @Component
标记一个类为可注册的组件。

```kotlin
@Component(value: String = "")
```

### @Bean
标记函数或类为需要自动初始化的 Bean。

```kotlin
@Bean
fun initDatabase() {
    // 初始化代码
}
```

## 生成的代码示例

### AutoBeanRegistry
```kotlin
public object AutoBeanRegistry : BeanRegistry {
    // 所有方法实现...
}
```

### IocContainer
```kotlin
public object IocContainer {
    private val collectRegular = listOf(...)

    fun iocRegularStart() {
        collectRegular.forEach { it() }
    }

    fun iocAllStart() {
        // 启动所有初始化逻辑
    }
}
```