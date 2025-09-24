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
import site.addzero.generated.isomorphic.DeviceIso
import site.addzero.generated.isomorphic.ProductIso
import site.addzero.screens.product.vm.DeviceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceScreen(viewModel: DeviceViewModel) {
    var devices by remember { mutableStateOf(listOf<DeviceIso>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedDevice by remember { mutableStateOf<DeviceIso?>(null) }

    // 刷新数据
    LaunchedEffect(Unit) {
        devices = viewModel.loadDevices()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "设备管理",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AddButton(
            displayName = "添加设备",
            onClick = { showAddDialog = true },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AddLazyList(devices) { device ->
            DeviceItem(
                device = device,
                onEdit = { selectedDevice = it },
                onDelete = { viewModel.deleteDevice(it.id?:0L) }
            )
        }
    }

    if (showAddDialog) {
        DeviceDialog(
            device = null,
            products = viewModel.loadProducts(),
            onDismiss = { showAddDialog = false },
            onConfirm = { device ->
                viewModel.addDevice(device)
                devices = viewModel.loadDevices()
                showAddDialog = false
            }
        )
    }

    selectedDevice?.let { device ->
        DeviceDialog(
            device = device,
            products = viewModel.loadProducts(),
            onDismiss = { selectedDevice = null },
            onConfirm = { updatedDevice ->
                viewModel.updateDevice(updatedDevice)
                devices = viewModel.loadDevices()
                selectedDevice = null
            }
        )
    }
}

@Composable
private fun DeviceItem(
    device: DeviceIso,
    onEdit: (DeviceIso) -> Unit,
    onDelete: (DeviceIso) -> Unit
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
                Text(text = device.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "编码: ${device.code}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "产品: ${device.product?.name}", style = MaterialTheme
                        .typography.bodyMedium
                )
                Text(
                    text = "状态: ${device.status}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (device.enabled) "启用" else "禁用",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (device.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { onEdit(device) }) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑")
                }

                IconButton(onClick = { onDelete(device) }) {
                    Icon(Icons.Default.Delete, contentDescription = "删除")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceDialog(
    device: DeviceIso?,
    products: List<ProductIso>,
    onDismiss: () -> Unit,
    onConfirm: (DeviceIso) -> Unit
) {
    var name by remember { mutableStateOf(device?.name ?: "") }
    var code by remember { mutableStateOf(device?.code ?: "") }
    var selectedProductId by remember { mutableStateOf(device?.product?.id ?: 0L) }
    var authInfo by remember { mutableStateOf(device?.authInfo ?: "") }
    var enabled by remember { mutableStateOf(device?.enabled ?: true) }
    var status by remember { mutableStateOf(device?.status ?: "离线") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (device == null) "添加设备" else "编辑设备") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
//                modifier = Modifier.verticalScroll()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("设备名称") }
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("设备编码") }
                )
                // 产品选择
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { /* TODO */ }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = products.find { it.id == selectedProductId }?.name ?: "请选择产品",
                        onValueChange = { },
                        label = { Text("所属产品") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) }
                    )
                    ExposedDropdownMenu(expanded = false, onDismissRequest = { /* TODO */ }) {
                        products.forEach { product ->
                            DropdownMenuItem(
                                text = { Text(product.name) },
                                onClick = {
                                    selectedProductId = product.id.toString().toLong()
                                    // TODO 关闭菜单
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = authInfo,
                    onValueChange = { authInfo = it },
                    label = { Text("认证信息") }
                )
                OutlinedTextField(
                    value = status,
                    onValueChange = { status = it },
                    label = { Text("设备状态") }
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = enabled,
                        onCheckedChange = { enabled = it }
                    )
                    Text("启用设备")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newDevice = DeviceIso(
                        id = device?.id ?: 0,
                        name = name,
                        code = code,
                        product = ProductIso(
                            id = selectedProductId,
                            name = products.find { it.id == selectedProductId }?.name ?: "",
                        ),
                        authInfo = authInfo,
                        enabled = enabled,
                        status = status
                    )
                    onConfirm(newDevice)
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

