import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class HiveMQAsyncClient {

    suspend fun connectAndPublish() = withContext(Dispatchers.IO) {
        val client: Mqtt3AsyncClient = Mqtt3Client.builder()
            .identifier("kotlin-async-client-${System.currentTimeMillis()}")
            .serverHost("broker.hivemq.com")
            .serverPort(1883)
            .buildAsync()

        try {
            // 异步连接
            val connAck = client.connect().await()
            println("异步连接成功: $connAck")

            // 订阅主题
            client.subscribeWith()
                .topicFilter("test/kotlin/async/topic")
                .callback { publish ->
                    val message = String(publish.payloadAsBytes)
                    println("异步收到消息: $message")
                }
                .send()
                .await()
            println("订阅成功")

            // 发布多条消息
            repeat(10) { index ->
                val message = "Async message from Kotlin - $index"
                client.publishWith()
                    .topic("test/kotlin/async/topic")
                    .payload(message.toByteArray())
                    .send()
                    .whenComplete { _, throwable ->
                        if (throwable != null) {
                            println("发布失败: ${throwable.message}")
                        } else {
                            println("异步发送成功: $message")
                        }
                    }

                delay(1000) // 每秒发送一条
            }

            delay(5000) // 等待接收消息

        } catch (e: Exception) {
            println("异步操作失败: ${e.message}")
        } finally {
            client.disconnect()
            println("异步客户端已断开")
        }
    }
}

fun main() = runBlocking {
    val asyncClient = HiveMQAsyncClient()
    asyncClient.connectAndPublish()
}
