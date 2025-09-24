package site.addzero.viewmodel

import androidx.lifecycle.ViewModel

class ThingModelViewModel : ViewModel() {
    
    fun loadThingModelProperties(productId: Long): List<ThingModelProperty> {
        // TODO: 从后端加载物模型属性数据
        return listOf(
            ThingModelProperty(
                id = 1,
                identifier = "temperature",
                name = "温度",
                dataType = "float",
                dataSpecs = "[-20, 100]",
                dataPrecision = 2,
                accessMode = "读写上报"
            )
        )
    }
    
    fun addProperty(productId: Long, property: ThingModelProperty) {
        // TODO: 添加物模型属性
    }
    
    fun updateProperty(property: ThingModelProperty) {
        // TODO: 更新物模型属性
    }
    
    fun deleteProperty(id: Long) {
        // TODO: 删除物模型属性
    }
}

data class ThingModelProperty(
    val id: Long,
    val identifier: String,
    val name: String,
    val dataType: String,
    val dataSpecs: String?,
    val dataPrecision: Int?,
    val accessMode: String
)