# IOC Processor

## @Component 注解介绍

这个 IOC 容器提供了一个简单的 Bean 注册表功能，特别适合用于实现策略模式，让你无需手动编写工厂类。

### 1. 添加注解

在需要自动注册的策略类上添加 `@Component` 注解：

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

**组件名称规则**：
- 不指定名称时，默认使用类名首字母小写（如 `AlipayStrategy` → `alipayStrategy`）
- 可以通过 `@Component("customName")` 指定自定义名称

### 2. 编译时自动生成

编译时，KSP 处理器会自动生成 `AutoBeanRegistry` 类，包含所有 `@Component` 注解的类。

### 3. 使用策略

通过 `AutoBeanRegistry` 获取策略实例：

```kotlin
import site.addzero.ioc.generated.AutoBeanRegistry
import site.addzero.ioc.registry.getBean

// 方式1：通过类型获取
val paymentStrategy = AutoBeanRegistry.getBean<PaymentStrategy>()
val notificationStrategy = AutoBeanRegistry.getBean<NotificationStrategy>()

// 方式2：通过 KClass 获取
val strategy = AutoBeanRegistry.getBean(PaymentStrategy::class)

// 方式3：通过组件名称获取（需要先通过名称获取类型）
val componentType = AutoBeanRegistry.getComponentType("alipay")
val alipayStrategy = componentType?.let {
    AutoBeanRegistry.getBean(it) as PaymentStrategy
}
```

## 策略模式最佳实践

### 1. 创建策略管理器

```kotlin
object PaymentStrategyManager {
    private val registry = AutoBeanRegistry

    fun getStrategy(paymentType: String): PaymentStrategy? {
        return registry.getComponentType(paymentType)?.let { type ->
            registry.getBean(type) as PaymentStrategy
        }
    }

    fun executePayment(paymentType: String, amount: Double): String {
        return getStrategy(paymentType)?.pay(amount)
            ?: "不支持的支付方式: $paymentType"
    }
}
```

### 2. 使用策略链

```kotlin
class ValidationChain {
    private val registry = AutoBeanRegistry
    private val strategies = mutableListOf<ValidationStrategy>()

    fun addValidation(validationType: String): ValidationChain {
        registry.getComponentType(validationType)?.let { type ->
            registry.getBean<ValidationStrategy>(type)?.let { strategy ->
                strategies.add(strategy)
            }
        }
        return this
    }

    fun validate(data: String): Boolean {
        return strategies.all { it.validate(data) }
    }
}

// 使用
val result = ValidationChain()
    .addValidation("length")
    .addValidation("regex")
    .validate("test123")
```

### 3. 动态注册策略

运行时也可以动态注册新的策略：

```kotlin
// 注册单例
AutoBeanRegistry.registerBean(CustomStrategy::class, CustomStrategy())

// 注册提供者（延迟创建）
AutoBeanRegistry.registerProvider(CustomStrategy::class) {
    CustomStrategy()
}
```

### injectList - 获取所有实现

`injectList` 是最强大的功能，可以自动获取某个接口的所有实现类：

```kotlin
// 定义接口
interface DdlGenerator {
    fun generate(sql: String): String
}

// 定义实现
@Component("mysql")
class MySqlDdlGenerator : DdlGenerator { ... }

@Component("postgresql")
class PostgreSqlDdlGenerator : DdlGenerator { ... }

@Component("oracle")
class OracleDdlGenerator : DdlGenerator { ... }

// 自动注入所有实现！
val ddlGenerators = AutoBeanRegistry.injectList<DdlGenerator>()
// 或者
val ddlGenerators = AutoBeanRegistry.injectList(DdlGenerator::class)

// 使用
ddlGenerators.forEach { generator ->
    println("使用生成器: ${generator.generate("CREATE TABLE...")}")
}
```

## 注解说明

### @Component

标记一个类为可注册的组件。

```kotlin
@Component(value: String = "")
```

- `value`：组件名称，可选。如果不指定，则使用类名。

### @Bean

标记函数或类为需要自动初始化的 Bean。

```kotlin
@Bean
fun initDatabase() {
    // 初始化代码
}
```

## 生成的代码

编译后会生成：

1. `IocContainer` - 包含所有 `@Bean` 标记的初始化方法
2. `AutoBeanRegistry` - 包含所有 `@Component` 标记的类

## 优势

1. **无需手动注册** - 注解标记即可自动注册
2. **类型安全** - 使用 KClass 作为键，避免字符串错误
3. **支持延迟创建** - 可以注册提供者，实现懒加载
4. **简单易用** - 类似 Spring 的 getBean，但更轻量
5. **适合策略模式** - 完美解决策略类的注册和获取问题
6. **支持抽象类接口继承** - 自动检测并注册从抽象类继承的接口实现关系

## 泛型策略支持

IOC 容器支持泛型策略，但需要注意类型擦除的影响：

```kotlin
// 定义泛型策略接口
interface Strategy<T> {
    fun process(input: T): String
}

@Component
class StringStrategy : Strategy<String> {
    override fun process(input: String): String = input.uppercase()
}

@Component
class IntStrategy : Strategy<Int> {
    override fun process(input: Int): String = "Number: $input"
}

// 使用 - 由于类型擦除，需要手动过滤
val allStrategies = AutoBeanRegistry.injectList<Strategy<*>>()
val stringStrategies = allStrategies.filter {
    it.process("test") is String
}
```

### 类型擦除的解决方案

1. **使用类型标记**
```kotlin
interface TypedStrategy<T> : Strategy<T> {
    fun getInputType(): Class<*>
}
```

2. **使用命名约定**
```kotlin
val stringStrategies = allStrategies.filter {
    it.javaClass.simpleName.contains("String")
}
```

## 注意事项

1. 确保策略类有无参构造函数（或者所有参数都有默认值）
2. 组件名称在同一个项目中应该保持唯一
3. 生成的代码在 `site.addzero.ioc.generated` 包下
4. **泛型策略**：由于 JVM 类型擦除，`Strategy<String>` 和 `Strategy<Int>` 在运行时都是 `Strategy`，需要通过其他方式区分
5. **抽象类支持**：支持通过抽象类实现的接口继承关系，自动检测并注册

## @Bean 自动初始化功能

### 功能特性
- 自动扫描项目中带有 `@Bean` 注解的函数、类和对象
- 支持多种类型的初始化项：
  - 顶层函数
  - 无参构造函数的类实例
  - 对象实例
  - 伴生对象中的函数
- 区分处理挂起函数（suspend）和可组合函数（@Composable）
- 生成统一的容器对象用于初始化管理
- 支持抽象类接口继承关系的自动检测和注册

### 使用方法

```kotlin
kspCommonMainMetadata("site.addzero:ioc-processor:+")
//    or KMP  二选一哈,jvm就用下面的
ksp("site.addzero:ioc-processor:+")

implementation("site.addzero:ioc-core:+")
```

1. 在需要自动管理的函数、类或对象上添加 `@Bean` 注解：

```kotlin
@Bean
fun hello(): Unit {
    println("hello")
}

@Bean
fun hello1(): Unit {
    println("hello1")
}

@Composable
@Bean
fun TestText(themeViewModel: ThemeViewModel = koinInject()): Unit {
    Text("TestText")
}

@Bean
suspend fun hello2() = withContext(Dispatchers.Main) {
    println("hello2")
}

@Bean
class Hello6 {

}

@Bean
object Hello5 {

}

@Composable
@Bean
fun Hello4(menuViewModel: ChatViewModel = koinInject<ChatViewModel>()) {
    println("hello3")
}
```

### 生成的容器代码

```kotlin
public object IocContainer {
    val collectRegular = listOf(
        { site.addzero.Hello() },
        { site.addzero.hello1() }
    )

    fun iocRegularStart() {
        collectRegular.forEach { it() }
    }

    val collectClassInstance = listOf(
        { site.addzero.Hello6() }
    )

    fun iocClassInstanceStart() {
        collectClassInstance.forEach { it() }
    }

    val collectObjectInstance = listOf(
        { site.addzero.Hello5 }
    )

    fun iocObjectInstanceStart() {
        collectObjectInstance.forEach { it() }
    }

    val collectSuspend = listOf(
        suspend { site.addzero.hello2() },
        suspend { site.addzero.hello3() }
    )

    suspend fun iocSuspendStart() {
        collectSuspend.forEach { it() }
    }

    val collectComposable = listOf(
        @androidx.compose.runtime.Composable { site.addzero.TestText() },
        @androidx.compose.runtime.Composable { site.addzero.Hello4() },
        @androidx.compose.runtime.Composable { site.addzero.events.EventBusConsumer() }
    )

    @androidx.compose.runtime.Composable
    fun IocComposeableStart() {
        collectComposable.forEach { it() }
    }

    suspend fun iocAllStart() {
        iocRegularStart()
        iocClassInstanceStart()
        iocObjectInstanceStart()
        iocSuspendStart()
    }
}
```

### 初始化调用

1. 生成完成后，在应用程序启动时调用生成的容器中的方法进行初始化：

```kotlin
// 对于普通函数和对象
IocContainer.iocAllStart()

// 项目中包含挂起函数的情况，不会调用 Compose 函数
// 需要手动调用 Compose 函数初始化方法
```

2. 如果不使用 `IocContainer.iocAllStart()`，则需要手动在适当位置调用各个初始化方法，例如在 Compose 应用的尾部：

```kotlin
@Composable
fun App() {
    // ... 其他代码 ...

    // 手动调用各个初始化方法
    AddToastListener()
    EventBusConsumer()
    // ... 以下省略几百个初始化逻辑 ...
}
```

## Suspend 与 Compose 关键字冲突处理

在处理初始化函数时，我们特别注意了挂起函数（suspend）和可组合函数（@Composable）之间的冲突问题：

### 问题背景
- 挂起函数只能在协程环境中调用
- 可组合函数只能在 Compose 环境中调用
- 两者不能在同一个函数中同时使用

### 解决方案
我们采用了以下策略来处理这种冲突：

1. **分离容器**：将挂起函数和可组合函数分别存储在不同的容器中
2. **条件初始化**：
   - 当存在挂起函数时，`iocAllStart()` 方法被标记为 `suspend`，但不会调用 Compose 函数
   - 当只有 Compose 函数时，`IocAllStart()` 方法被标记为 `@Composable`
   - 当两者同时存在时，优先处理挂起函数，开发者需要在 Compose 环境中手动调用 Compose 初始化方法

### tips
- 常规后台SpringBoot项目也可以用, 如果项目中包含挂起函数，注意创建协程作用域即可
- 如果项目中同时包含 Compose 函数，和挂起函数,则 Compose 上下文需要手动调用IocComposeableStart,挂起容器的start依然会自动包含在iocAllStart中