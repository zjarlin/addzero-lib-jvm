package site.addzero.netty

import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import io.netty.handler.timeout.IdleStateHandler
import io.netty.util.CharsetUtil
import io.netty.util.concurrent.DefaultThreadFactory
import java.util.concurrent.TimeUnit

/**
 * Netty 工具类，提供简单易用的 Netty 服务器和客户端操作方法
 *
 * @author AddZero
 * @version 1.0
 * @since 2024-01-01
 */
object NettyUtil {

    /**
     * 启动一个简单的 TCP 服务器
     *
     * @param host 绑定主机地址
     * @param port 绑定端口
     * @param messageHandler 消息处理器
     * @return ChannelFuture 服务器启动结果
     */
    fun startTcpServer(
        host: String,
        port: Int,
        messageHandler: (String) -> Unit
    ): ChannelFuture = startTcpServer(host, port, messageHandler, null)

    /**
     * 启动一个简单的 TCP 服务器
     *
     * @param host 绑定主机地址
     * @param port 绑定端口
     * @param messageHandler 消息处理器
     * @param connectHandler 连接处理器（可选）
     * @return ChannelFuture 服务器启动结果
     */
    fun startTcpServer(
        host: String,
        port: Int,
        messageHandler: (String) -> Unit,
        connectHandler: ((Channel) -> Unit)?
    ): ChannelFuture {
        val bossGroup = NioEventLoopGroup(1, DefaultThreadFactory("netty-boss"))
        val workerGroup = NioEventLoopGroup(0, DefaultThreadFactory("netty-worker"))

        return try {
            val bootstrap = ServerBootstrap()
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        val pipeline = ch.pipeline()

                        // 添加空闲检测
                        pipeline.addLast(IdleStateHandler(60, 30, 90, TimeUnit.SECONDS))

                        // 添加编解码器
                        pipeline.addLast(StringDecoder(CharsetUtil.UTF_8))
                        pipeline.addLast(StringEncoder(CharsetUtil.UTF_8))

                        // 添加业务处理器
                        pipeline.addLast(object : SimpleChannelInboundHandler<String>() {
                            override fun channelRead0(ctx: ChannelHandlerContext, msg: String) {
                                try {
                                    messageHandler(msg)
                                } catch (e: Exception) {
                                    System.err.println("处理消息时发生错误: ${e.message}")
                                    e.printStackTrace()
                                }
                            }

                            override fun channelActive(ctx: ChannelHandlerContext) {
                                connectHandler?.invoke(ctx.channel())
                            }

                            override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
                                System.err.println("连接异常: ${cause.message}")
                                ctx.close()
                            }
                        })
                    }
                })

            val future = bootstrap.bind(host, port).sync()
            println("TCP 服务器启动成功，监听地址: $host:$port")

            future
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw RuntimeException("服务器启动被中断", e)
        } catch (e: Exception) {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
            throw RuntimeException("服务器启动失败", e)
        }
    }

    /**
     * 创建一个 TCP 客户端
     *
     * @param host 服务器地址
     * @param port 服务器端口
     * @param messageHandler 消息处理器
     * @return Channel 客户端通道
     */
    fun createTcpClient(
        host: String,
        port: Int,
        messageHandler: (String) -> Unit
    ): Channel {
        val group = NioEventLoopGroup(DefaultThreadFactory("netty-client"))

        return try {
            val bootstrap = Bootstrap()
            bootstrap.group(group)
                .channel(NioSocketChannel::class.java)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        val pipeline = ch.pipeline()

                        // 添加空闲检测
                        pipeline.addLast(IdleStateHandler(0, 30, 0, TimeUnit.SECONDS))

                        // 添加编解码器
                        pipeline.addLast(StringDecoder(CharsetUtil.UTF_8))
                        pipeline.addLast(StringEncoder(CharsetUtil.UTF_8))

                        // 添加业务处理器
                        pipeline.addLast(object : SimpleChannelInboundHandler<String>() {
                            override fun channelRead0(ctx: ChannelHandlerContext, msg: String) {
                                try {
                                    messageHandler(msg)
                                } catch (e: Exception) {
                                    System.err.println("处理消息时发生错误: ${e.message}")
                                    e.printStackTrace()
                                }
                            }

                            override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
                                System.err.println("客户端连接异常: ${cause.message}")
                                ctx.close()
                            }
                        })
                    }
                })

            val future = bootstrap.connect(host, port).sync()
            println("TCP 客户端连接成功，服务器地址: $host:$port")

            future.channel()
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            group.shutdownGracefully()
            throw RuntimeException("客户端连接被中断", e)
        } catch (e: Exception) {
            group.shutdownGracefully()
            throw RuntimeException("客户端连接失败", e)
        }
    }

    /**
     * 发送消息到指定通道
     *
     * @param channel 目标通道
     * @param message 要发送的消息
     */
    fun sendMessage(channel: Channel, message: String) {
        if (channel?.isActive == true) {
            channel.writeAndFlush(message)
        } else {
            throw IllegalStateException("通道未激活或已关闭")
        }
    }

    /**
     * 广播消息到多个通道
     *
     * @param channels 目标通道集合
     * @param message 要广播的消息
     */
    fun broadcastMessage(channels: Iterable<Channel>, message: String) {
        channels?.forEach { channel ->
            if (channel?.isActive == true) {
                channel.writeAndFlush(message)
            }
        }
    }

    /**
     * 关闭通道
     *
     * @param channel 要关闭的通道
     */
    fun closeChannel(channel: Channel?) {
        if (channel?.isActive == true) {
            channel.close()
        }
    }

    /**
     * 优雅关闭 EventLoopGroup
     *
     * @param group 要关闭的 EventLoopGroup
     */
    fun shutdownGracefully(group: EventLoopGroup?) {
        if (group != null && !group.isShutdown) {
            group.shutdownGracefully()
        }
    }

    /**
     * 获取通道的远程地址
     *
     * @param channel 目标通道
     * @return 远程地址字符串，如果通道未连接则返回 null
     */
    fun getRemoteAddress(channel: Channel?): String? {
        return if (channel?.isActive == true) {
            channel.remoteAddress().toString()
        } else {
            null
        }
    }

    /**
     * 检查通道是否活跃
     *
     * @param channel 目标通道
     * @return true 如果通道活跃，否则返回 false
     */
    fun isChannelActive(channel: Channel?): Boolean {
        return channel?.isActive == true
    }
}