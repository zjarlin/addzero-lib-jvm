package site.addzero.screens.product.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.koin.android.annotation.KoinViewModel
import site.addzero.generated.isomorphic.ProductCategoryIso
import site.addzero.generated.isomorphic.ProductIso


@KoinViewModel
class ProductViewModel : ViewModel() {
    var products by mutableStateOf(listOf<ProductIso>())
    var showAddDialog by mutableStateOf(false)
    var selectedProduct by mutableStateOf<ProductIso?>(null)


    fun loadProducts(): List<ProductIso> {
        return listOf(
            ProductIso(
                id = 1,
                name = "数控机床",
                code = "CNC-001",
                productCategory = ProductCategoryIso(id = 1, name = "机床", enabled = true),
                description = "高精度数控机床",
                accessMethod = "MQTT",
                authMethod = "证书认证",
                enabled = true
            )
        )
    }

    fun loadCategories(): List<ProductCategoryIso> {
        // TODO: 从后端加载产品分类数据
        return listOf(
        )
    }

    fun addProduct(product: ProductIso) {
        // TODO: 添加产品
    }

    fun updateProduct(product: ProductIso) {
        // TODO: 更新产品
    }

    fun deleteProduct(id: Long) {
        // TODO: 删除产品
    }

    fun navigateToThingModel(productId: Long) {
        // TODO: 跳转到物模型管理页面
    }

    init {
        loadProducts()
    }
}

