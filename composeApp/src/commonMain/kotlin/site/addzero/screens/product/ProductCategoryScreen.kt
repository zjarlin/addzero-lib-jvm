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
fun ProductCategoryScreen(viewModel: ProductCategoryViewModel) {
    var categories by remember { mutableStateOf(listOf<ProductCategory>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<ProductCategory?>(null) }

    // 刷新数据
    LaunchedEffect(Unit) {
        categories = viewModel.loadCategories()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "产品分类管理",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AddButton(
            displayName = "添加分类",
            onClick = { showAddDialog = true },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AddLazyList(categories) { category ->
            ProductCategoryItem(
                category = category,
                onEdit = { selectedCategory = it },
                onDelete = { viewModel.deleteCategory(it.id) }
            )
        }
    }

    if (showAddDialog) {
        ProductCategoryDialog(
            category = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { category ->
                viewModel.addCategory(category)
                categories = viewModel.loadCategories()
                showAddDialog = false
            }
        )
    }

    selectedCategory?.let { category ->
        ProductCategoryDialog(
            category = category,
            onDismiss = { selectedCategory = null },
            onConfirm = { updatedCategory ->
                viewModel.updateCategory(updatedCategory)
                categories = viewModel.loadCategories()
                selectedCategory = null
            }
        )
    }
}

@Composable
private fun ProductCategoryItem(
    category: ProductCategory,
    onEdit: (ProductCategory) -> Unit,
    onDelete: (ProductCategory) -> Unit
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
                Text(text = category.name, style = MaterialTheme.typography.titleMedium)
                category.description?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    text = if (category.enabled) "启用" else "禁用",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (category.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { onEdit(category) }) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑")
                }

                IconButton(onClick = { onDelete(category) }) {
                    Icon(Icons.Default.Delete, contentDescription = "删除")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductCategoryDialog(
    category: ProductCategory?,
    onDismiss: () -> Unit,
    onConfirm: (ProductCategory) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var description by remember { mutableStateOf(category?.description ?: "") }
    var enabled by remember { mutableStateOf(category?.enabled ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "添加分类" else "编辑分类") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("分类名称") }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("分类描述") }
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = enabled,
                        onCheckedChange = { enabled = it }
                    )
                    Text("启用分类")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newCategory = ProductCategory(
                        id = category?.id ?: 0,
                        name = name,
                        description = description,
                        enabled = enabled
                    )
                    onConfirm(newCategory)
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

data class ProductCategory(
    val id: Long,
    val name: String,
    val description: String?,
    val enabled: Boolean
)