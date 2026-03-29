# example-kcp-i18n

这个 example 用来验证一件事：业务项目只接 `site.addzero.kcp.i18n` 这个 Gradle 插件，不再手写 `-Xplugin`。

## Minimal Config

当前示例的核心配置就是这几行：

```kotlin
plugins {
    alias(libs.plugins.kotlinJvm)
    application
    id("site.addzero.kcp.i18n")
}

i18n {
    resourceBasePath.set("i18n")
    managedLocales.add("en")
}
```

插件会自动完成：

1. 透传编译器参数给 `kcp-i18n`
2. 自动补上 `site.addzero:kcp-i18n-runtime`
3. 注册 `syncI18nLocales` / `checkI18nLocales`

## How To Run

这个 example 的 [`settings.gradle.kts`](/Users/zjarlin/IdeaProjects/addzero-lib-jvm/example/example-kcp-i18n/settings.gradle.kts) 默认开启了 composite build：

```kotlin
val useIncludedBuild =
    System.getenv("ADDZERO_USE_INCLUDED_BUILD")
        ?.toBooleanStrictOrNull()
        ?: true
```

所以在当前仓库里直接跑即可，不需要先 `publishToMavenLocal`：

```bash
./gradlew -p example/example-kcp-i18n test
./gradlew -p example/example-kcp-i18n run
./gradlew -p example/example-kcp-i18n run --args=en
./gradlew -p example/example-kcp-i18n syncI18nLocales
./gradlew -p example/example-kcp-i18n checkI18nLocales
```

运行行为：

- `run` 不带参数时，示例不会主动切语言；如果当前 locale 没有翻译文件，就回退到源码里的中文
- `run --args=en` 时，示例会调用 `I8nutil.setLocale("en")`，输出英文

## Runtime Usage

示例主程序写法：

```kotlin
import site.addzero.util.I8nutil

fun main(args: Array<String>) {
    args.firstOrNull()?.let(I8nutil::setLocale)
    println(helloMessage())
    println(farewellMessage())
}
```

业务项目里通常在应用启动、用户切换语言、读取本地配置时调用：

```kotlin
I8nutil.setLocale("en")
I8nutil.setLocale("ja")
I8nutil.clearLocale()
```

## Resource Layout

翻译文件位置：

```text
src/main/resources/i18n/en.properties
```

当前示例内容：

```properties
# 你好
Messages_helloMessage_text_你好=hello

# 再见
Messages_farewellMessage_text_再见=goodbye
```

这里没有 `zh.properties`。中文原文只在 Kotlin 源码里维护，`en.properties` 只维护翻译值。

## Simulate External Consumer

如果你要模拟“仓库外业务项目从制品仓库解析插件”这条链路，再关闭 composite build：

```bash
ADDZERO_USE_INCLUDED_BUILD=false ./gradlew \
  :lib:kcp:kcp-i18n:publishToMavenLocal \
  :lib:kcp:kcp-i18n-runtime:publishToMavenLocal \
  :lib:kcp:kcp-i18n-gradle-plugin:publishToMavenLocal
```

然后再跑：

```bash
ADDZERO_USE_INCLUDED_BUILD=false ./gradlew -p example/example-kcp-i18n test
ADDZERO_USE_INCLUDED_BUILD=false ./gradlew -p example/example-kcp-i18n run --args=en
```

这一步只是为了模拟外部消费方。当前仓库内开发默认不需要这样做。
