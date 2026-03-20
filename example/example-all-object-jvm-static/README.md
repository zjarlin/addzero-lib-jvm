# example-all-object-jvm-static

这个 example 现在是一个独立的两模块 Gradle 工程，用来验证真实消费链路：

1. `kotlin-lib`
   Kotlin 库模块，直接应用 `site.addzero.kcp.all-object-jvm-static`
2. `java-app`
   纯 Java 模块，只依赖 `kotlin-lib`

验证目标很明确：

- Kotlin 作者只写普通 `object XxxUtil`
- Java 模块完全不引入 KCP 插件
- Java 代码直接写 `XxxUtil.method(...)`

运行方式：

```bash
./gradlew -p example/example-all-object-jvm-static :java-app:test
./gradlew -p example/example-all-object-jvm-static :java-app:run
```

关键说明：

- 这个独立 example 通过 `pluginManagement.includeBuild("../../")` 直接从仓库根工程拿 `site.addzero.kcp.all-object-jvm-static` 插件
- 所以示例里 `kotlin-lib` 用的就是最终用户看到的 Gradle id，而不是 `-Xplugin=...`
