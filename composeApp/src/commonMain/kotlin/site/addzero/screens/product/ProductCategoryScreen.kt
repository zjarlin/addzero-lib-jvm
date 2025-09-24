package site.addzero.screens.product

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import site.addzero.component.table.biz.AddTable
import site.addzero.component.table.original.entity.ColumnConfig
import site.addzero.generated.forms.ProductCategoryForm
import site.addzero.generated.forms.rememberProductCategoryFormState
import site.addzero.generated.isomorphic.ProductCategoryIso
import site.addzero.screens.product.vm.ProductCategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCategoryScreen(viewModel: ProductCategoryViewModel) {
    val categories = viewModel.loadCategories()
    val productCategoryFormState = rememberProductCategoryFormState()

    ProductCategoryForm(
        productCategoryFormState,
        visible = viewModel.showAddDialog,
        title = "产品分类表单",
        onClose = {},
        onSubmit = {},
    )

    AddTable(
        data = categories,
        columns = listOf(
            ColumnConfig(
                key = "name",
                comment = "分类名称",
                kmpType = "kotlin.String"
            ),
            ColumnConfig(
                key = "description",
                comment = "分类描述",
                kmpType = "kotlin.String"
            ),
            ColumnConfig(
                key = "enabled",
                comment = "是否启用",
                kmpType = "kotlin.Boolean"
            )
        ),
        getColumnKey = { it.key },
        getColumnLabel = {

             Text(
             it.comment
            )


         },
        onSearch = { keyword, serchState, stateSort, StatePagination ->
            println("搜索")
        },
        onSaveClick = {},
        onImportClick = {},
        onExportClick = { keyword, serchState, stateSort, StatePagination ->
        },
        onBatchDelete = {},
        onBatchExport = {},
        onEditClick = { category ->
            viewModel.selectedCategory = category as ProductCategoryIso
            viewModel.showAddDialog = true
        },
        onDeleteClick = { category ->
            viewModel.deleteCategory((category as ProductCategoryIso).id ?: 0L)
        }
    )
}
