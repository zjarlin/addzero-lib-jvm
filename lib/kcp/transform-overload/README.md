# Transform Overload

`transform-overload` 是kcp插件。
- 你先显式声明一个类型变换，比如 `S -> T` 并声明`@OverloadTransform`
- 再把 `@GenerateTransformOverloads` 标到函数或类上
- 编译器根据可用变换，自动派生 forwarding overload

## 适用场景

这个插件特别适合下面这种场景：

- 你有一个工具类、仓库类、网关类或者 DSL 入口
- 它真正的业务逻辑只想维护一个“基方法”
- 但调用方会反复要求各种“入参长得不一样、最后都能变成基方法形参”的重载形式

也就是：

- 先定义若干个显式变换，比如 `S -> T`、`G -> R`
- 再定义一个基方法，比如 `fun sout(t: T, r: R)`
- 插件会基于“哪些形参可变换、哪些不可变换”，做参数维度上的笛卡尔积组合
- 最终自动派生出一批 forwarding overload，而不是手写一堆样板代码

这种模式很适合“核心逻辑只有一份，但输入形式很多”的 API 设计。

例如：

```kotlin
data class T(val value: String)
data class R(val value: Int)
data class S(val value: String)
data class G(val value: Int)

假设存在以下变换

@OverloadTransform
fun S.toT(): T = T(value)

@OverloadTransform
fun G.toR(): R = R(value)

@GenerateTransformOverloads
interface SoutExample {
    fun sout(t: T, r: R): String
}
```

会派生出类似：

```kotlin
fun sout(s: S, r: R): String = sout(s.toT(), r)
fun sout(t: T, g: G): String = sout(t, g.toR())
fun sout(s: S, g: G): String = sout(s.toT(), g.toR())
```

也就是说，基方法是：

```kotlin
fun sout(t: T, r: R): String
```

如果第一个参数可以由 `S -> T` 变换，第二个参数可以由 `G -> R` 变换，那么插件会自动得到：

```kotlin
fun sout(s: S, r: R): String = sout(s.toT(), r)
fun sout(t: T, g: G): String = sout(t, g.toR())
fun sout(s: S, g: G): String = sout(s.toT(), g.toR())
```

这正是“一个基方法 + 多个形参变换 + 笛卡尔积重载派生”的核心模型。

这里因为没有 JVM 擦除冲突，所以派生方法仍然叫 `sout`。
只有发生擦除冲突时，才会改名成 `xxxViaConverterSuffix`。

注意：
- 当前实现会做多参数笛卡尔积派生
- 内建 lifting 支持 `Iterable`、`Collection`、`List`、`Set`、`Sequence`
- JVM 擦除冲突时会自动改名为 `xxxViaConverterSuffix`

重点：
- 业务项目正常接入时，通常只需要应用 Gradle 插件 `site.addzero.kcp.transform-overload`
- `annotations` 依赖会由 Gradle 子插件自动加到常见配置里
- `compiler plugin` artifact 通常不需要业务项目手写依赖
- IDEA 插件不是 Maven 依赖，不通过 `implementation(...)` 引入

```kotlin
plugins {
    kotlin("jvm") version "<your-kotlin-version>"
    id("site.addzero.kcp.transform-overload") version "<transform-overload-version>"
}
```

## 最小可用示例

```kotlin
import site.addzero.kcp.transformoverload.annotations.GenerateTransformOverloads
import site.addzero.kcp.transformoverload.annotations.OverloadTransform

data class T(val value: String)
data class R(val value: Int)
data class S(val value: String)
data class G(val value: Int)

@OverloadTransform
fun S.toT(): T = T(value)

@OverloadTransform
fun G.toR(): R = R(value)

@GenerateTransformOverloads
interface SoutExample {
    fun sout(t: T, r: R): String
}
```

## 插件怎么用
如果你是业务项目使用方，照这个顺序接就行：
 业务模块应用 `id("site.addzero.kcp.transform-overload") version "<version>"`
 给变换加`@OverloadTransform`给要变换的函数加`@GenerateTransformOverloads`
 如果你想在 IDEA 里也看见这些派生重载，再安装 IDEA 插件 zip
