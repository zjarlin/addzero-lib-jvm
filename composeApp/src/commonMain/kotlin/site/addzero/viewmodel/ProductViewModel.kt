package site.addzero.viewmodel

import androidx.lifecycle.ViewModel

class ProductViewModel : ViewModel() {
    
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
            )
        )
    }
    
    fun loadCategories(): List<ProductCategory> {
        // TODO: 从后端加载产品分类数据
        return listOf(
            ProductCategory(1, "机床", "各类机床设备", true),
            ProductCategory(2, "传感器", "各种传感器设备", true)
        )
    }
    
    fun addProduct(product: Product) {
        // TODO: 添加产品
    }
    
    fun updateProduct(product: Product) {
        // TODO: 更新产品
    }
    
    fun deleteProduct(id: Long) {
        // TODO: 删除产品
    }
    
    fun navigateToThingModel(productId: Long) {
        // TODO: 跳转到物模型管理页面
    }
}

data class Product(
    val id: Long,
    val name: String,
    val code: String,
    val categoryId: Long,
    val categoryName: String,
    val description: String?,
    val accessMethod: String,
    val authMethod: String,
    val enabled: Boolean
)