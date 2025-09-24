package site.addzero.screens.product

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.component.button.AddButton
import site.addzero.component.high_level.AddLazyList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(viewModel: ProductViewModel) {
    var products by remember { mutableStateOf(listOf<Product>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    // 刷新数据
    LaunchedEffect(Unit) {
        products = viewModel.loadProducts()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "产品管理",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AddButton(
            displayName = "添加产品",
            onClick = { showAddDialog = true },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AddLazyList(products) { product ->
            ProductItem(
                product = product,
                onEdit = { selectedProduct = it },
                onDelete = { viewModel.deleteProduct(it.id) },
                onManageThingModel = { viewModel.navigateToThingModel(it.id) }
            )
        }
    }

    if (showAddDialog) {
        ProductDialog(
            product = null,
            categories = viewModel.loadCategories(),
            onDismiss = { showAddDialog = false },
            onConfirm = { product ->
                viewModel.addProduct(product)
                products = viewModel.loadProducts()
                showAddDialog = false
            }
        )
    }

    selectedProduct?.let { product ->
        ProductDialog(
            product = product,
            categories = viewModel.loadCategories(),
            onDismiss = { selectedProduct = null },
            onConfirm = { updatedProduct ->
                viewModel.updateProduct(updatedProduct)
                products = viewModel.loadProducts()
                selectedProduct = null
            }
        )
    }
}

@Composable
private fun ProductItem(
    product: Product,
    onEdit: (Product) -> Unit,
    onDelete: (Product) -> Unit,
    onManageThingModel: (Product) -> Unit
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
                Text(text = product.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "编码: ${product.code}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "分类: ${product.categoryName}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "接入方式: ${product.accessMethod}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (product.enabled) "启用" else "禁用",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (product.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onManageThingModel(product) }) {
                    Text("物模型")
                }
                IconButton(onClick = { onEdit(product) }) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑")
                }

                IconButton(onClick = { onDelete(product) }) {
                    Icon(Icons.Default.Delete, contentDescription = "删除")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductDialog(
    product: Product?,
    categories: List<ProductCategory>,
    onDismiss: () -> Unit,
    onConfirm: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var code by remember { mutableStateOf(product?.code ?: "") }
    var selectedCategoryId by remember { mutableStateOf(product?.categoryId ?: 0L) }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var accessMethod by remember { mutableStateOf(product?.accessMethod ?: "MQTT") }
    var authMethod by remember { mutableStateOf(product?.authMethod ?: "") }
    var enabled by remember { mutableStateOf(product?.enabled ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "添加产品" else "编辑产品") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("产品名称") }
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("产品编码") }
                )
                // 产品分类选择
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { /* TODO */ }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = categories.find { it.id == selectedCategoryId }?.name ?: "请选择分类",
                        onValueChange = { },
                        label = { Text("产品分类") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) }
                    )
                    ExposedDropdownMenu(expanded = false, onDismissRequest = { /* TODO */ }) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategoryId = category.id
                                    // TODO 关闭菜单
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("产品描述") }
                )
                OutlinedTextField(
                    value = accessMethod,
                    onValueChange = { accessMethod = it },
                    label = { Text("接入方式") }
                )
                OutlinedTextField(
                    value = authMethod,
                    onValueChange = { authMethod = it },
                    label = { Text("认证方式") }
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = enabled,
                        onCheckedChange = { enabled = it }
                    )
                    Text("启用产品")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newProduct = Product(
                        id = product?.id ?: 0,
                        name = name,
                        code = code,
                        categoryId = selectedCategoryId,
                        categoryName = categories.find { it.id == selectedCategoryId }?.name ?: "",
                        description = description,
                        accessMethod = accessMethod,
                        authMethod = authMethod,
                        enabled = enabled
                    )
                    onConfirm(newProduct)
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