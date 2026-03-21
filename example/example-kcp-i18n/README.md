# example-kcp-i18n

这个 example 现在已经改成“业务项目直连 Gradle 插件”的写法，不再手写 `-Xplugin`。

当前 `build.gradle.kts` 只保留两块核心配置：

```kotlin
plugins {
    id("site.addzero.kcp.i18n")
}

i18n {
    targetLocale.set("en")
    resourceBasePath.set("i18n")
}
```

## Why It Was Complex Before

之前之所以写得很底层，不是因为 `kcp-i18n` 必须那样接，而是因为当时这个 example 还在做“仓库内自举验证”：

- 直接拿编译器插件 jar
- 手写 `-Xplugin`
- 手写 `-P plugin:...`

这种方式适合排查编译器插件本体，不适合业务项目。

## Why It Still Needs `mavenLocal()`

这里虽然已经是业务接法，但它仍然在同一个 monorepo 里。

Gradle 的 `plugins { id("...") }` 在脚本解析阶段就要先把插件解析出来；这时候同仓库里的 sibling module
`:lib:kcp:kcp-i18n-gradle-plugin` 还没有被当成“已发布插件”提供给 plugin resolution。

所以仓库内 example 要模拟真实业务项目，仍然需要先把三件套发布到本机 `mavenLocal()`：

```bash
./gradlew \
  :lib:kcp:kcp-i18n:publishToMavenLocal \
  :lib:kcp:kcp-i18n-runtime:publishToMavenLocal \
  :lib:kcp:kcp-i18n-gradle-plugin:publishToMavenLocal
```

然后再跑 example：

```bash
./gradlew :example:example-kcp-i18n:test
./gradlew :example:example-kcp-i18n:run
```

## Verification

我已经按这条链路验证通过，运行输出：

```text
hello
goodbye
```

## Resource Layout

资源文件位置：

```text
src/main/resources/i18n/en.properties
```

当前示例内容：

```properties
Messages_helloMessage_text_你好=hello
Messages_farewellMessage_text_再见=goodbye
```

## Summary

- 业务项目外部接入：直接 `id("site.addzero.kcp.i18n")` 就对
- 仓库内 example：也可以写成同样形式
- 但因为它和插件源码在同一个仓库里，所以要先 `publishToMavenLocal` 一次，才能让 plugin id 被解析到
