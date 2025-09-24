package site.addzero.screens.product

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import site.addzero.component.table.biz.AddTable
import site.addzero.component.table.original.entity.ColumnConfig
import site.addzero.generated.forms.ProductForm
import site.addzero.generated.forms.rememberProductFormState
import site.addzero.generated.isomorphic.ProductIso
import site.addzero.screens.product.vm.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(viewModel: ProductViewModel) {
    val rememberProductFormState = rememberProductFormState()

    ProductForm(
        rememberProductFormState,
        visible = viewModel.showAddDialog,
        title = "产品表单",
        onClose = {},
        onSubmit = {},
    )

    AddTable(
        data = viewModel.products,
        columns = listOf(
            ColumnConfig(
                key = "name",
                comment = "产品名称",
                kmpType = "kotlin.String"
            ),
            ColumnConfig(
                key = "code",
                comment = "产品编码",
                kmpType = "kotlin.String"
            ),
            ColumnConfig(
                key = "productCategory",
                comment = "产品分类",
                kmpType = "kotlin.String"
            ),
            ColumnConfig(
                key = "devices",
                comment = "设备列表",
                kmpType = "kotlin.collections.List"
            ),
            ColumnConfig(
                key = "description",
                comment = "描述",
                kmpType = "kotlin.String"
            ),
            ColumnConfig(
                key = "accessMethod",
                comment = "接入方式",
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
        onEditClick = { product ->
            viewModel.selectedProduct = product as ProductIso
            viewModel.showAddDialog = true
        },
        onDeleteClick = { product ->
            viewModel.deleteProduct((product as ProductIso).id ?: 0L)
        }
    )
}
