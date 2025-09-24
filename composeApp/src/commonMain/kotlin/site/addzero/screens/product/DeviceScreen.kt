package site.addzero.screens.product

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import site.addzero.component.table.biz.AddTable
import site.addzero.component.table.original.entity.ColumnConfig
import site.addzero.generated.forms.DeviceForm
import site.addzero.generated.forms.rememberDeviceFormState
import site.addzero.generated.isomorphic.DeviceIso
import site.addzero.screens.product.vm.DeviceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceScreen(viewModel: DeviceViewModel) {
    val devices = viewModel.loadDevices()
    val deviceFormState = rememberDeviceFormState()

    DeviceForm(
        deviceFormState,
        visible = viewModel.showAddDialog,
        title = "产品分类表单",
        onClose = {},
        onSubmit = {},
    )

    AddTable(

        data = devices,
        columns = listOf(
            ColumnConfig(
                key = "name",
                comment = "设备名称",
                kmpType = "kotlin.String"
            ),
            ColumnConfig(
                key = "code",
                comment = "设备编码",
                kmpType = "kotlin.String"
            ),
            ColumnConfig(
                key = "product",
                comment = "所属产品",
                kmpType = "site.addzero.generated.isomorphic.ProductIso"
            ),
            ColumnConfig(
                key = "authInfo",
                comment = "认证信息",
                kmpType = "kotlin.String"
            ),
            ColumnConfig(
                key = "status",
                comment = "设备状态",
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
        onEditClick = { device ->
            viewModel.selectedDevice = device as DeviceIso
            viewModel.showAddDialog = true
        },
        onDeleteClick = { device ->
            viewModel.deleteDevice((device as DeviceIso).id ?: 0L)
        }
    )
}


