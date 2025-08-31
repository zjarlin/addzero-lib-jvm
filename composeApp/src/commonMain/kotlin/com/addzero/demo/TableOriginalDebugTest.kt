package com.addzero.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.addzero.annotation.Route
import com.addzero.component.table.TableColumn
import com.addzero.component.table.TableOriginal
import com.addzero.component.card.MellumCardType

@Route
@Composable
fun TableOriginalDebugTest() {
    // 测试数据模型
    data class TestUser(
        val id: Int,
        val name: String,
        val email: String,
        val age: Int,
        val department: String,
        val salary: Double,
        val status: String
    )

    // 生成大量测试数据以便测试垂直滚动
    val testUsers = remember {
        val names = listOf("张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十", "郑十一", "王十二", "冯十三", "陈十四", "褚十五", "卫十六", "蒋十七", "沈十八", "韩十九", "杨二十")
        val departments = listOf("技术部", "产品部", "设计部", "市场部", "人事部", "财务部", "运营部", "客服部", "法务部", "行政部")
        val statuses = listOf("在职", "离职", "试用期", "实习")
        val domains = listOf("example.com", "company.com", "work.cn", "office.net")

        (1..50).map { i ->
            val name = names[i % names.size] + if (i > names.size) "${i}" else ""
            val department = departments[i % departments.size]
            val status = statuses[i % statuses.size]
            val domain = domains[i % domains.size]
            val email = "${name.lowercase().replace("十", "shi")}${i}@${domain}"
            val age = 22 + (i % 20)
            val salary = 8000.0 + (i % 30) * 1000 + (i % 7) * 500

            TestUser(i, name, email, age, department, salary, status)
        }
    }

    // 状态管理
    var checkedItems by remember { mutableStateOf(setOf<TestUser>()) }
    var sortColumn by remember { mutableStateOf("") }
    var currentData by remember { mutableStateOf(testUsers) }

    // 定义表格列 - 使用自动宽度计算
    val columns = remember {
        listOf(
            com.addzero.component.table.TableColumn<TestUser>(
                key = "name",
                label = "姓名",
                minWidth = 80.dp,
                maxWidth = 150.dp,
                sortable = true
            ) { user ->
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            },
            com.addzero.component.table.TableColumn<TestUser>(
                key = "email",
                label = "邮箱地址",
                minWidth = 180.dp,
                maxWidth = 280.dp,
                sortable = true
            ) { user ->
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            com.addzero.component.table.TableColumn<TestUser>(
                key = "age",
                label = "年龄",
                minWidth = 60.dp,
                maxWidth = 80.dp,
                sortable = true
            ) { user ->
                Text(
                    text = "${user.age}岁",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            },
            com.addzero.component.table.TableColumn<TestUser>(
                key = "department",
                label = "所属部门",
                minWidth = 80.dp,
                maxWidth = 100.dp,
                sortable = true
            ) { user ->
                Text(
                    text = user.department,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            com.addzero.component.table.TableColumn<TestUser>(
                key = "salary",
                label = "月薪资",
                minWidth = 80.dp,
                maxWidth = 120.dp,
                sortable = true
            ) { user ->
                Text(
                    text = user.salary.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            com.addzero.component.table.TableColumn<TestUser>(
                key = "status",
                label = "工作状态",
                minWidth = 80.dp,
                maxWidth = 100.dp,
                sortable = true
            ) { user ->
                val statusColor = when (user.status) {
                    "在职" -> Color(0xFF4CAF50)
                    "试用期" -> Color(0xFFFF9800)
                    "实习" -> Color(0xFF2196F3)
                    else -> Color(0xFFF44336)
                }
                Text(
                    text = user.status,
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column {
                Text(
                    text = "TableOriginal 调试测试",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "每行数据作为一个整体烟头卡片渲染，字段在卡片内水平展开",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 测试1：深色表头表格（完整功能 + 垂直滚动）
        item {
            Column {
                Text(
                    text = "测试1：深色表头表格（整行烟头卡片 + 字段水平展开）",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                com.addzero.component.table.TableOriginal(
                    columns = columns,
                    data = currentData,
                    modifier = Modifier.height(500.dp), // 增加高度以便看到垂直滚动
                    headerCardType = com.addzero.component.card.MellumCardType.Dark,
                    headerCornerRadius = 12.dp,
                    headerElevation = 4.dp,
                    showCheckbox = true,
                    checkedItems = checkedItems,
                    onItemChecked = { item, isChecked ->
                        checkedItems = if (isChecked) {
                            checkedItems + item
                        } else {
                            checkedItems - item
                        }
                    },
                    onHeaderSort = { columnKey ->
                        sortColumn = columnKey
                        currentData = when (columnKey) {
                            "name" -> currentData.sortedBy { it.name }
                            "age" -> currentData.sortedBy { it.age }
                            "salary" -> currentData.sortedByDescending { it.salary }
                            "department" -> currentData.sortedBy { it.department }
                            "status" -> currentData.sortedBy { it.status }
                            else -> currentData
                        }
                    },
                    headerBar = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "员工列表 (${currentData.size}人)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { /* 添加用户 */ },
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("添加", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    },
                    headerActions = {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { /* 编辑 */ }) {
                                Icon(Icons.Default.Edit, contentDescription = "编辑", modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { /* 删除 */ }) {
                                Icon(Icons.Default.Delete, contentDescription = "删除", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    selectContent = {
                        if (checkedItems.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "已选择 ${checkedItems.size} 项",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        TextButton(onClick = { checkedItems = emptySet() }) {
                                            Text("清除选择")
                                        }
                                        Button(onClick = { /* 批量操作 */ }) {
                                            Text("批量删除")
                                        }
                                    }
                                }
                            }
                        }
                    },
                    pagination = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "显示 ${currentData.size} 条记录",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        }

        // 测试2：浅色表头表格（简化版）
        item {
            Column {
                Text(
                    text = "测试2：浅色表头表格（整行烟头卡片 + 水平滚动）",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                com.addzero.component.table.TableOriginal(
                    columns = columns, // 显示所有列以测试水平滚动
                    data = testUsers.take(10),
                    modifier = Modifier.height(400.dp),
                    headerCardType = com.addzero.component.card.MellumCardType.Light,
                    headerCornerRadius = 12.dp,
                    headerBar = {
                        Text(
                            text = "简化版员工表（测试水平滚动）",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
            }
        }
    }
}
