# All Object Jvm Static

`all-object-jvm-static` 是一个 Kotlin compiler plugin。

- 业务代码只要应用 `site.addzero.kcp.all-object-jvm-static`
- 插件就会在 JVM 编译阶段，把当前模块里所有 `object` 成员函数隐式按 `@JvmStatic` 语义处理
- 作者继续写普通 `object XxxUtil { fun foo() {} }`
- Java 侧就可以直接写 `XxxUtil.foo()`

## 使用方式

```kotlin
plugins {
    kotlin("jvm") version "<your-kotlin-version>"
    id("site.addzero.kcp.all-object-jvm-static") version "<plugin-version>"
}
```

## 当前行为

- 只作用于 JVM compilation
- 处理源码中的 `object` 与 `companion object`
- 只处理函数，不处理属性
- 跳过已经显式写了 `@JvmStatic` 的函数
- 跳过 fake override 和 synthetic accessor

## 说明

这个插件不会把 `object` 变成纯 Java 工具类。

它做的是：

- 保留 Kotlin `object` 单例语义
- 同时给 JVM backend 提供 `@JvmStatic` 语义
- 让 Java 调用面变成静态风格
