package com.addzero.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.addzero.annotation.Route
import com.addzero.component.dropdown.Select
import com.addzero.component.dropdown.SelectMode

/**
 * GenericSelect组件演示页面
 * 展示GenericSelect组件的单选和多选功能
 */
@Composable
@Route("组件示例", "GenericSelect组件", routePath = "/component/genericSelect")
fun GenericSelectDemo() {
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
            text = "GenericSelect 选择框组件",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // 描述
        Text(
            text = "展示GenericSelect组件的单选和多选功能",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        HorizontalDivider()

        // 单选模式
        SectionTitle("单选模式")

        var selectedSingle by remember { mutableStateOf<String?>(null) }

        val items = remember {
            listOf("选项1", "选项2", "选项3", "选项4", "选项5")
        }

        Select(
            modifier = Modifier.fillMaxWidth(),
            value = selectedSingle,
            items = items,
            onValueChange = { selectedSingle = it },
            placeholder = "请选择一个选项",
            selectMode = SelectMode.SINGLE
        )

        Text(
            text = "选中的值: ${selectedSingle ?: "无"}",
            style = MaterialTheme.typography.bodyMedium
        )

        // 多选模式
        SectionTitle("多选模式")

        var selectedMultiple by remember { mutableStateOf<List<String>>(emptyList()) }

        Select(
            modifier = Modifier.fillMaxWidth(),
            values = selectedMultiple,
            items = items,
            onValuesChange = { selectedMultiple = it },
            placeholder = "请选择多个选项",
            selectMode = SelectMode.MULTIPLE
        )

        Text(
            text = "选中的值: ${if (selectedMultiple.isEmpty()) "无" else selectedMultiple.joinToString(", ")}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
