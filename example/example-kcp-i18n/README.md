# example-kcp-i18n

这个 example 保留的是“仓库内自举”的低层验证方式，用来直接证明编译器插件本身已经能工作。

它验证的链路是：

1. 编译期扫描 Kotlin 字符串字面量
2. `CommandLineProcessor` 接收 `targetLocale` / `resourceBasePath`
3. IR 把字面量改写为 `site.addzero.util.i18nT(key, locale, basePath)`
4. `kcp-i18n-runtime` 在运行时读取 `src/main/resources/i18n/en.properties`

运行命令：

```bash
./gradlew :example:example-kcp-i18n:test
./gradlew :example:example-kcp-i18n:run
```

## Why It Still Uses `-Xplugin`

这个 example 的目标是验证“编译器插件核心逻辑”，所以仍然显式传 `-Xplugin`，这样最容易定位 IR 改写问题。

## Direct Business Usage

业务项目不要照着这个 example 去手写 `-Xplugin`，应该直接接 Gradle 插件：

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

这条“业务项目直接用”的链路已经由下面这条烟测验证通过：

```bash
./gradlew :lib:kcp:kcp-i18n-gradle-plugin:test \
  --tests site.addzero.kcp.i18n.gradle.I18NGradleSubpluginSmokeTest
```

它验证的是：

- 插件 ID 接入成功
- `i18n {}` DSL 生效
- runtime 自动注入成功
- 运行时翻译命中成功
