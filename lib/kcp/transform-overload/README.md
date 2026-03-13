# Transform Overload

`transform-overload` 是kcp插件。
- 你先显式声明一个类型变换，比如 `S -> T` 并声明`@OverloadTransform`
- 再把 `@GenerateTransformOverloads` 标到函数或类上
- 编译器根据可用变换，自动派生 forwarding overload

例如：

```kotlin
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

会派生出类似：

```kotlin
fun sout(s: S, r: R): String = sout(s.toT(), r)
fun sout(t: T, g: G): String = sout(t, g.toR())
fun sout(s: S, g: G): String = sout(s.toT(), g.toR())
```
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
