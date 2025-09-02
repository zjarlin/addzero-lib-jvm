package com.addzero.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.addzero.annotation.Route
import com.addzero.component.button.AddIconButton
import com.addzero.component.table.TableOriginal
import com.addzero.component.table.TableSlots
import com.addzero.core.ext.toMap
import kotlinx.serialization.Serializable

@Route
@Composable
fun TableBigDataTest() {

    // 大数据模型 - 100个字段
    @Serializable
    data class BigDataModel(
        val field001: String, val field002: String, val field003: String, val field004: String, val field005: String,
        val field006: String, val field007: String, val field008: String, val field009: String, val field010: String,
        val field011: String, val field012: String, val field013: String, val field014: String, val field015: String,
        val field016: String, val field017: String, val field018: String, val field019: String, val field020: String,
        val field021: String, val field022: String, val field023: String, val field024: String, val field025: String,
        val field026: String, val field027: String, val field028: String, val field029: String, val field030: String,
        val field031: String, val field032: String, val field033: String, val field034: String, val field035: String,
        val field036: String, val field037: String, val field038: String, val field039: String, val field040: String,
        val field041: String, val field042: String, val field043: String, val field044: String, val field045: String,
        val field046: String, val field047: String, val field048: String, val field049: String, val field050: String,
        val field051: String, val field052: String, val field053: String, val field054: String, val field055: String,
        val field056: String, val field057: String, val field058: String, val field059: String, val field060: String,
        val field061: String, val field062: String, val field063: String, val field064: String, val field065: String,
        val field066: String, val field067: String, val field068: String, val field069: String, val field070: String,
        val field071: String, val field072: String, val field073: String, val field074: String, val field075: String,
        val field076: String, val field077: String, val field078: String, val field079: String, val field080: String,
        val field081: String, val field082: String, val field083: String, val field084: String, val field085: String,
        val field086: String, val field087: String, val field088: String, val field089: String, val field090: String,
        val field091: String, val field092: String, val field093: String, val field094: String, val field095: String,
        val field096: String, val field097: String, val field098: String, val field099: String, val field100: String
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

    // 生成1000行×100字段的大数据集
    val bigDataSet = remember {
        val fieldNames = (1..100).map { "field${it.toString().padStart(3, '0')}" }
        (1..1000).map { rowIndex ->
            val fieldValues = fieldNames.map { fieldName ->
                "${fieldName}_row${rowIndex}_data"
            }

            BigDataModel(
                field001 = fieldValues[0], field002 = fieldValues[1], field003 = fieldValues[2],
                field004 = fieldValues[3], field005 = fieldValues[4], field006 = fieldValues[5],
                field007 = fieldValues[6], field008 = fieldValues[7], field009 = fieldValues[8],
                field010 = fieldValues[9], field011 = fieldValues[10], field012 = fieldValues[11],
                field013 = fieldValues[12], field014 = fieldValues[13], field015 = fieldValues[14],
                field016 = fieldValues[15], field017 = fieldValues[16], field018 = fieldValues[17],
                field019 = fieldValues[18], field020 = fieldValues[19], field021 = fieldValues[20],
                field022 = fieldValues[21], field023 = fieldValues[22], field024 = fieldValues[23],
                field025 = fieldValues[24], field026 = fieldValues[25], field027 = fieldValues[26],
                field028 = fieldValues[27], field029 = fieldValues[28], field030 = fieldValues[29],
                field031 = fieldValues[30], field032 = fieldValues[31], field033 = fieldValues[32],
                field034 = fieldValues[33], field035 = fieldValues[34], field036 = fieldValues[35],
                field037 = fieldValues[36], field038 = fieldValues[37], field039 = fieldValues[38],
                field040 = fieldValues[39], field041 = fieldValues[40], field042 = fieldValues[41],
                field043 = fieldValues[42], field044 = fieldValues[43], field045 = fieldValues[44],
                field046 = fieldValues[45], field047 = fieldValues[46], field048 = fieldValues[47],
                field049 = fieldValues[48], field050 = fieldValues[49], field051 = fieldValues[50],
                field052 = fieldValues[51], field053 = fieldValues[52], field054 = fieldValues[53],
                field055 = fieldValues[54], field056 = fieldValues[55], field057 = fieldValues[56],
                field058 = fieldValues[57], field059 = fieldValues[58], field060 = fieldValues[59],
                field061 = fieldValues[60], field062 = fieldValues[61], field063 = fieldValues[62],
                field064 = fieldValues[63], field065 = fieldValues[64], field066 = fieldValues[65],
                field067 = fieldValues[66], field068 = fieldValues[67], field069 = fieldValues[68],
                field070 = fieldValues[69], field071 = fieldValues[70], field072 = fieldValues[71],
                field073 = fieldValues[72], field074 = fieldValues[73], field075 = fieldValues[74],
                field076 = fieldValues[75], field077 = fieldValues[76], field078 = fieldValues[77],
                field079 = fieldValues[78], field080 = fieldValues[79], field081 = fieldValues[80],
                field082 = fieldValues[81], field083 = fieldValues[82], field084 = fieldValues[83],
                field085 = fieldValues[84], field086 = fieldValues[85], field087 = fieldValues[86],
                field088 = fieldValues[87], field089 = fieldValues[88], field090 = fieldValues[89],
                field091 = fieldValues[90], field092 = fieldValues[91], field093 = fieldValues[92],
                field094 = fieldValues[93], field095 = fieldValues[94], field096 = fieldValues[95],
                field097 = fieldValues[96], field098 = fieldValues[97], field099 = fieldValues[98],
                field100 = fieldValues[99]
            )
        }
    }

    // 性能缓存 - 预计算Map转换
    val dataMapsCache by remember {
        derivedStateOf {
            bigDataSet.associateWith { it.toMap() }
        }
    }

    // 100列配置
    val bigColumns = remember {
        (1..100).map { index ->
            ColumnConfig(
                key = "field${index.toString().padStart(3, '0')}",
                label = "字段$index",
                color = when (index % 5) {
                    0 -> Color(0xFF4CAF50)
                    1 -> Color(0xFF2196F3)
                    2 -> Color(0xFFFF9800)
                    3 -> Color(0xFF9C27B0)
                    else -> Color(0xFF607D8B)
                },
                fontWeight = if (index % 10 == 0) FontWeight.Bold else null,
                textAlign = if (index % 3 == 0) TextAlign.Center else TextAlign.Start
            )
        }
    }

    val tableSlots = TableSlots<BigDataModel>(
        topHeaderBar = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "大数据量表格性能测试",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "1000行 × 100字段 = 100000个单元格",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "单一LazyColumn架构",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "垂直滚动完全同步",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        },
        rowActions = { item, index ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AddIconButton(
                    text = "编辑",
                    imageVector = Icons.Default.Edit,
                    modifier = Modifier.size(20.dp),
                    onClick = { /* 编辑操作 */ }
                )
                AddIconButton(
                    text = "删除",
                    imageVector = Icons.Default.Delete,
                    modifier = Modifier.size(20.dp),
                    onClick = { /* 删除操作 */ }
                )
            }
        }
    )

//    val tableConfig = TableConfig<>(
//        headerCardType = MellumCardType.Dark,
//        headerCornerRadius = 8.dp,
//        headerElevation = 6.dp
//    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    "TableOriginal 大数据量测试",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "测试单一LazyColumn架构在极端数据量下的表现",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "性能测试指标:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("✓ 垂直滚动流畅度", style = MaterialTheme.typography.bodySmall)
                    Text("✓ 水平滚动同步性", style = MaterialTheme.typography.bodySmall)
                    Text("✓ 初始渲染响应速度", style = MaterialTheme.typography.bodySmall)
                    Text("✓ 内存占用稳定性", style = MaterialTheme.typography.bodySmall)
                    Text("✓ 表头数据对齐精度", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            TableOriginal(
                columns = bigColumns,
                data = bigDataSet,
                getColumnKey = { it.key },
                getRowId = { "${it.field001}_${it.field002}" }, // 使用复合ID确保唯一性
                getColumnLabel = { config ->
                    Text(
                        text = config.label,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = config.fontWeight ?: FontWeight.Medium
                        ),
                        color = config.color ?: Color.Black,
                        textAlign = config.textAlign ?: TextAlign.Start
                    )
                },
                getCellContent = { item, config ->
                    val itemMap = dataMapsCache[item] ?: emptyMap()
                    val value = itemMap[config.key] ?: ""

                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = config.color ?: Color.Unspecified,
                        fontWeight = config.fontWeight,
                        textAlign = config.textAlign ?: TextAlign.Start,
                        maxLines = 1
                    )
                },
                modifier = Modifier.height(600.dp),
//                config = tableConfig,
                slots = tableSlots
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "架构验证结果:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text("🎯 单一LazyColumn确保所有行垂直滚动完全同步", style = MaterialTheme.typography.bodySmall)
                    Text("🎯 共享ScrollState确保表头数据行水平滚动同步", style = MaterialTheme.typography.bodySmall)
                    Text("🎯 行虚拟化优化内存占用，支持无限数据量", style = MaterialTheme.typography.bodySmall)
                    Text("🎯 derivedStateOf缓存Map转换避免重复序列化", style = MaterialTheme.typography.bodySmall)
                    Text("🎯 固定列宽计算确保表头数据完美对齐", style = MaterialTheme.typography.bodySmall)

                    Spacer(Modifier.height(8.dp))
                    Divider()
                    Spacer(Modifier.height(8.dp))

                    Text(
                        "对比香烟列架构的改进:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text("❌ 香烟列架构: 多个LazyColumn滚动可能不同步", style = MaterialTheme.typography.bodySmall, color = Color.Red)
                    Text("✅ 行虚拟化架构: 单一LazyColumn滚动完全同步", style = MaterialTheme.typography.bodySmall, color = Color.Blue)
                }
            }
        }
    }
}
