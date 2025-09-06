package com.addzero.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.addzero.annotation.Route
import com.addzero.component.dropdown.AddDropDown
import com.addzero.component.dropdown.Select

/**
 * Select组件演示页面
 * 展示Select组件的各种使用方式和功能
 */
@Composable
@Route("组件示例", "Select选择框", routePath = "/component/select")
fun SelectDemo() {

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
            text = "Select 选择框组件",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // 描述
        Text(
            text = "展示Select组件的各种使用方式和功能特性",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        HorizontalDivider()

        // 基础用法
        Text("基础用法 - 字符串类型")

        var selectedString by remember { mutableStateOf<String?>(null) }

        val stringItems = remember {
            listOf("选项1", "选项2", "选项3", "选项4")
        }

        Select(
            modifier = Modifier.fillMaxWidth(),
            value = selectedString,
            items = stringItems,
            onValueChange = { selectedString = it },
            placeholder = "请选择一个选项"
        )

        // 数据类用法
        Text("数据类用法")

        var selectedUser by remember { mutableStateOf<User?>(null) }

        val userItems = remember {
            listOf(
                User(1, "张三", "zhangsan@example.com"),
                User(2, "李四", "lisi@example.com"),
                User(3, "王五", "wangwu@example.com"),
                User(4, "赵六", "zhaoliu@example.com")
            )
        }

        Select(
            modifier = Modifier.fillMaxWidth(),
            value = selectedUser,
            items = userItems,
            onValueChange = { selectedUser = it },
            label = { it.name },
            placeholder = "请选择用户"
        )
        // 自定义选项渲染
        Text("自定义选项渲染")
        var selectedCustom by remember { mutableStateOf<User?>(null) }
        Select(
            modifier = Modifier.fillMaxWidth(),
            value = selectedCustom,
            items = userItems,
            onValueChange = { selectedCustom = it },
            label = { it.name },
            placeholder = "请选择用户",
            itemContent = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = label(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (this@Select.isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            text = this@Select.item.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (this@Select.isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "已选中",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        )

        // 带前置图标
        Text("带前置图标")

        var selectedWithIcon by remember { mutableStateOf<String?>(null) }

        val iconItems = remember {
            listOf("用户管理", "角色管理", "部门管理", "权限管理")
        }

        Select(
            modifier = Modifier.fillMaxWidth(),
            value = selectedWithIcon,
            items = iconItems,
            onValueChange = { selectedWithIcon = it },
            placeholder = "请选择功能模块",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )

        // 禁用状态
        Text("禁用状态")

        Select(
            modifier = Modifier.fillMaxWidth(),
            value = "选项1",
            items = listOf("选项1", "选项2"),
            onValueChange = { },
            enabled = false
        )

        // 错误状态
        Text("错误状态")

        var selectedError by remember { mutableStateOf<String?>(null) }

        val errorItems = remember {
            listOf("北京", "上海", "广州", "深圳")
        }

        Select(
            modifier = Modifier.fillMaxWidth(),
            value = selectedError,
            items = errorItems,
            onValueChange = { selectedError = it },
            placeholder = "请选择城市",
            isError = true,
            errorMessage = "请选择一个有效的城市"
        )

        // 自定义样式
        Text("自定义样式")

        var selectedCustomStyle by remember { mutableStateOf<String?>(null) }

        val customItems = remember {
            listOf("选项A", "选项B", "选项C", "选项D")
        }

        Select(
            modifier = Modifier.fillMaxWidth(),
            value = selectedCustomStyle,
            items = customItems,
            onValueChange = { selectedCustomStyle = it },
            placeholder = "自定义样式选择",
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.medium,
            borderWidth = 2.dp,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        var exception by mutableStateOf(false)
        AddDropDown(
            options = customItems,
            expanded = exception,
            getLabel = { it },
            onOptionSelected = {},
            onDismissRequest = { exception = false }
        )
    }
}


// 示例数据类
data class User(
    val id: Int,
    val name: String,
    val email: String
)
