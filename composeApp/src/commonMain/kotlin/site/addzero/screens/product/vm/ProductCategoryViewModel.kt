package site.addzero.screens.product.vm

import androidx.lifecycle.ViewModel
import org.koin.android.annotation.KoinViewModel
import site.addzero.generated.isomorphic.ProductCategoryIso


@KoinViewModel
class ProductCategoryViewModel : ViewModel() {

    fun loadCategories(): List<ProductCategoryIso > {

        return emptyList()
    }

    fun addCategory(category: ProductCategoryIso ) {
        // TODO: 添加产品分类
    }

    fun updateCategory(category: ProductCategoryIso) {
        // TODO: 更新产品分类
    }

    fun deleteCategory(id: Long) {
        // TODO: 删除产品分类
    }
}

