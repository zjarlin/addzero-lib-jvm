package site.addzero.util

import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * MQTT工具类，提供简单的MQTT消息发布和订阅功能
 * @param brokerHost MQTT代理主机地址，默认为"broker.hivemq.com"
 * @param brokerPort MQTT代理端口，默认为1883
 */
class MQTTUtils(
    private val brokerHost: String = "broker.emqx.io",
    private val brokerPort: Int = 1883
) {
    companion object {
        /**
         * 创建MQTT生产者客户端
         * @param clientId 客户端ID
         * @param brokerHost MQTT代理主机地址
         * @param brokerPort MQTT代理端口
         * @return Mqtt3BlockingClient
         */
        fun createProducer(
            clientId: String = generateClientId("producer"),
            brokerHost: String = "broker.hivemq.com",
            brokerPort: Int = 1883
        ): Mqtt3BlockingClient = Mqtt3Client.builder()
            .identifier(clientId)
            .serverHost(brokerHost)
            .serverPort(brokerPort)
            .buildBlocking()

        /**
         * 创建MQTT消费者客户端
         * @param clientId 客户端ID
         * @param brokerHost MQTT代理主机地址
         * @param brokerPort MQTT代理端口
         * @return Mqtt3BlockingClient
         */
        fun createConsumer(
            clientId: String = generateClientId("consumer"),
            brokerHost: String = "broker.hivemq.com",
            brokerPort: Int = 1883
        ): Mqtt3BlockingClient = Mqtt3Client.builder()
            .identifier(clientId)
            .serverHost(brokerHost)
            .serverPort(brokerPort)
            .automaticReconnect()
            .initialDelay(1, TimeUnit.SECONDS)
            .maxDelay(30, TimeUnit.SECONDS)
            .applyAutomaticReconnect()
            .buildBlocking()

        /**
         * 发布消息
         * @param client MQTT客户端
         * @param topic 主题
         * @param message 消息内容
         */
        fun publishMessage(client: Mqtt3BlockingClient, topic: String, message: String) {
            client.publishWith()
                .topic(topic)
                .payload(message.toByteArray())
                .send()
        }

        /**
         * 订阅消息
         * @param client MQTT客户端
         * @param topic 主题
         * @param messageHandler 消息处理函数
         */
        fun subscribeToTopic(
            client: Mqtt3BlockingClient,
            topic: String,
            messageHandler: (Mqtt3Publish) -> Unit
        ): CompletableFuture<Mqtt3SubAck?> {
            val send = client.toAsync().subscribeWith()
                .topicFilter(topic)
                .callback(messageHandler)
                .send()
            return send
        }


        /**
         * 订阅消息
         * @param client MQTT客户端
         * @param topic 主题
         * @param messageHandler 消息处理函数
         */
        fun subscribeToTopicAndGet(
            client: Mqtt3BlockingClient,
            topic: String,
            messageHandler: (Mqtt3Publish) -> Unit
        ): Mqtt3SubAck? {
            return subscribeToTopic(client, topic, messageHandler).get()
        }


        /**
         * 生成随机客户端ID
         * @param prefix 前缀
         * @return 客户端ID
         */
        private fun generateClientId(prefix: String): String {
            return "$prefix-${System.currentTimeMillis()}-${(1000..9999).random()}"
        }
    }

    /**
     * 创建MQTT生产者客户端实例
     * @param clientId 客户端ID
     * @return Mqtt3BlockingClient
     */
    fun createProducerInstance(clientId: String = generateClientId("producer")): Mqtt3BlockingClient =
        Mqtt3Client.builder()
            .identifier(clientId)
            .serverHost(brokerHost)
            .serverPort(brokerPort)
            .buildBlocking()

    /**
     * 创建MQTT消费者客户端实例
     * @param clientId 客户端ID
     * @return Mqtt3BlockingClient
     */
    fun createConsumerInstance(clientId: String = generateClientId("consumer")): Mqtt3BlockingClient =
        Mqtt3Client.builder()
            .identifier(clientId)
            .serverHost(brokerHost)
            .serverPort(brokerPort)
            .automaticReconnect()
            .initialDelay(1, TimeUnit.SECONDS)
            .maxDelay(30, TimeUnit.SECONDS)
            .applyAutomaticReconnect()
            .buildBlocking()
}
