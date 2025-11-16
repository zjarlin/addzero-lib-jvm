# KMP-IOC
这是一个KMP跨平台的简易IOC实现，用于自动收集和管理带有 `@Bean` 注解的函数、类和对象。
## 功能特性
- 自动扫描项目中带有 `@Bean` 注解的函数、类和对象
- 支持多种类型的初始化项：
  - 顶层函数
  - 无参构造函数的类实例
  - 对象实例
  - 伴生对象中的函数
- 区分处理挂起函数（suspend）和可组合函数（@Composable）
- 生成统一的容器对象用于初始化管理
## 使用方法
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
suspend fun hello3() = {
    println("hello3")
}


@Bean
class Hello6 {

}

@Bean
object Hello5 {

}


@Bean
@Composable
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


2. 生成之后,在应用程序启动时调用生成的容器中的就可以初始化啦

```kotlin
// 对于普通函数和对象
IocContainer.iocAllStart()

// 项目中包含挂起函数的情况 , 不会调用 Compose 函数
// 需要手动调用 Compose 函数初始化方法
```

## 如果不使用 `IocContainer.iocAllStart()`，则需要手动在适当位置调用各个初始化方法，例如在 Compose 应用的尾部：

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

