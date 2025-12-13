package site.addzero.apt.dict.example

import site.addzero.apt.dict.annotations.DictTranslate
import site.addzero.apt.dict.annotations.DictField

/**
 * 复杂嵌套结构示例，展示 APT 处理器如何自动检测和处理嵌套对象和 List
 * 
 * 这个示例模拟了类似 producttrace-master 项目中的复杂嵌套场景：
 * - 嵌套对象
 * - List 集合
 * - 多层嵌套
 * - 混合字典类型（系统字典和表字典）
 */

@DictTranslate
data class ComplexNestedEntity(
    // 基本字段的字典翻译
    @DictField(dictCode = "sys_user_sex")
    val userSex: String? = null,
    
    @DictField(dictCode = "device_status") 
    val deviceStatus: String? = null,
    
    @DictField(table = "iot_product", codeColumn = "product_key", nameColumn = "product_name")
    val productKey: String? = null,
    
    // 嵌套对象 - APT 会自动检测并处理
    val deviceInfo: DeviceInfo? = null,
    
    // List 集合 - APT 会自动检测并递归处理每个元素
    val sensors: List<SensorInfo>? = null,
    
    // 复杂嵌套：包含 List 的嵌套对象
    val alarmConfig: AlarmConfig? = null
)

@DictTranslate
data class DeviceInfo(
    @DictField(dictCode = "device_type")
    val deviceType: String? = null,
    
    @DictField(table = "iot_device_info", codeColumn = "device_id", nameColumn = "device_name")
    val deviceId: String? = null,
    
    // 进一步嵌套
    val location: LocationInfo? = null
)

@DictTranslate
data class LocationInfo(
    @DictField(dictCode = "area_code")
    val areaCode: String? = null,
    
    @DictField(dictCode = "sensor_type")
    val sensorType: String? = null
)

@DictTranslate
data class SensorInfo(
    @DictField(dictCode = "sensor_status")
    val status: String? = null,
    
    @DictField(table = "iot_sensor", codeColumn = "sensor_id", nameColumn = "sensor_name")
    val sensorId: String? = null
)

@DictTranslate
data class AlarmConfig(
    @DictField(table = "iot_alarm_record", codeColumn = "alarm_id", nameColumn = "alarm_name")
    val alarmName: String? = null,
    
    @DictField(table = "iot_alarm_record", codeColumn = "alarm_id", nameColumn = "alarm_level")
    val alarmLevel: String? = null,
    
    @DictField(table = "iot_alarm_record", codeColumn = "alarm_id", nameColumn = "alarm_type")
    val alarmType: String? = null,
    
    // 嵌套的 List
    val alarmRules: List<AlarmRule>? = null
)

@DictTranslate
data class AlarmRule(
    @DictField(dictCode = "rule_type")
    val ruleType: String? = null,
    
    @DictField(dictCode = "rule_status")
    val ruleStatus: String? = null
)