# KCP I18N

`kcp-i18n` 已经不是 PoC 写法了，现在有一条完整可落地的业务接入链路：

- `kcp-i18n`
  Kotlin 编译器插件，负责把源码里的字符串字面量改写成运行期翻译调用。
- `kcp-i18n-runtime`
  运行时翻译库，负责从 `resources` 里的 `.properties` 读取目标语言。
- `kcp-i18n-gradle-plugin`
  业务项目直接接的 Gradle 插件，负责透传编译器参数并自动补运行时依赖。
- `kcp-i18n-idea-plugin`
  IDEA companion plugin，用来在 IDE 启动后刷新分析并输出插件检测日志。

## Business Usage

业务项目推荐直接接 `site.addzero.kcp.i18n`，不要再手写 `-Xplugin`。

```kotlin
plugins {
    kotlin("jvm") version "2.3.20-RC"
    id("site.addzero.kcp.i18n") version "2026.03.13"
}

i18n {
    targetLocale.set("en")
    resourceBasePath.set("i18n")
}
```

这个 Gradle 插件会自动做两件事：

1. 把 `targetLocale` / `resourceBasePath` 传给编译器插件
2. 自动给业务项目补上 `site.addzero:kcp-i18n-runtime`

## Resource Layout

翻译资源默认放在：

```text
src/main/resources/i18n/en.properties
```

示例：

```properties
Messages_helloMessage_text_你好=hello
Messages_farewellMessage_text_再见=goodbye
```

源码：

```kotlin
fun helloMessage(): String = "你好"
fun farewellMessage(): String = "再见"
```

编译后会被改写成对 `site.addzero.util.i18nT(...)` 的调用，运行时再命中 `.properties`。

## Key Rule

当前 key 规则是：

```text
文件名_函数名_调用组件名_text_原始字符串
```

例如：

```text
Messages_helloMessage_text_你好
```

## Verification

仓库里现在有两条验证链路：

1. 低层编译器验证

```bash
./gradlew :example:example-kcp-i18n:test
./gradlew :example:example-kcp-i18n:run
```

2. 业务项目直连 Gradle 插件验证

```bash
./gradlew :lib:kcp:kcp-i18n-gradle-plugin:test \
  --tests site.addzero.kcp.i18n.gradle.I18NGradleSubpluginSmokeTest
```

第二条烟测验证的是：

- 业务工程通过插件 ID 接入
- `i18n {}` DSL 生效
- 编译器插件参数透传成功
- runtime 自动注入成功
- 运行期能读到 `resources/i18n/en.properties`

## IDEA Plugin

构建 IDEA companion plugin：

```bash
./gradlew :lib:kcp:kcp-i18n-idea-plugin:buildPlugin
```

产物位置：

```text
lib/kcp/kcp-i18n-idea-plugin/build/distributions/kcp-i18n-idea-plugin-2026.03.13.zip
```

安装后它会：

- 检测 Kotlin facet 里是否带上了 `kcp-i18n` 编译器插件 classpath
- 在项目启动后刷新 PSI / Daemon 分析

## Notes

- 当前会处理非空字符串字面量。
- 当前不会改写 `site.addzero.util.I8nutil` 自己内部的字符串。
- 如果业务项目直接接 `site.addzero.kcp.i18n`，就不要再重复手写 `-Xplugin`。
