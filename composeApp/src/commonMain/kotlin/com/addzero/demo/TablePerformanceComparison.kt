package com.addzero.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
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
import com.addzero.component.table.TableOriginalOptimized
import com.addzero.component.table.TableSlots
import com.addzero.core.ext.toMap
import kotlinx.serialization.Serializable

@Route
@Composable
fun TablePerformanceComparison() {
    @Serializable
    data class TestData(
        val id: Int,
        val name: String,
        val email: String,
        val age: Int,
        val department: String,
        val salary: Double,
        val status: String,
        val createTime: String,
        val lastLogin: String,
        val performance: String
    )

    data class ColumnConfig(
        val key: String,
        val label: String,
        val color: Color? = null,
        val fontWeight: FontWeight? = null,
        val textAlign: TextAlign? = null,
        val formatter: ((Any?) -> String)? = null,
        val colorResolver: ((Any?) -> Color)? = null
    )

    // 生成大量测试数据
    val largeDataSet = remember {
        val names = listOf("张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十")
        val departments = listOf("技术部", "产品部", "设计部", "运营部", "市场部")
        val statuses = listOf("在职", "试用期", "实习", "离职")
        val performances = listOf("优秀", "良好", "一般", "待改进")

        (1..100).map { i ->
            TestData(
                id = i,
                name = "${names[i % names.size]}$i",
                email = "${names[i % names.size].lowercase()}$i@company.com",
                age = 22 + (i % 20),
                department = departments[i % departments.size],
                salary = 8000.0 + (i % 30) * 1000,
                status = statuses[i % statuses.size],
                createTime = "2024-${(i % 12) + 1}-${(i % 28) + 1}",
                lastLogin = "2024-12-${(i % 30) + 1}",
                performance = performances[i % performances.size]
            )
        }
    }

    // 多列配置测试场景
    val manyColumns = listOf(
        ColumnConfig("name", "姓名", Color(0xFF1976D2), FontWeight.Medium),
        ColumnConfig("email", "邮箱", Color(0xFF7B1FA2)),
        ColumnConfig("age", "年龄", textAlign = TextAlign.Center, formatter = { "${it}岁" }),
        ColumnConfig("department", "部门"),
        ColumnConfig("salary", "薪资", Color(0xFF4CAF50), FontWeight.Bold, formatter = { "¥${it}" }),
        ColumnConfig("status", "状态", fontWeight = FontWeight.Medium, colorResolver = { value ->
            when (value.toString()) {
                "在职" -> Color(0xFF4CAF50)
                "试用期" -> Color(0xFFFF9800)
                "实习" -> Color(0xFF2196F3)
                else -> Color(0xFFF44336)
            }
        }),
        ColumnConfig("createTime", "入职时间"),
        ColumnConfig("lastLogin", "最后登录"),
        ColumnConfig("performance", "绩效评级", colorResolver = { value ->
            when (value.toString()) {
                "优秀" -> Color(0xFF4CAF50)
                "良好" -> Color(0xFF8BC34A)
                "一般" -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            }
        })
    )

    // 性能缓存优化
    val dataMapsCache by remember {
        derivedStateOf {
            largeDataSet.associateWith { it.toMap() }
        }
    }

    // 显示对比状态
    var showComparison by remember { mutableStateOf(false) }

    val tableSlots = TableSlots<TestData>(
        headerBar = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "性能对比测试 (${largeDataSet.size}条数据, ${manyColumns.size}列)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "测试大数据量多字段场景的渲染性能",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showComparison = !showComparison }
                ) {
                    Text(if (showComparison) "隐藏对比" else "显示对比")
                }
            }
        }
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                "TableOriginal 性能对比测试",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (showComparison) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 性能指标卡片
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "原版架构",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "香烟列模式",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "优点: 列独立滚动, 实现简单",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "缺点: 多LazyColumn, 状态同步复杂",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Red
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "优化架构",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "单一虚拟化+叠加",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "优点: 单一LazyColumn, 高性能虚拟化",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Blue
                            )
                            Text(
                                "缺点: 架构复杂度略高",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // 原版架构测试
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "原版架构 - 香烟列模式",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        TableOriginal(
                            columns = manyColumns,
                            data = largeDataSet,
                            getColumnKey = { it.key },
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
                                val userMap = dataMapsCache[user] ?: emptyMap()
                                val rawValue = userMap[config.key]
                                val displayValue = config.formatter?.invoke(rawValue) ?: rawValue.toString()
                                val textColor = config.colorResolver?.invoke(rawValue) 
                                    ?: config.color 
                                    ?: Color.Unspecified

                                Text(
                                    text = displayValue,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textColor,
                                    fontWeight = config.fontWeight,
                                    textAlign = config.textAlign ?: TextAlign.Start
                                )
                            },
                            modifier = Modifier.height(400.dp),
                            slots = tableSlots,
                            rowActions = { user, _ ->
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, "编辑", Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, "删除", Modifier.size(16.dp))
                                    }
                                }
                            },
                            headerCardType = MellumCardType.Light
                        )
                    }
                }
            }

            // 优化架构测试
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "优化架构 - 单一虚拟化+叠加模式",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        TableOriginalOptimized(
                            columns = manyColumns,
                            data = largeDataSet,
                            getColumnKey = { it.key },
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
                                val userMap = dataMapsCache[user] ?: emptyMap()
                                val rawValue = userMap[config.key]
                                val displayValue = config.formatter?.invoke(rawValue) ?: rawValue.toString()
                                val textColor = config.colorResolver?.invoke(rawValue) 
                                    ?: config.color 
                                    ?: Color.Unspecified

                                Text(
                                    text = displayValue,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textColor,
                                    fontWeight = config.fontWeight,
                                    textAlign = config.textAlign ?: TextAlign.Start
                                )
                            },
                            modifier = Modifier.height(400.dp),
                            slots = tableSlots,
                            rowActions = { user, _ ->
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, "编辑", Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, "删除", Modifier.size(16.dp))
                                    }
                                }
                            },
                            headerCardType = MellumCardType.Dark
                        )
                    }
                }
            }

            // 性能分析总结
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "架构对比分析",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "原版香烟列架构:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text("• 每列一个LazyColumn", style = MaterialTheme.typography.bodySmall)
                                Text("• 多个滚动状态需同步", style = MaterialTheme.typography.bodySmall)
                                Text("• 复杂的状态管理", style = MaterialTheme.typography.bodySmall)
                                Text("• 适合少量列场景", style = MaterialTheme.typography.bodySmall)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "优化叠加架构:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text("• 单一LazyColumn虚拟化", style = MaterialTheme.typography.bodySmall)
                                Text("• 固定列zIndex叠加", style = MaterialTheme.typography.bodySmall)
                                Text("• 统一滚动状态", style = MaterialTheme.typography.bodySmall)
                                Text("• 适合大数据量场景", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Divider()
                        Spacer(Modifier.height(8.dp))

                        Text(
                            "关键性能优化:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text("✓ derivedStateOf缓存用户Map转换", style = MaterialTheme.typography.bodySmall)
                        Text("✓ 智能列宽计算和空间分配", style = MaterialTheme.typography.bodySmall)
                        Text("✓ 配置驱动的渲染减少硬编码", style = MaterialTheme.typography.bodySmall)
                        Text("✓ 固定列叠加避免重复虚拟化", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    "TableOriginal 性能对比测试",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "对比原版香烟列架构 vs 优化叠加架构的性能表现",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "测试控制面板",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Button(
                            onClick = { showComparison = !showComparison }
                        ) {
                            Text(if (showComparison) "隐藏对比" else "开始对比测试")
                        }
                    }
                    
                    if (showComparison) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "数据规模: ${largeDataSet.size}行 × ${manyColumns.size}列 = ${largeDataSet.size * manyColumns.size}个单元格",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        if (showComparison) {
            item {
                Text(
                    "观察两种架构在大数据量场景下的:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text("• 初始渲染速度", style = MaterialTheme.typography.bodySmall)
                Text("• 滚动流畅度", style = MaterialTheme.typography.bodySmall)
                Text("• 内存占用", style = MaterialTheme.typography.bodySmall)
                Text("• 操作响应性", style = MaterialTheme.typography.bodySmall)
            }

            items(2) { index ->
                if (index == 0) {
                    Card {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "原版架构 - 香烟列模式",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))

                            TableOriginal(
                                columns = manyColumns,
                                data = largeDataSet,
                                getColumnKey = { it.key },
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
                                    val userMap = dataMapsCache[user] ?: emptyMap()
                                    val rawValue = userMap[config.key]
                                    val displayValue = config.formatter?.invoke(rawValue) ?: rawValue.toString()
                                    val textColor = config.colorResolver?.invoke(rawValue) 
                                        ?: config.color 
                                        ?: Color.Unspecified

                                    Text(
                                        text = displayValue,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = textColor,
                                        fontWeight = config.fontWeight,
                                        textAlign = config.textAlign ?: TextAlign.Start
                                    )
                                },
                                modifier = Modifier.height(400.dp),
                                rowActions = { user, _ ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = { },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, "编辑", Modifier.size(16.dp))
                                        }
                                        IconButton(
                                            onClick = { },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, "删除", Modifier.size(16.dp))
                                        }
                                    }
                                },
                                headerCardType = MellumCardType.Light
                            )
                        }
                    }
                } else {
                    Card {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "优化架构 - 单一虚拟化+叠加",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(Modifier.height(8.dp))

                            TableOriginalOptimized(
                                columns = manyColumns,
                                data = largeDataSet,
                                getColumnKey = { it.key },
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
                                    val userMap = dataMapsCache[user] ?: emptyMap()
                                    val rawValue = userMap[config.key]
                                    val displayValue = config.formatter?.invoke(rawValue) ?: rawValue.toString()
                                    val textColor = config.colorResolver?.invoke(rawValue) 
                                        ?: config.color 
                                        ?: Color.Unspecified

                                    Text(
                                        text = displayValue,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = textColor,
                                        fontWeight = config.fontWeight,
                                        textAlign = config.textAlign ?: TextAlign.Start
                                    )
                                },
                                modifier = Modifier.height(400.dp),
                                rowActions = { user, _ ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = { },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, "编辑", Modifier.size(16.dp))
                                        }
                                        IconButton(
                                            onClick = { },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, "删除", Modifier.size(16.dp))
                                        }
                                    }
                                },
                                headerCardType = MellumCardType.Dark
                            )
                        }
                    }
                }
            }
        }
    }
}