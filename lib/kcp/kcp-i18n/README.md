# KCP I18N

`kcp-i18n` 现在是运行时语言切换方案，不再把目标语言编进字节码。

- `kcp-i18n`
  Kotlin 编译器插件，负责把源码里的字符串字面量改写成运行期翻译调用，并生成源码语言目录。
- `kcp-i18n-runtime`
  运行时翻译库，负责根据当前 locale 从 `resources` 读取翻译，缺失时回退到源码原文。
- `kcp-i18n-gradle-plugin`
  业务项目直接接的 Gradle 插件，负责透传编译器参数、自动补运行时依赖，并提供语言文件同步/校验任务。
- `kcp-i18n-idea-plugin`
  IDEA companion plugin，用来在 IDE 启动后刷新分析并输出插件检测日志。

## Business Usage

业务项目推荐直接接 `site.addzero.kcp.i18n`，不要再手写 `-Xplugin`。

```kotlin
plugins {
    kotlin("jvm") version "2.3.20-RC"
    id("site.addzero.kcp.i18n") version "2026.10329.101"
}

i18n {
    resourceBasePath.set("i18n")
    managedLocales.addAll("en", "ja")
    scanScope.set("composableOnly")
}
```

这个 Gradle 插件会自动做三件事：

1. 把 `resourceBasePath` 传给编译器插件
2. 自动给业务项目补上 `site.addzero:kcp-i18n-runtime`
3. 注册 `syncI18nLocales` / `checkI18nLocales`

`targetLocale` 还保留着，只作为兼容旧脚本的单语言别名；新项目直接用 `managedLocales`。

`scanScope` 用来控制扫描范围：

- `"all"`: 扫描当前编译目标里的普通字符串字面量
- `"composableOnly"`: 只扫描 `@Composable` 函数里的字符串，适合 Compose 前端模块

如果你的主要诉求是 Compose UI 国际化，推荐直接配成：

```kotlin
i18n {
    managedLocales.addAll("en", "ja")
    scanScope.set("composableOnly")
}
```

## Runtime Model

编译后源码里的中文会被改写成：

```kotlin
i18nT(key = "...", fallback = "源码原文", basePath = "i18n")
```

运行时读取顺序：

1. 当前设置的 locale
2. 当前 locale 的语言部分，比如 `en-US -> en`
3. 源码原文 fallback

这意味着：

- 不需要维护 `zh.properties`
- `en.properties` / `ja.properties` 只维护翻译值
- 某个 key 没翻译时，界面会直接回退到源码里的中文

运行时切语言示例：

```kotlin
import site.addzero.util.I8nutil

I8nutil.setLocale("en")
I8nutil.setLocale("ja")
I8nutil.clearLocale()
```

## Scope Design

当前推荐的作用域设计是：

1. 前端 Compose 模块：`scanScope.set("composableOnly")`
2. 后端 / 路由 / 控制器模块：不要接这个插件，或者保持单独模块隔离
3. 注解参数永远跳过，不参与 i18n 改写

例如下面这种不会被改写：

```kotlin
@RequestMapping("/abcd")
fun endpoint() = "/abcd"
```

因为 `"/abcd"` 是注解参数；而在 `composableOnly` 模式下，普通函数体里的字符串也不会被改写。

## Resource Layout

翻译资源默认放在：

```text
src/main/resources/i18n/en.properties
```

KMP Compose 模块通常对应：

```text
src/jvmMain/resources/i18n/en.properties
src/jvmMain/resources/i18n/ja.properties
```

示例：

```properties
# 你好
Messages_helloMessage_text_你好=hello

# 再见
Messages_farewellMessage_text_再见=goodbye
```

注意这里的中文只是注释，不是数据副本。真正的中文 source of truth 在 Kotlin 源码里。

## Sync And Check

不要手写补 key，直接让插件生成：

```bash
./gradlew syncI18nLocales
```

这个任务会：

- 从编译器插件生成的源码语言目录同步所有 key
- 给每个受管语言文件补齐缺失 key
- 移除已经不存在的旧 key
- 保留已有翻译值
- 在每个 key 上方写源码原文注释，方便翻译

语言一致性校验：

```bash
./gradlew checkI18nLocales
```

它会检查每个受管语言的 key 集合是否与源码目录一致。这个任务已经挂到 `check` 上，某个语言多词条或少词条时会直接失败。

## Key Rule

当前 key 规则还是：

```text
文件名_函数名_调用组件名_text_原始字符串
```

例如：

```text
Messages_helloMessage_text_你好
```

消费方不需要自己补 key，只需要改 `=` 右边的翻译值。

## Example

源码：

```kotlin
fun helloMessage(): String = "你好"
fun farewellMessage(): String = "再见"
```

运行时切到 `en` 时命中 `en.properties`，切到 `ja` 时命中 `ja.properties`，切回 `zh` 或缺翻译时回退到源码里的中文。

## Verification

仓库里现在有两条验证链路：

1. 仓库内 example 验证

```bash
./gradlew -p example/example-kcp-i18n test
./gradlew -p example/example-kcp-i18n run
./gradlew -p example/example-kcp-i18n run --args=en
```

其中：

- `run` 用来验证缺翻译时会回退源码中文
- `run --args=en` 用来验证运行时切到英文后能命中 `en.properties`
- 这个 example 默认走 composite build，不需要先 `publishToMavenLocal`

2. 业务项目直连 Gradle 插件验证

```bash
./gradlew :lib:kcp:kcp-i18n-gradle-plugin:test \
  --tests site.addzero.kcp.i18n.gradle.I18NGradleSubpluginSmokeTest
```

第二条烟测验证的是：

- 业务工程通过插件 ID 接入
- `i18n { managedLocales }` DSL 生效
- runtime 自动注入成功
- `syncI18nLocales` 会补齐语言文件
- `checkI18nLocales` 会拦截 key 漂移

## IDEA Plugin

构建 IDEA companion plugin：

```bash
./gradlew :lib:kcp:kcp-i18n-idea-plugin:buildPlugin
```

产物位置：

```text
lib/kcp/kcp-i18n-idea-plugin/build/distributions/kcp-i18n-idea-plugin-2026.10329.101.zip
```

安装后它会：

- 检测 Kotlin facet 里是否带上了 `kcp-i18n` 编译器插件 classpath
- 在项目启动后刷新 PSI / Daemon 分析

## Notes

- 当前会处理非空字符串字面量。
- 当前不会改写 `site.addzero.util.I8nutil` 自己内部的字符串。
- 当前字符串模板还是分段翻译，比如 `"按钮已经点击 $count 次。"` 还会拆成前后两段；对语序差异特别大的语言还不够理想。
- 如果业务项目直接接 `site.addzero.kcp.i18n`，就不要再重复手写 `-Xplugin`。
