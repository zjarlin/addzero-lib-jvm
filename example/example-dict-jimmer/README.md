# example-dict-jimmer

验证 `dict-trans-spring-boot-starter` 对 Jimmer immutable 实体属性上 `@Dict` 的支持，包含顶层实体、实体列表和 `T<List<R>>` 包装对象。

运行：

```bash
./gradlew -p example/example-dict-jimmer :app:runDictJimmerSmoke --no-daemon --no-configuration-cache
```

期望输出包含：

```text
DICT_JIMMER_SMOKE_OK
```

示例重点：Kotlin Jimmer interface 属性可以直接写 `@Dict(...)`；starter 会读取 Kotlin 属性注解并输出翻译字段。
