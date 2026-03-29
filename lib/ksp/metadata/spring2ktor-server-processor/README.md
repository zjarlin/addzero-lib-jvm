# Spring2Ktor Server Processor

用 Spring 原生 Web 注解写接口，在编译期生成干净的 Ktor Server 路由注册代码。

它不是 Spring Boot，也不会启动 Spring 容器；它只是读取 Spring 注解，然后生成 Ktor 代码。

## 适用场景

- 想保留 `@GetMapping`、`@PostMapping`、`@RequestBody` 这一套写法
- 运行时想用 Ktor，而不是 Spring MVC / Spring Boot
- 主要写 Kotlin 顶层函数，同时希望兼容少量 `@RestController`

## Gradle 接入

推荐直接使用消费插件：

```kotlin
plugins {
    kotlin("jvm")
    id("site.addzero.ksp.spring2ktor-server")
}

spring2ktorServer {
    generatedPackage.set("com.example.generated.springktor")
}
```

这个插件会自动：

- 应用 `com.google.devtools.ksp`
- 注入 `spring2ktor-server-processor`
- 注入 `spring2ktor-server-core`
- 添加 `compileOnly("org.springframework:spring-web")`

如果你需要最低层手动控制，仍然可以继续使用原始依赖写法：

```kotlin
plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation("site.addzero:spring2ktor-server-core:2026.03.13")
    ksp("site.addzero:spring2ktor-server-processor:2026.03.13")

    compileOnly("org.springframework:spring-web:5.3.21")
}
```

这里的 `compileOnly("org.springframework:spring-web:5.3.21")` 是给业务源码里的 Spring 注解和 `MultipartFile` 过编译用的。
业务模块本身不需要启动 Spring Boot；当前版本的 `spring2ktor-server-core` 仍会为 `MultipartFile` 兼容携带 `spring-web` 运行时依赖。

如果你的接口返回 JSON，通常还会需要：

```kotlin
dependencies {
    implementation("io.ktor:ktor-server-content-negotiation:3.4.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
}
```

## 最小示例

```kotlin
package demo

import kotlinx.serialization.Serializable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@Serializable
data class EchoRequest(
    val name: String,
)

@Serializable
data class EchoResponse(
    val message: String,
)

@GetMapping("/hello/{id}")
fun hello(
    @PathVariable id: Int,
    name: String,
    @RequestHeader("X-Trace") trace: String,
): String {
    return "$id:$name:$trace"
}

@PostMapping("/echo")
suspend fun echo(@RequestBody body: EchoRequest): EchoResponse {
    return EchoResponse("echo:${body.name}")
}
```

说明：

- 顶层函数可以直接写
- 顶层函数如果有公共前缀，可以在文件顶部写 `@file:RequestMapping("/base")`
- 不强制所有方法都写 `suspend`
- 未标注参数默认按 `@RequestParam` 处理，所以例子里的 `name` 等价于 `@RequestParam("name")`
- 成功时直接 `return`
- 失败时直接 `throw`
- 返回 `null` 会写成 `204 No Content`
- 返回 `Unit` 且你没有手动写响应时，会写成 `200 OK`

文件级前缀示例：

```kotlin
@file:RequestMapping("/api/demo")

package demo.routes

import org.springframework.web.bind.annotation.GetMapping
import site.addzero.springktor.runtime.RequestMapping

@GetMapping("/health")
fun readHealth(): String {
    return "ok"
}
```

## 在 Ktor 中启用

生成代码后，在你的 `Application.module` 里手动注册：

```kotlin
package demo

import demo.generated.springktor.registerGeneratedSpringRoutes
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    routing {
        registerGeneratedSpringRoutes()
    }
}
```

如果你用了 `@RestController`，控制器实例需要你自己交给 Koin 或其他 DI 容器管理。
以 Koin 为例：

```kotlin
dependencies {
    implementation("io.insert-koin:koin-ktor:<your-version>")
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    install(org.koin.ktor.plugin.Koin) {
        modules(
            org.koin.dsl.module {
                single { GreetingService() }
                single { UserController(get()) }
            }
        )
    }
    routing {
        registerGeneratedSpringRoutes()
    }
}
```

`spring2ktor-server` 不再生成 `Application.generatedSpringApplication()` 或 `generatedSpringKoinModule`；
应用装配和依赖注入完全由业务自己控制。

## 生成出来的 API

处理器只会生成这个总入口：

- `fun Route.registerGeneratedSpringRoutes()`

还会按源文件 / Controller 生成更细粒度的注册函数，例如：

- `fun Route.registerSmokeHandlersSpringRoutes()`
- `fun Route.registerSmokeControllerSpringRoutes()`

默认生成包名是：

```text
<你的业务包>.generated.springktor
```

例如源码在 `demo` 包下，默认就会生成到：

```text
demo.generated.springktor
```

也可以手动指定：

```kotlin
ksp {
    arg("springKtor.generatedPackage", "com.example.generated.springktor")
}
```

## 支持的 Spring 注解

HTTP 路由：

- `@GetMapping`
- `@PostMapping`
- `@PutMapping`
- `@DeleteMapping`
- `@PatchMapping`
- `@RequestMapping`

参数绑定：

- `@PathVariable`
- `@RequestParam`
- `@RequestBody`
- `@RequestHeader`
- `@RequestPart`
- `MultipartFile`
- `List<MultipartFile>`

类式兼容：

- `@RestController`
- 类级别 `@RequestMapping`

## 参数规则

参数解析优先级如下：

1. Ktor 上下文类型直接注入
2. 显式 Spring 参数注解按注解绑定
3. `MultipartFile` / `List<MultipartFile>` 按 multipart part 名绑定
4. 未标注参数默认按 `@RequestParam`

目前支持直接注入的 Ktor 类型包括：

- `ApplicationCall`
- `Application`
- `RoutingContext`
- `ApplicationRequest`
- `ApplicationResponse`

这意味着你可以在 Spring 风格 handler 里直接写 Ktor 原生响应逻辑。

例如文件下载：

```kotlin
import io.ktor.http.HttpHeaders
import io.ktor.http.ContentType
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.header
import io.ktor.server.response.respondBytes
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@GetMapping("/download/{name}")
suspend fun download(
    @PathVariable name: String,
    call: ApplicationCall,
) {
    call.response.header(HttpHeaders.ContentDisposition, """attachment; filename="$name.txt"""")
    call.respondBytes("hello:$name".encodeToByteArray(), ContentType.Text.Plain)
}
```

例如 SSE：

```kotlin
import io.ktor.http.ContentType
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondTextWriter
import org.springframework.web.bind.annotation.GetMapping

@GetMapping("/sse/messages")
suspend fun sseMessages(call: ApplicationCall) {
    call.respondTextWriter(contentType = ContentType.Text.EventStream) {
        write("data:hello\n\n")
        flush()
        write("data:world\n\n")
    }
}
```

## `@RestController` 兼容示例

```kotlin
package demo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

class GreetingService {
    fun greet(id: Int): String = "hello-$id"
}

@RestController
@RequestMapping("/users")
class UserController(
    private val greetingService: GreetingService,
) {
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Int): String {
        return greetingService.greet(id)
    }
}
```

这类写法至少需要：

- `compileOnly("org.springframework:spring-web:5.3.21")`
- 你自己的 DI 注册，例如 Koin `single { GreetingService() }` 和 `single { UserController(get()) }`

## 目前不做的东西

当前 MVP 主要面向 HTTP JSON API。

不在当前支持范围内的能力包括：

- Spring MVC 视图渲染
- `ResponseEntity`
- WebSocket 注解式生成
- 流式文件下载专用返回类型抽象
- 更完整的 SSE 抽象

其中 WebSocket 目前建议直接写原生 Ktor 路由，与生成路由并存：

```kotlin
import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send

fun Route.registerWsRoutes() {
    webSocket("/ws/echo") {
        for (frame in incoming) {
            if (frame is Frame.Text) {
                send(Frame.Text("echo:${frame.readText()}"))
            }
        }
    }
}
```

## 一句话总结

如果你想要的是“写起来像 Spring Controller，但运行起来是 Ktor”，这个处理器就是干这个的。
