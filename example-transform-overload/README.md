# Transform Overload KCP 插件示例

这个示例只演示一件事：

- 已声明 `S -> T`
- 已声明 `G -> R`
- 原始函数是 `fun sout(t: T, r: R)`
- 插件会对可变换参数做笛卡尔积派生

## 示例代码

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

编译后会生成这 3 个派生重载：

```kotlin
fun sout(s: S, r: R): String = sout(s.toT(), r)
fun sout(t: T, g: G): String = sout(t, g.toR())
fun sout(s: S, g: G): String = sout(s.toT(), g.toR())
```

因为这里没有 JVM 擦除冲突，所以生成方法仍然叫 `sout`，不会改名成 `Via...`。

## 运行

```bash
./gradlew :example-transform-overload:run
./gradlew :example-transform-overload:test
```

`run` 会打印：

- 原始调用结果
- 3 个派生重载调用结果
- 反射看到的 `sout(T, R)` / `sout(S, R)` / `sout(T, G)` / `sout(S, G)`

## 项目说明

- `settings.gradle.kts` 里的 `pluginManagement` 会先查 `mavenLocal()`
- 通过 `id("site.addzero.kcp.transform-overload")` 应用插件
- 注解依赖由 Gradle 子插件自动补入
