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
import com.addzero.component.dropdown.SelectMode
import com.addzero.hook.UseSelect

/**
 * UseSelect Hook演示页面
 * 展示UseSelect Hook的使用方式
 */
@Composable
@Route("组件示例", "UseSelect Hook", routePath = "/hook/useSelect")
fun UseSelectDemo() {
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
            text = "UseSelect Hook演示",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // 描述
        Text(
            text = "展示UseSelect Hook的使用方式",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        HorizontalDivider()

        // 单选模式示例
        SectionTitle("单选模式")

        val singleSelectItems = listOf("选项1", "选项2", "选项3", "选项4", "选项5")

        UseSelect(
            items = singleSelectItems,
            title = "单选下拉",
            getLabelFun = { it },
            placeholder = "请选择一个选项",
            selectMode = SelectMode.SINGLE,
            initialValue = null
        ).Render {
            Text(

                text = "选中的值: ${state.selectedValue ?: "无"}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // 多选模式示例
        SectionTitle("多选模式")

        val multiSelectItems = listOf("苹果", "香蕉", "橙子", "葡萄", "草莓")

        UseSelect(
            items = multiSelectItems,
            title = "多选下拉",
            getLabelFun = { it },
            placeholder = "请选择多个选项",
            selectMode = SelectMode.MULTIPLE,
            initialValues = emptyList()
        ).Render {
            Text(
                text = "选中的值: ${if (state.selectedValues.isEmpty()) "无" else state.selectedValues.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

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
