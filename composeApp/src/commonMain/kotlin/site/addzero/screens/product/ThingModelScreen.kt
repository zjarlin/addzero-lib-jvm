package site.addzero.screens.product

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import site.addzero.component.table.biz.AddTable
import site.addzero.component.table.original.entity.ColumnConfig
import site.addzero.generated.forms.ThingModelForm
import site.addzero.generated.forms.rememberThingModelFormState
import site.addzero.generated.isomorphic.ThingModelPropertyIso
import site.addzero.screens.product.vm.ThingModelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThingModelScreen(viewModel: ThingModelViewModel, productId: Long) {
    val thingModelProperties = viewModel.loadThingModelProperties(productId)
    val thingModelFormState = rememberThingModelFormState()

    ThingModelForm(
        thingModelFormState,
        visible = viewModel.showAddDialog,
        title = "物模型属性表单",
        onClose = {viewModel.showAddDialog=false},
        onSubmit = {},
    )

    AddTable(
        data = thingModelProperties,
        columns = listOf(
            ColumnConfig(
                key = "identifier",
                comment = "属性标识",
                kmpType = "kotlin.String"
            ),
            ColumnConfig(
                key = "name",
                comment = "属性名称",
                kmpType = "kotlin.String"
            ),
            ColumnConfig(
                key = "dataType",
                comment = "数据类型",
                kmpType = "kotlin.String"
            ),
            ColumnConfig(
                key = "dataPrecision",
                comment = "精度值",
                kmpType = "kotlin.Int"
            ),
            ColumnConfig(
                key = "accessMode",
                comment = "访问方式",
                kmpType = "kotlin.String"
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
        onEditClick = { property ->
            viewModel.selectedProperty = property as ThingModelPropertyIso
            viewModel.showAddDialog = true
        },
        onDeleteClick = { property ->
            viewModel.deleteProperty((property as ThingModelPropertyIso).id ?: 0L)
        }
    )
}
