package site.addzero.web.modules.controller.product

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import site.addzero.utils.MQTTUtils

@RestController
@RequestMapping("/mqtt")
class MqttController {
    @GetMapping("/producer")
    fun producer(
        brokerHost: String = "broker.emqx.io", brokerPort: Int =
            1883, topic: String, message: String
    ) {
        val mqttUtils = MQTTUtils(
            brokerHost = brokerHost,
            brokerPort = brokerPort,
        )
        val createProducerInstance = mqttUtils.createProducerInstance()
        MQTTUtils.publishMessage(createProducerInstance, topic, message)
    }


    @GetMapping("/consumer")
    fun consumer(
        brokerHost: String = "broker.emqx.io", brokerPort: Int =
            1883, topic: String
    ) {
        val mqttUtils = MQTTUtils(
            brokerHost = brokerHost,
            brokerPort = brokerPort,
        )
        val createProducerInstance = mqttUtils.createConsumerInstance()
        MQTTUtils.subscribeToTopic(createProducerInstance, topic) {
            val msg = String(it.payloadAsBytes)
            println("”收到消息 [主题: ${it.topic}]")
            println("内容$msg")
        }
    }


}
