# Netty Util 工具类

这是一个基于 Netty 的简单易用的网络通信工具类，提供了快速创建 TCP 服务器和客户端的功能。

## 功能特性

- ✅ 简单易用的 API
- ✅ 支持 TCP 服务器和客户端
- ✅ 自动消息编解码（UTF-8 字符串）
- ✅ 连接管理和空闲检测
- ✅ 消息广播功能
- ✅ 优雅的资源管理

## 快速开始

### 1. 添加依赖

在你的 `build.gradle.kts` 文件中添加：

```kotlin
dependencies {
    implementation("site.addzero:netty-util:1.0.0")
}
```

### 2. 创建 TCP 服务器

```kotlin
import site.addzero.netty.NettyUtil

fun main() {
    // 启动服务器
    NettyUtil.startTcpServer("localhost", 8080) { message ->
        println("收到消息: $message")
    }
}
```

### 3. 创建 TCP 客户端

```kotlin
import site.addzero.netty.NettyUtil
import io.netty.channel.Channel

fun main() {
    // 创建客户端连接
    val channel = NettyUtil.createTcpClient("localhost", 8080) { message ->
        println("服务器消息: $message")
    }

    // 发送消息
    NettyUtil.sendMessage(channel, "Hello Server!")
}
```

## API 文档

### 服务器相关方法

#### `startTcpServer(String host, int port, Consumer<String> messageHandler)`

启动一个简单的 TCP 服务器。

**参数：**
- `host`: 绑定主机地址
- `port`: 绑定端口
- `messageHandler`: 消息处理器

**返回：** `ChannelFuture` - 服务器启动结果

#### `startTcpServer(String host, int port, Consumer<String> messageHandler, Consumer<Channel> connectHandler)`

启动 TCP 服务器，支持连接处理器。

**参数：**
- `host`: 绑定主机地址
- `port`: 绑定端口
- `messageHandler`: 消息处理器
- `connectHandler`: 连接处理器（可选）

### 客户端相关方法

#### `createTcpClient(String host, int port, Consumer<String> messageHandler)`

创建一个 TCP 客户端连接。

**参数：**
- `host`: 服务器地址
- `port`: 服务器端口
- `messageHandler`: 消息处理器

**返回：** `Channel` - 客户端通道

### 消息发送方法

#### `sendMessage(Channel channel, String message)`

发送消息到指定通道。

#### `broadcastMessage(Iterable<Channel> channels, String message)`

广播消息到多个通道。

### 工具方法

#### `isChannelActive(Channel channel)`

检查通道是否活跃。

#### `getRemoteAddress(Channel channel)`

获取通道的远程地址。

#### `closeChannel(Channel channel)`

关闭指定通道。

#### `shutdownGracefully(EventLoopGroup group)`

优雅关闭 EventLoopGroup。

## 完整示例

### 聊天服务器示例

参见 `NettyServerExample.kt`：

```kotlin
object NettyServerExample {
    @JvmStatic
    fun main(args: Array<String>) {
        NettyUtil.startTcpServer(
            host = "localhost",
            port = 8080,
            messageHandler = ::handleMessage,
            connectHandler = ::handleClientConnection
        )
    }

    private fun handleMessage(message: String) {
        // 处理接收到的消息
        println("接收到消息: $message")
    }

    private fun handleClientConnection(channel: Channel) {
        // 处理客户端连接
        println("新客户端连接: ${NettyUtil.getRemoteAddress(channel)}")
    }
}
```

### 聊天客户端示例

参见 `NettyClientExample.kt`：

```kotlin
object NettyClientExample {
    @JvmStatic
    fun main(args: Array<String>) {
        val channel = NettyUtil.createTcpClient(
            host = "localhost",
            port = 8080,
            messageHandler = { message -> println("服务器消息: $message") }
        )

        // 发送消息
        NettyUtil.sendMessage(channel, "Hello!")
    }
}
```

## 运行示例

1. **启动服务器：**
   ```bash
   cd lib/tool-jvm/netty-util
   ./gradlew build
   java -cp build/libs/netty-util.jar site.addzero.netty.example.NettyServerExampleKt
   ```

2. **启动客户端：**
   ```bash
   java -cp build/libs/netty-util.jar site.addzero.netty.example.NettyClientExampleKt
   ```

3. **在客户端输入消息进行聊天测试**

## 注意事项

1. **异常处理：** 工具类会自动处理基本的网络异常，但建议在应用层添加适当的错误处理逻辑。

2. **资源管理：** 服务器和客户端会自动管理线程资源，但在应用关闭时建议调用相应的关闭方法。

3. **消息格式：** 默认使用 UTF-8 编码的字符串消息，如需支持其他格式，请自定义编解码器。

4. **线程安全：** 所有公共方法都是线程安全的，可以在多线程环境中使用。

## 依赖信息

- Netty: 4.1.104.Final
- SLF4J API: 2.0.9
- FastJSON2 Kotlin: 2.0.43
- Hutool: 5.8.25

## 许可证

本项目采用 MIT 许可证。