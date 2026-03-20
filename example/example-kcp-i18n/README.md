# example-kcp-i18n

这个 example 用来验证 `:lib:kcp:kcp-i18n` 当前真实可用的最小链路。

它验证的不是 README 里宣称的“资源包自动翻译”，而是当前插件源码里实际发生的行为：

1. 编译期扫描 Kotlin 字符串字面量
2. 将字符串改写为 `site.addzero.util.I8nutil.t("生成的资源键")`
3. 运行时由示例里的 `I8nutil` 完成 key -> 文案 映射

运行命令：

```bash
./gradlew :example:example-kcp-i18n:test
./gradlew :example:example-kcp-i18n:run
```

当前限制：

- 还没有独立的 Gradle subplugin，所以示例通过 `-Xplugin=...` 直接挂载 compiler plugin jar
- 还没有 `CommandLineProcessor`，因此 `targetLocale` / `resourceBasePath` 配置暂时不能从 Gradle 参数真正传入
- 资源文件不会在编译期被读取；当前实际行为是“生成 key + 运行时 lookup”
