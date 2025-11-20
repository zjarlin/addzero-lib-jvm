package site.addzero.lib_adaptor

import cn.hutool.core.io.FileUtil
import site.addzero.util.MpGenerator
import site.addzero.util.PropertyUtil

fun main(args: Array<String>) {
    val outputPath = getGenDir()
    val mpGenerator = MpGenerator(MpGeneratorSettingsImpl())
    val genList = arrayOf(
//        "iot_product_device_access",

        "iot_thingmodel_property_mapping",
//        "iot_industrial_protocol",
//        "iot_product_connection_param_act",
//        "iot_product_connection_param"
//        "iot_device_connection",
//        "iot_function_definition"
//        "iot_energy_type"
//       "iot_device_emission_source"
//        "iot_action_metadata",
//        "iot_alarm_history",
//        "iot_alarm_record",
//        "iot_alarm_rule",
//        "iot_codec_config",
//        "iot_device_action_execution",
//        "iot_device_codec_mapping",
//        "iot_device_event_record",
//        "iot_device_info",
//        "iot_device_message",
//        "iot_device_property",
//        "iot_device_shadow",
//        "iot_device_tag",
////        "iot_energy_type",
//        "iot_event_metadata",
//        "iot_event_parameter",
//        "iot_message_send_result",
//        "iot_notification_config",
//        "iot_notification_message",
//        "iot_notification_template",
//        "iot_product",
//        "iot_property_metadata",
//        "iot_protocol_config"
    )
    mpGenerator.gen(outputPath, "com.zlj.iot", *genList)

}

fun getGenDir(): String {
    val projectRoot = PropertyUtil.getProperty("generator.properties", "project.root")
    val module = PropertyUtil.getProperty("generator.properties", "project.module")
    val file = FileUtil.file(projectRoot, module, "src", "main", "java")
    return file.absolutePath
}
