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
import com.addzero.component.card.MellumCardType
import com.addzero.component.table.TableOriginal
import com.addzero.component.table.TableSlots
import com.addzero.core.ext.toMap
import kotlinx.serialization.Serializable

@Route
@Composable
fun TableOriginalDebugTest() {
    // 测试数据模型 - 添加@Serializable注解
    @Serializable
    data class TestUser(
        val id: Int,
        val name: String,
        val email: String,
        val age: Int,
        val department: String,
        val salary: Double,
        val status: String
    )

    // 列配置模型 - 泛型C，增强配置能力
    data class ColumnConfig(
        val key: String,
        val label: String,
        val color: Color? = null,
        val textStyle: androidx.compose.ui.text.TextStyle? = null,
        val fontWeight: FontWeight? = null,
        val textAlign: TextAlign? = null,
        val formatter: ((Any?) -> String)? = null, // 值格式化器
        val colorResolver: ((Any?) -> Color)? = null // 动态颜色解析器
    )

    // 生成测试数据
    val testUsers = remember {
        val names = listOf("张三", "李四", "王五", "赵六", "钱七")
        val departments = listOf("技术部", "产品部", "设计部")
        val statuses = listOf("在职", "试用期", "实习")

        (1..15).map { i ->
            TestUser(
                id = i,
                name = names[i % names.size] + if (i > names.size) "$i" else "",
                email = "${names[i % names.size].lowercase()}$i@example.com",
                age = 22 + (i % 15),
                department = departments[i % departments.size],
                salary = 8000.0 + (i % 20) * 1000,
                status = statuses[i % statuses.size]
            )
        }
    }

    // 性能优化：使用derivedStateOf预计算所有用户的Map，避免在渲染时重复转换
    val userMapsCache by remember {
        derivedStateOf {
            testUsers.associateWith { user -> user.toMap() }
        }
    }

    // 定义列配置 - 使用配置驱动的方式
    val columns = listOf(
        ColumnConfig(
            "name", "姓名", 
            color = Color(0xFF1976D2),
            fontWeight = FontWeight.Medium
        ),
        ColumnConfig(
            "email", "邮箱", 
            color = Color(0xFF7B1FA2),
            textStyle = MaterialTheme.typography.bodySmall
        ),
        ColumnConfig(
            "age", "年龄",
            textAlign = TextAlign.Center,
            formatter = { "${it}岁" }
        ),
        ColumnConfig(
            "department", "部门",
            color = Color.Gray
        ),
        ColumnConfig(
            "salary", "薪资",
            color = Color(0xFF4CAF50),
            fontWeight = FontWeight.Bold,
            formatter = { value ->
                val doubleValue = value.toString().toDoubleOrNull()
                if (doubleValue != null) "¥${doubleValue.toInt()}" else value.toString()
            }
        ),
        ColumnConfig(
            "status", "状态",
            fontWeight = FontWeight.Medium,
            formatter = { value -> value.toString() },
            colorResolver = { value ->
                when (value.toString()) {
                    "在职" -> Color(0xFF4CAF50)
                    "试用期" -> Color(0xFFFF9800)
                    "实习" -> Color(0xFF2196F3)
                    else -> Color(0xFFF44336)
                }
            }
        )
    )

    // 插槽配置
    val tableSlots = TableSlots<TestUser>(
        headerBar = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "员工管理系统 (${testUsers.size}人)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { /* 添加 */ },
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("添加")
                }
            }
        },
// headerActions 移除，因为现在每行都有操作按钮
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column {
                Text(
                    text = "TableOriginal 香烟排列测试",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "转置LazyRow渲染，每列作为香烟垂直排列",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Column {
                Text(
                    text = "基本表格示例",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                TableOriginal(
                    columns = columns,
                    data = testUsers,
                    getColumnKey = { it.key },
                    getRowId = { it.id },
                    getColumnLabel = { config ->
                        Text(
                            text = config.label,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = config.color ?: Color.Black
                        )
                    },
                    getCellContent = { user, config ->
                        // 性能优化：O1时间复杂度从缓存Map中获取值，避免重复序列化
                        val userMap = userMapsCache[user] ?: emptyMap()
                        val rawValue = userMap[config.key]
                        
                        // 使用formatter格式化值
                        val displayValue = config.formatter?.invoke(rawValue) ?: rawValue.toString()
                        
                        // 使用colorResolver动态计算颜色，避免硬编码if-else
                        val textColor = config.colorResolver?.invoke(rawValue) 
                            ?: config.color 
                            ?: Color.Unspecified
                        
                        // 完全配置驱动的通用渲染
                        Text(
                            text = displayValue,
                            style = config.textStyle ?: MaterialTheme.typography.bodyMedium,
                            color = textColor,
                            fontWeight = config.fontWeight,
                            textAlign = config.textAlign ?: TextAlign.Start
                        )
                    },
                    modifier = Modifier.height(500.dp),
                    slots = tableSlots,
                    // 每行操作按钮 - 修复操作列问题
                    rowActions = { user, index ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { /* 编辑用户 $user */ },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Edit, "编辑", Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { /* 删除用户 $user */ },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, "删除", Modifier.size(16.dp))
                            }
                        }
                    },
                    headerCardType = MellumCardType.Dark,
                    headerCornerRadius = 12.dp,
                    headerElevation = 4.dp
                )
            }
        }
    }
}
