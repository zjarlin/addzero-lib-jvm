package site.addzero.netty.example

import io.netty.channel.Channel
import site.addzero.netty.NettyUtil
import java.util.concurrent.ConcurrentHashMap

/**
 * Netty 服务器示例
 * 演示如何使用 NettyUtil 创建一个简单的聊天服务器
 *
 * @author AddZero
 * @version 1.0
 * @since 2024-01-01
 */
object NettyServerExample {

    private val connectedClients = ConcurrentHashMap<String, Channel>()
    private const val SERVER_PORT = 8080

    @JvmStatic
    fun main(args: Array<String>) {
        println("=== Netty 聊天服务器示例 ===")

        try {
            // 启动服务器
            NettyUtil.startTcpServer(
                host = "localhost",
                port = SERVER_PORT,
                messageHandler = ::handleMessage,
                connectHandler = ::handleClientConnection
            )
        } catch (e: Exception) {
            System.err.println("服务器启动失败: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 处理接收到的消息
     */
    private fun handleMessage(message: String) {
        println("接收到消息: $message")

        // 简单的消息处理逻辑
        when {
            message.startsWith("broadcast:") -> {
                // 广播消息
                val broadcastMsg = message.substring(10) // 去掉 "broadcast:" 前缀
                broadcastToAllClients("[广播] $broadcastMsg")
            }
            message.startsWith("list") -> {
                // 列出在线客户端
                val clientList = "在线客户端列表: ${connectedClients.keys.joinToString(", ")}"
                broadcastToAllClients(clientList)
            }
            message.startsWith("quit") -> {
                // 客户端退出
                println("客户端请求退出")
            }
            else -> {
                // 普通消息，广播给所有客户端
                broadcastToAllClients("[消息] $message")
            }
        }
    }

    /**
     * 处理客户端连接
     */
    private fun handleClientConnection(channel: Channel) {
        val clientAddress = NettyUtil.getRemoteAddress(channel)
        println("新客户端连接: $clientAddress")

        // 将客户端添加到连接池
        if (clientAddress != null) {
            connectedClients[clientAddress] = channel

            // 发送欢迎消息
            val welcomeMsg = "欢迎连接到聊天服务器！当前在线人数: ${connectedClients.size}"
            NettyUtil.sendMessage(channel, welcomeMsg)

            // 通知其他客户端有新用户加入
            val joinMsg = "用户 $clientAddress 加入了聊天室"
            broadcastToClientExcept(clientAddress, joinMsg)
        }

        // 添加连接关闭监听器
        channel.closeFuture().addListener {
            println("客户端断开连接: $clientAddress")
            connectedClients.remove(clientAddress)

            // 通知其他客户端用户离开
            val leaveMsg = "用户 $clientAddress 离开了聊天室"
            broadcastToAllClients(leaveMsg)
        }
    }

    /**
     * 向所有客户端广播消息
     */
    private fun broadcastToAllClients(message: String) {
        println("广播消息: $message")
        NettyUtil.broadcastMessage(connectedClients.values, message)
    }

    /**
     * 向除指定客户端外的所有客户端广播消息
     */
    private fun broadcastToClientExcept(excludeClient: String, message: String) {
        connectedClients.filterKeys { it != excludeClient }
            .forEach { (_, channel) ->
                NettyUtil.sendMessage(channel, message)
            }
    }
}