# KCP I18N

`kcp-i18n` 是运行时国际化方案。

- `kcp-i18n`
  Kotlin 编译器插件，扫描源码里的字符串字面量并改写成运行时翻译调用，同时生成源码 catalog。
- `kcp-i18n-runtime`
  运行时翻译库，按当前 locale 从 `resources` 读取翻译，缺失时回退到源码原文。
- `kcp-i18n-gradle-plugin`
  业务项目直接接入的 Gradle 插件，负责透传编译参数、自动补 runtime 依赖，并提供语言文件同步/校验任务。
- `kcp-i18n-idea-plugin`
  IDEA companion plugin，用来辅助 IDE 导入和分析刷新。

## 业务项目接法

不要手写 `-Xplugin`，直接接 Gradle 插件：

```kotlin
plugins {
    kotlin("jvm") version "2.3.20-RC"
    id("site.addzero.kcp.i18n") version "<version>"
}

i18n {
    resourceBasePath.set("i18n")
    managedLocales.addAll("en", "ja")
    scanScope.set("composableOnly")
}
```

如果你是通过 `plugins { id(...) }` 方式解析插件，`settings.gradle.kts` 里要保证 `pluginManagement.repositories` 包含 `mavenCentral()`：

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
```

这个插件会自动做三件事：

1. 把 `resourceBasePath`、`scanScope`、注解规则传给编译器插件
2. 自动补 `site.addzero:kcp-i18n-runtime`
3. 注册 `syncI18nLocales` / `checkI18nLocales`

`targetLocale` 还保留着，只是旧的单语言别名；新项目直接用 `managedLocales`。

## 作用域设计

`scanScope` 控制普通字符串字面量的扫描范围：

- `"all"`：扫描 JVM 目标里的普通字符串字面量
- `"composableOnly"`：只扫描 `@Composable` 函数体里的字符串，适合 Compose 前端模块

推荐做法：

1. Compose 前端模块：`scanScope.set("composableOnly")`
2. 后端或纯服务模块：通常不要接这个插件，或者拆模块隔离
3. 业务项目默认吃内置注解规则，只有自定义注解才补配置

例如这类路径字符串默认就不应该翻译：

```kotlin
@RequestMapping("/abcd")
fun endpoint() = "/abcd"
```

插件默认内置了一组“展示型注解白名单”，这类注解的字符串会自动进入 catalog：

- `Route`
- `RouteTitle`
- `Menu`
- `MenuTitle`
- `NavTitle`
- `PageTitle`
- `TabTitle`
- `Label`
- `Placeholder`
- `Description`
- `DisplayName`
- `Help`
- `HelpText`
- `Tooltip`

插件也内置了一组“机器协议型注解黑名单”，这类注解默认不会进 i18n：

- `RequestMapping`
- `GetMapping`
- `PostMapping`
- `PutMapping`
- `DeleteMapping`
- `PatchMapping`
- `Path`
- `PathVariable`
- `RequestParam`
- `Query`
- `QueryParam`
- `Header`
- `HeaderParam`
- `SerialName`
- `JsonProperty`
- `JsonAlias`
- `JsonClassDiscriminator`
- `Column`
- `JoinColumn`
- `Table`
- `CollectionTable`
- `Named`
- `Qualifier`
- `Value`
- `Cacheable`
- `CachePut`
- `CacheEvict`
- `KafkaListener`

所以像 `@Route("用户管理")` 这种，默认就会被收集，不需要再配白名单。

如果你有自己的展示型注解，例如 `@ScreenLabel("设备中心")`，再追加到白名单：

```kotlin
i18n {
    annotationWhitelist.add("ScreenLabel")
}
```

支持两种匹配方式：

- 简名：`"Route"`
- 全限定名：`"site.addzero.example.Route"`

`annotationWhitelist` / `annotationBlacklist` 都是“追加规则”，不会覆盖内置规则。

黑名单优先级高于白名单：

```kotlin
i18n {
    annotationBlacklist.add("site.addzero.example.Route")
}
```

如果你就是想完全关闭内置规则，切成纯自定义模式：

```kotlin
i18n {
    useDefaultAnnotationRules.set(false)
    annotationWhitelist.add("Route")
}
```

## 注解翻译的工作方式

这一点要分清：

1. 注解参数不会被改写
2. 白名单命中的注解字符串只会进入 catalog
3. 运行时需要按“源码原文”去查翻译

原因很直接：注解参数必须是编译期常量，不能改写成 `i18nT(...)`。

所以：

```kotlin
@Route("用户管理")
fun userRoute() = Unit
```

编译后 `@Route("用户管理")` 还是原样保留；只是 `"用户管理"` 会进入生成的 catalog。消费方在渲染菜单、标题、路由描述时，应该这样取翻译：

```kotlin
import site.addzero.util.I8nutil

val label = I8nutil.tBySource("用户管理")
```

如果你拿到的是注解值，也一样：

```kotlin
val label = I8nutil.tBySource(route.label)
```

## 运行时模型

普通源码字符串会被改写成：

```kotlin
i18nT(key = "...", fallback = "源码原文", basePath = "i18n")
```

运行时查找顺序：

1. 当前 locale
2. 当前 locale 的语言部分，比如 `en-US -> en`
3. 源码原文 fallback

这意味着：

- 不需要维护 `zh.properties`
- `en.properties` / `ja.properties` 只维护翻译值
- 缺翻译时直接回退到源码里的中文

运行时切语言：

```kotlin
import site.addzero.util.I8nutil

I8nutil.setLocale("en")
I8nutil.setLocale("ja")
I8nutil.clearLocale()
```

按源码原文翻译：

```kotlin
I8nutil.tBySource("用户管理")
```

## 资源文件布局

源码里维护的翻译文件：

```text
src/main/resources/i18n/en.properties
src/main/resources/i18n/ja.properties
```

KMP Compose JVM 目标通常是：

```text
src/jvmMain/resources/i18n/en.properties
src/jvmMain/resources/i18n/ja.properties
```

插件在构建期还会自动放一份运行时 catalog 到输出目录：

```text
build/resources/main/i18n/_catalog.properties
```

这份 `_catalog.properties` 是自动生成的，不用手改；你删掉构建输出后，下次构建会重新生成。

## 同步与校验

不要手工补 key，直接跑：

```bash
./gradlew syncI18nLocales
```

这个任务会：

- 从源码生成 catalog
- 给每个受管语言文件补齐缺失 key
- 移除已经不存在的旧 key
- 保留已有翻译值
- 在每个 key 上方写源码原文注释，方便翻译

校验语言文件是否齐全：

```bash
./gradlew checkI18nLocales
```

它会检查每个受管语言的 key 集合是否和源码 catalog 一致。某个语言多 key 或少 key，会直接失败。

## 翻译文件怎么维护

推荐流程：

1. UI 和展示文本直接写中文源码
2. 跑一次 `syncI18nLocales`
3. 只填写 `en.properties`、`ja.properties` 里 `=` 右边的翻译值
4. 新增语言时，再把空模板同步出来补值

示例：

```properties
# 你好
Messages_helloMessage_text_你好=hello

# 用户管理
Routes_userRoute_Route_text_用户管理=User Management
```

注意：

- 中文不是单独维护的副本，源码本身就是中文 source of truth
- 翻译人员不需要自己造 key，只改 `=` 右边
- `checkI18nLocales` 会兜底拦截某个语言多词条或少词条

## 验证命令

仓库内 example：

```bash
./gradlew -p example/example-kcp-i18n test
./gradlew -p example/example-kcp-i18n run
./gradlew -p example/example-kcp-i18n run --args=en
```

Gradle 插件烟测：

```bash
./gradlew :lib:kcp:kcp-i18n-gradle-plugin:test \
  --tests site.addzero.kcp.i18n.gradle.I18NGradleSubpluginSmokeTest \
  --no-configuration-cache
```

烟测覆盖点：

- 业务工程通过插件 ID 接入
- `scanScope` 生效
- 注解白名单/黑名单生效
- 注解值不改写，但能进入 catalog
- `I8nutil.tBySource(...)` 能命中翻译
- `syncI18nLocales` / `checkI18nLocales` 能兜底 key 漂移

## IDEA Plugin

构建 IDEA companion plugin：

```bash
./gradlew :lib:kcp:kcp-i18n-idea-plugin:buildPlugin
```

产物位置：

```text
lib/kcp/kcp-i18n-idea-plugin/build/distributions/kcp-i18n-idea-plugin-<version>.zip
```

## 限制

- 当前处理的是非空字符串字面量
- `site.addzero.util.I8nutil` 自己内部的字符串不会再被改写
- 字符串模板仍然是分段翻译，例如 `"按钮已经点击 $count 次"` 会拆成多个片段
- 如果业务项目已经接了 `site.addzero.kcp.i18n`，不要再重复手写 `-Xplugin`
