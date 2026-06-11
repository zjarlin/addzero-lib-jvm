# tool-config-center-client

Kotlin Multiplatform 配置中心客户端。底层使用 Ktor Client，公共 API 放在 `commonMain`，可供 JVM 服务、Compose Multiplatform 前端和 Wasm 前端复用。

## 依赖

```kotlin
implementation("site.addzero:tool-config-center-client:2026.06.11")
```

## 用法

```kotlin
import site.addzero.configcenter.ConfigCenter

@kotlinx.serialization.Serializable
data class RedisConfig(
  val host: String,
  val port: Int,
)

suspend fun demo() {
  val instance = ConfigCenter("http://127.0.0.1:18080")
    .login("zjarlin", System.getenv("CONFIG_CENTER_PASSWORD"))
    .checkoutNamespace("cmp-aio.dev")

  val text: String? = instance.get("app.name")
  val timeout: Int? = instance.get("service.timeout")
  val enabled: Boolean? = instance.get("feature.enabled")
  val redis: RedisConfig? = instance.get("redis")

  instance.set("app.name", "cmp-aio")
  instance.set("service.timeout", 30)
  instance.set("feature.enabled", true)
  instance.set("redis", RedisConfig("127.0.0.1", 6379))
}
```

不要把生产密码写进源码。密码应来自环境变量、密钥系统或运行时注入。
