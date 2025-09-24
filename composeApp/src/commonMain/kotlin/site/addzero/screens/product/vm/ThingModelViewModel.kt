package site.addzero.screens.product.vm

import androidx.lifecycle.ViewModel
import org.koin.android.annotation.KoinViewModel
import site.addzero.generated.isomorphic.ThingModelPropertyIso

@KoinViewModel
class ThingModelViewModel : ViewModel() {

    fun loadThingModelProperties(productId: Long): List<ThingModelPropertyIso> {
        // TODO: 从后端加载物模型属性数据
        return listOf(
            ThingModelPropertyIso(
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

    fun addProperty(productId: Long, property: ThingModelPropertyIso) {
        // TODO: 添加物模型属性
    }

    fun updateProperty(property: ThingModelPropertyIso) {
        // TODO: 更新物模型属性
    }

    fun deleteProperty(id: Long) {
        // TODO: 删除物模型属性
    }
}

