package com.addzero.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.addzero.annotation.Route
import com.addzero.component.dropdown.BaseSelect
import com.addzero.component.dropdown.SelectItemModel

/**
 * BaseSelect组件演示页面
 * 展示BaseSelect组件的各种使用方式和功能
 */
@Composable
@Route("组件示例", "选择框组件", routePath = "/component/baseSelect")
fun BaseSelectDemo() {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 标题
        Text(
            text = "BaseSelect 选择框组件",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // 描述
        Text(
            text = "展示BaseSelect组件的各种使用方式和功能特性",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        HorizontalDivider()

        // 基础用法
        SectionTitle("基础用法")
        
        var selectedBasic by remember { mutableStateOf<SelectItemModel?>(null) }
        var isBasicExpanded by remember { mutableStateOf(false) }
        
        val basicItems = remember {
            listOf(
                SelectItemModel("选项1", "value1"),
                SelectItemModel("选项2", "value2"),
                SelectItemModel("选项3", "value3"),
                SelectItemModel("选项4", "value4")
            )
        }
        
        BaseSelect(
            modifier = Modifier.fillMaxWidth(),
            isExpanded = isBasicExpanded,
            selectedItem = selectedBasic,
            items = basicItems,
            onToggle = { isBasicExpanded = !isBasicExpanded },
            onItemSelected = { 
                selectedBasic = it
                isBasicExpanded = false
            },
            placeholder = "请选择一个选项",
            maxDropdownHeight = 200.dp
        )

        // 带前置图标
        SectionTitle("带前置图标")
        
        var selectedWithIcon by remember { mutableStateOf<SelectItemModel?>(null) }
        var isIconExpanded by remember { mutableStateOf(false) }
        
        val iconItems = remember {
            listOf(
                SelectItemModel("用户管理", "user"),
                SelectItemModel("角色管理", "role"),
                SelectItemModel("部门管理", "dept"),
                SelectItemModel("权限管理", "permission")
            )
        }
        
        BaseSelect(
            modifier = Modifier.fillMaxWidth(),
            isExpanded = isIconExpanded,
            selectedItem = selectedWithIcon,
            items = iconItems,
            onToggle = { isIconExpanded = !isIconExpanded },
            onItemSelected = { 
                selectedWithIcon = it
                isIconExpanded = false
            },
            placeholder = "请选择功能模块",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            maxDropdownHeight = 200.dp
        )

        // 禁用状态
        SectionTitle("禁用状态")
        
        BaseSelect(
            modifier = Modifier.fillMaxWidth(),
            isExpanded = false,
            selectedItem = SelectItemModel("选项1", "value1"),
            items = listOf(
                SelectItemModel("选项1", "value1"),
                SelectItemModel("选项2", "value2")
            ),
            onToggle = { },
            onItemSelected = { },
            enabled = false,
            maxDropdownHeight = 200.dp
        )

        // 错误状态
        SectionTitle("错误状态")
        
        var selectedError by remember { mutableStateOf<SelectItemModel?>(null) }
        var isErrorExpanded by remember { mutableStateOf(false) }
        
        val errorItems = remember {
            listOf(
                SelectItemModel("北京", "beijing"),
                SelectItemModel("上海", "shanghai"),
                SelectItemModel("广州", "guangzhou"),
                SelectItemModel("深圳", "shenzhen")
            )
        }
        
        BaseSelect(
            modifier = Modifier.fillMaxWidth(),
            isExpanded = isErrorExpanded,
            selectedItem = selectedError,
            items = errorItems,
            onToggle = { isErrorExpanded = !isErrorExpanded },
            onItemSelected = { 
                selectedError = it
                isErrorExpanded = false
            },
            placeholder = "请选择城市",
            isError = true,
            errorMessage = "请选择一个有效的城市",
            maxDropdownHeight = 200.dp
        )

        // 部分选项禁用
        SectionTitle("部分选项禁用")
        
        var selectedPartialDisabled by remember { mutableStateOf<SelectItemModel?>(null) }
        var isPartialDisabledExpanded by remember { mutableStateOf(false) }
        
        val partialDisabledItems = remember {
            listOf(
                SelectItemModel("启用选项1", "enabled1", true),
                SelectItemModel("禁用选项1", "disabled1", false),
                SelectItemModel("启用选项2", "enabled2", true),
                SelectItemModel("禁用选项2", "disabled2", false),
                SelectItemModel("启用选项3", "enabled3", true)
            )
        }
        
        BaseSelect(
            modifier = Modifier.fillMaxWidth(),
            isExpanded = isPartialDisabledExpanded,
            selectedItem = selectedPartialDisabled,
            items = partialDisabledItems,
            onToggle = { isPartialDisabledExpanded = !isPartialDisabledExpanded },
            onItemSelected = { 
                selectedPartialDisabled = it
                isPartialDisabledExpanded = false
            },
            placeholder = "请选择启用的选项",
            maxDropdownHeight = 200.dp
        )

        // 自定义样式
        SectionTitle("自定义样式")
        
        var selectedCustom by remember { mutableStateOf<SelectItemModel?>(null) }
        var isCustomExpanded by remember { mutableStateOf(false) }
        
        val customItems = remember {
            listOf(
                SelectItemModel("选项A", "a"),
                SelectItemModel("选项B", "b"),
                SelectItemModel("选项C", "c"),
                SelectItemModel("选项D", "d")
            )
        }
        
        BaseSelect(
            modifier = Modifier.fillMaxWidth(),
            isExpanded = isCustomExpanded,
            selectedItem = selectedCustom,
            items = customItems,
            onToggle = { isCustomExpanded = !isCustomExpanded },
            onItemSelected = { 
                selectedCustom = it
                isCustomExpanded = false
            },
            placeholder = "自定义样式选择",
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.medium,
            borderWidth = 2.dp,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            maxDropdownHeight = 200.dp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}