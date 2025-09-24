package site.addzero.screens.product

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.component.button.AddButton
import site.addzero.component.high_level.AddLazyList
import site.addzero.generated.isomorphic.ThingModelPropertyIso
import site.addzero.screens.product.vm.ThingModelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThingModelScreen(viewModel: ThingModelViewModel, productId: Long) {
    var thingModelProperties by remember { mutableStateOf(listOf<ThingModelPropertyIso>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedProperty by remember { mutableStateOf<ThingModelPropertyIso?>(null) }

    // 刷新数据
    LaunchedEffect(Unit) {
        thingModelProperties = viewModel.loadThingModelProperties(productId)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "物模型管理",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AddButton(
            displayName = "添加属性",
            onClick = { showAddDialog = true },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AddLazyList(thingModelProperties) { property ->
            ThingModelPropertyItem(
                property = property,
                onEdit = { selectedProperty = it },
                onDelete = { viewModel.deleteProperty(it.id?:0L) }
            )
        }
    }

    if (showAddDialog) {
        ThingModelPropertyDialog(
            property = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { property ->
                viewModel.addProperty(productId, property)
                thingModelProperties = viewModel.loadThingModelProperties(productId)
                showAddDialog = false
            }
        )
    }

    selectedProperty?.let { property ->
        ThingModelPropertyDialog(
            property = property,
            onDismiss = { selectedProperty = null },
            onConfirm = { updatedProperty ->
                viewModel.updateProperty(updatedProperty)
                thingModelProperties = viewModel.loadThingModelProperties(productId)
                selectedProperty = null
            }
        )
    }
}

@Composable
private fun ThingModelPropertyItem(
    property: ThingModelPropertyIso,
    onEdit: (ThingModelPropertyIso) -> Unit,
    onDelete: (ThingModelPropertyIso) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
//                Text(text = "${property.name} (${property.identifier})", style = MaterialTheme.typography.titleMedium)
//                Text(text = "数据类型: ${property.dataType}", style = MaterialTheme.typography.bodyMedium)
//                property.dataPrecision?.let {
//                    Text(text = "精度: $it", style = MaterialTheme.typography.bodyMedium)
//                }
//                Text(text = "访问方式: ${property.accessMode}", style = MaterialTheme.typography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { onEdit(property) }) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑")
                }

                IconButton(onClick = { onDelete(property) }) {
                    Icon(Icons.Default.Delete, contentDescription = "删除")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThingModelPropertyDialog(
    property: ThingModelPropertyIso?,
    onDismiss: () -> Unit,
    onConfirm: (ThingModelPropertyIso) -> Unit
) {
    var identifier by remember { mutableStateOf(property?.identifier ?: "") }
    var name by remember { mutableStateOf(property?.name ?: "") }
    var dataType by remember { mutableStateOf(property?.dataType ?: "") }
    var dataPrecision by remember { mutableStateOf(property?.dataPrecision?.toString() ?: "") }
    var accessMode by remember { mutableStateOf(property?.accessMode ?: "读写") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (property == null) "添加属性" else "编辑属性") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
//                modifier = Modifier.verticalScroll()
            ) {
                OutlinedTextField(
                    value = identifier,
                    onValueChange = { identifier = it },
                    label = { Text("属性标识") }
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("属性名称") }
                )
                OutlinedTextField(
                    value = dataType,
                    onValueChange = { dataType = it },
                    label = { Text("数据类型") }
                )
                OutlinedTextField(
                    value = dataPrecision,
                    onValueChange = { dataPrecision = it },
                    label = { Text("精度值") }
                )
                // 访问方式选择
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { /* TODO */ }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = accessMode,
                        onValueChange = { },
                        label = { Text("访问方式") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) }
                    )
                    ExposedDropdownMenu(expanded = false, onDismissRequest = { /* TODO */ }) {
                        listOf("读", "写", "上报", "读写", "读上报", "写上报", "读写上报").forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode) },
                                onClick = {
                                    accessMode = mode
                                    // TODO 关闭菜单
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newProperty = ThingModelPropertyIso(
                        id = property?.id ?: 0,
                        identifier = identifier,
                        name = name,
                        dataType = dataType,
                        dataPrecision = dataPrecision.toIntOrNull(),
                        accessMode = accessMode
                    )
                    onConfirm(newProperty)
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

