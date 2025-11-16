package site.addzero.netty.example

import io.netty.channel.Channel
import site.addzero.netty.NettyUtil
import java.util.*

/**
 * Netty 客户端示例
 * 演示如何使用 NettyUtil 创建一个简单的聊天客户端
 *
 * @author AddZero
 * @version 1.0
 * @since 2024-01-01
 */
object NettyClientExample {

    private const val SERVER_HOST = "localhost"
    private const val SERVER_PORT = 8080
    private var clientChannel: Channel? = null

    @JvmStatic
    fun main(args: Array<String>) {
        println("=== Netty 聊天客户端示例 ===")

        try {
            // 创建客户端连接
            clientChannel = NettyUtil.createTcpClient(
                host = SERVER_HOST,
                port = SERVER_PORT,
                messageHandler = ::handleServerMessage
            )

            // 启动用户输入监听线程
            startUserInputListener()

        } catch (e: Exception) {
            System.err.println("客户端连接失败: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 处理服务器消息
     */
    private fun handleServerMessage(message: String) {
        println("服务器消息: $message")
    }

    /**
     * 启动用户输入监听
     */
    private fun startUserInputListener() {
        val scanner = Scanner(System.`in`)
        println("连接成功！您可以开始聊天了。")
        println("可用命令:")
        println("  直接输入消息 - 发送聊天消息")
        println("  broadcast:消息 - 广播消息")
        println("  list - 查看在线用户")
        println("  quit - 退出连接")
        println("----------------------------------------")

        while (true) {
            try {
                print("请输入消息: ")
                val input = scanner.nextLine().trim()

                if (input.isEmpty()) {
                    continue
                }

                if (input.equals("quit", ignoreCase = true)) {
                    // 退出连接
                    clientChannel?.let { NettyUtil.sendMessage(it, "quit") }
                    break
                }

                // 发送消息到服务器
                if (NettyUtil.isChannelActive(clientChannel)) {
                    clientChannel?.let { NettyUtil.sendMessage(it, input) }
                } else {
                    System.err.println("连接已断开，无法发送消息")
                    break
                }

            } catch (e: Exception) {
                System.err.println("发送消息时发生错误: ${e.message}")
                break
            }
        }

        // 关闭连接
        NettyUtil.closeChannel(clientChannel)
        scanner.close()
        println("客户端已关闭")
    }
}