# Multireceiver KCP 插件示例

这个示例演示 3 件事：

- 单参函数生成扩展包装
- `@Receiver` 参数生成 `context(...)` 包装
- 成员函数也能生成扩展包装

## 示例代码

```kotlin
@AddGenerateExtension
fun wrap(param: String): String = "<$param>"

@AddGenerateExtension
fun render(@Receiver service: Service, value: Int): String =
    "${service.prefix}:$value"

class Engine(private val prefix: String) {
    @AddGenerateExtension
    fun decorate(param: String): String = "$prefix[$param]"
}
```

编译后，效果等价于：

```kotlin
fun String.wrap(): String {
    val param = this
    return wrap(param)
}

context(service: Service)
fun render(value: Int): String = render(service, value)
```

实际 JVM 层的方法会带唯一的 `@JvmName(...)`，避免和原函数发生签名冲突。

当前这个 example 不演示泛型包装。
原因很直接：我已经实测到 `fun <T> describe(param: Box<T>)` 这一类案例在当前实现下调用推断还不稳定，所以示例先只保留已验证通过的形态。

## 运行前准备

先把本仓库里的 multireceiver 相关产物发布到 `mavenLocal()`：

```bash
./gradlew \
  :lib:kcp:multireceiver:kcp-multireceiver-annotations:publishToMavenLocal \
  :lib:kcp:multireceiver:kcp-multireceiver-plugin:publishToMavenLocal \
  :lib:kcp:multireceiver:kcp-multireceiver-gradle-plugin:publishToMavenLocal
```

## 运行

在仓库根目录执行：

```bash
./gradlew -p example-multireceiver run
./gradlew -p example-multireceiver test
```

`run` 会打印：

- 单参扩展调用结果
- `context(...)` 调用结果
- 成员函数扩展调用结果
- 反射看到的生成方法名

## 项目说明

- `settings.gradle.kts` 会先查 `mavenLocal()`
- 通过 `id("site.addzero.kcp.multireceiver")` 应用插件
- 注解依赖和 `-Xcontext-parameters` 由 Gradle 子插件自动补入
