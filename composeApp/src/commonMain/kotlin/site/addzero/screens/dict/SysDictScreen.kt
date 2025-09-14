package site.addzero.screens.dict

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import site.addzero.annotation.Route
import site.addzero.component.button.AddIconButton
import site.addzero.component.high_level.AddDoubleCardLayout
import site.addzero.component.high_level.AddLazyList
import site.addzero.component.search_bar.AddSearchBar
import site.addzero.compose.icons.IconKeys
import site.addzero.generated.forms.SysDictForm
import site.addzero.generated.forms.SysDictItemForm
import site.addzero.generated.forms.rememberSysDictFormState
import site.addzero.generated.forms.rememberSysDictItemFormState
import site.addzero.viewmodel.SysDictViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Route(value = "系统管理", title = "字典管理", routePath = "/dict", icon = IconKeys.CATEGORY, order = 3.0)
fun DictManagerScreen() {
    val vm = koinViewModel<SysDictViewModel>()

    // 使用双卡片布局
    AddDoubleCardLayout(leftContent = {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            AddIconButton(
                text = "添加字典",
            ) {
                vm.showDictForm = true
                vm.selectedDict = null
            }
            // 在分割线下方添加间距
//                HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
            // 搜索框
            AddSearchBar(
                keyword = vm.keyword,
                onKeyWordChanged = { vm.keyword = it },
                onSearch = { vm.onSearch() },
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                placeholder = ""
            )

            RenderDictList(vm)
        }
    }, rightContent = {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp)
        ) {
            // 标题栏：显示当前字典类型信息和操作按钮
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (vm.selectedDict == null) {
                            Text(
                                text = "未选择字典",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "请从左侧选择一个字典类型",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        } else {
                            Text(
                                text = vm.selectedDict!!.dictName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "编码: ${vm.selectedDict!!.dictCode}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            if (vm.selectedDict!!.description != null) {
                                Text(
                                    text = vm.selectedDict!!.description ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    AddIconButton(
                        text = "添加字典项",
                    ) { vm.onAddDictItem() }
                }
            }

            // 字典项列表
            RenderItemList(vm)
        }
    })

    // 字典类型表单
    val rememberSysDictFormState = rememberSysDictFormState(vm.selectedDict)



    SysDictForm(
        state = rememberSysDictFormState,
        visible = vm.showDictForm,
        title = if (vm.selectedDict == null) "新增字典类型" else "编辑字典类型",
        onClose = { vm.showDictForm = false },
        onSubmit = {
            vm.onSaveDict(rememberSysDictFormState.value)
            vm.showDictForm = false
        }
    ) {
        sysDictItems(true)
    }
    val rememberSysDictItemFormState = rememberSysDictItemFormState(vm.selectedDictItem)


    // 字典项表单
    if (vm.selectedDict != null) {
        rememberSysDictItemFormState.value = rememberSysDictItemFormState.value.copy(sysDict = vm.selectedDict)
        SysDictItemForm(

            state = rememberSysDictItemFormState,
            visible = vm.showItemForm,
            title = if (vm.selectedDictItem == null) "新增字典项" else "编辑字典项",
            onClose = { vm.showItemForm = false },
            onSubmit = {
                vm.onSaveDictItem(rememberSysDictItemFormState.value)
            }
        ) {
            sysDict(true)
        }


    }
}

@Composable
private fun RenderDictList(vm: SysDictViewModel) {
    AddLazyList(
        modifier = Modifier.fillMaxSize(),
        items = vm.dicts,
        key = { it.id!! },
    ) {
        DictCard(dictType = it, isSelected = vm.selectedDict?.id == it.id, onClick = {
            vm.selectedDict = it
        }, onEditClick = {
            vm.selectedDict = it
            vm.showDictForm = true
        }, onDeleteClick = {
            vm.deleteDict(it.id!!)
        })
    }
}

@Composable
private fun RenderItemList(vm: SysDictViewModel) {
    when (vm.selectedDict) {
        null -> {
            Box(
                modifier = Modifier.fillMaxSize().padding(vertical = 32.dp), contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Text(
                        "请选择左侧的字典分类",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        else -> {
//            val dictItems = vm.selectedDict?.sysDictItems ?: emptyList()

            when {
                vm.dictItems.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(vertical = 32.dp), contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                            Text(
                                "当前字典下暂无字典项",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                else -> {
                    AddLazyList(
                        modifier = Modifier.fillMaxSize(),
                        items = vm.dictItems,
                        key = { it.id!! },
                    ) {
                        DictItemCard(
                            dictItem = it,
                            onEditClick = { vm.onEditDictItem(it) },
                            onDeleteClick = { vm.onDeleteDictItem(it.id!!) },
                        )
                    }
                }
            }
        }
    }
}


