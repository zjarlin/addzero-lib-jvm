package com.addzero.demo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.addzero.annotation.Route
import com.addzero.component.dropdown.Select
import com.addzero.component.dropdown.SelectMode
import com.addzero.hook.UseAutoComplate

/**
 * GenericSelect组件演示页面
 * 展示GenericSelect组件的单选和多选功能
 */
@Composable
@Route("组件示例", "GenericSelect组件", routePath = "/component/genericSelect")
fun GenericSelectDemo() {
    var selectedSingle by remember { mutableStateOf<String?>(null) }
    var selectedMultiple by remember { mutableStateOf<List<String>>(emptyList()) }


    Column {
        Select(
            modifier = Modifier.fillMaxWidth(),
            value = selectedSingle,
            items = listOf("选项1", "选项2", "选项3"),
            onValueChange = { selectedSingle = it },
            placeholder = "请选择一个选项",
            selectMode = SelectMode.SINGLE
        )


        Select(
            modifier = Modifier.fillMaxWidth(),
            values = selectedMultiple,
            items = listOf("选项1", "选项2", "选项3"),
            onValuesChange = { selectedMultiple = it },
            placeholder = "请选择多个选项",
            selectMode = SelectMode.MULTIPLE
        )
        UseAutoComplate(suggestions = listOf("选项1", "选项2", "选项3"), title = "自动补全", getLabelFun = { it }).render{}

    }

}


