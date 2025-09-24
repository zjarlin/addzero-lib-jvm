package site.addzero.screens.product

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import site.addzero.component.table.biz.AddTable
import site.addzero.generated.forms.ProductForm
import site.addzero.generated.forms.rememberProductFormState
import site.addzero.screens.product.vm.ProductViewModel
data class ColumnMeta(
    val key: String,
    val label: String,
    val kmpType: String,
    val kmpConfig: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(viewModel: ProductViewModel) {

    val rememberProductFormState = rememberProductFormState()

    ProductForm(
        rememberProductFormState,
        visible = viewModel.showAddDialog,
        title = "产品表单",
        onClose = { viewModel.showAddDialog = false },
        onSubmit = {
            println("提交了表单")
        }
    )

    AddTable(
        data = viewModel.products,
        columns = listOf(
            "name" to "产品名称",
            "code" to "产品编码",
            "productCategory" to "产品分类",
            "devices" to "设备列表",
            "description" to "描述",
            "accessMethod" to "接入方式",
            "enabled" to "是否启用"
        ),
        getColumnKey = { it.first },
        onSearch = { keyword, serchState, stateSort, StatePagination ->
            println("搜索")
        },
        onSaveClick = {},
        onImportClick = {},
        onExportClick = { keyword, serchState, stateSort, StatePagination ->
        },
        onBatchDelete = {},
        onBatchExport = {},
        onEditClick = {},
        onDeleteClick = {}
    )


}


