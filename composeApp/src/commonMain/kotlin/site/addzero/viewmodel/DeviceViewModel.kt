package site.addzero.viewmodel

import androidx.lifecycle.ViewModel

class DeviceViewModel : ViewModel() {

    fun loadDevices(): List<Device> {
        // TODO: 从后端加载设备数据
        return listOf(
            Device(
                id = 1,
                name = "温度传感器001",
                code = "TS-001",
                productId = 2,
                productName = "传感器",
                authInfo = "cert123456",
                enabled = true,
                status = "在线"
            )
        )
    }

    fun loadProducts(): List<Product> {
        // TODO: 从后端加载产品数据
        return listOf(
            Product(
                id = 1,
                name = "数控机床",
                code = "CNC-001",
                categoryId = 1,
                categoryName = "机床",
                description = "高精度数控机床",
                accessMethod = "MQTT",
                authMethod = "证书认证",
                enabled = true
            ),
            Product(
                id = 2,
                name = "温湿度传感器",
                code = "THS-001",
                categoryId = 2,
                categoryName = "传感器",
                description = "温湿度传感器",
                accessMethod = "MQTT",
                authMethod = "密钥认证",
                enabled = true
            )
        )
    }

    fun addDevice(device: Device) {
        // TODO: 添加设备
    }

    fun updateDevice(device: Device) {
        // TODO: 更新设备
    }

    fun deleteDevice(id: Long) {
        // TODO: 删除设备
    }
}

data class Device(
    val id: Long,
    val name: String,
    val code: String,
    val productId: Long,
    val productName: String,
    val authInfo: String?,
    val enabled: Boolean,
    val status: String
)
