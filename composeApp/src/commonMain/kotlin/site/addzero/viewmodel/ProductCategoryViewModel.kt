package site.addzero.viewmodel

import androidx.lifecycle.ViewModel

class ProductCategoryViewModel : ViewModel() {
    
    fun loadCategories(): List<ProductCategory> {
        // TODO: 从后端加载产品分类数据
        return listOf(
            ProductCategory(1, "机床", "各类机床设备", true),
            ProductCategory(2, "传感器", "各种传感器设备", true)
        )
    }
    
    fun addCategory(category: ProductCategory) {
        // TODO: 添加产品分类
    }
    
    fun updateCategory(category: ProductCategory) {
        // TODO: 更新产品分类
    }
    
    fun deleteCategory(id: Long) {
        // TODO: 删除产品分类
    }
}

data class ProductCategory(
    val id: Long,
    val name: String,
    val description: String?,
    val enabled: Boolean
)