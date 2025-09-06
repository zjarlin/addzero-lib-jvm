package com.addzero.demo

import androidx.compose.runtime.*
import com.addzero.annotation.Route
import com.addzero.component.dropdown.AddDropDown

/**
 * Select组件演示页面
 * 展示Select组件的各种使用方式和功能
 */
@Composable
@Route("组件示例", "Select选择框2", routePath = "/component/select2")
fun SelectDemo2() {


    val customItems = remember {
        listOf("选项A", "选项B", "选项C", "选项D")
    }

    var exception by mutableStateOf(false)
    AddDropDown(
        options = customItems,
        expanded = exception,
        getLabel = { it },
        onOptionSelected = {
            exception = false
        },
        onDismissRequest = { exception = false }
    )
}


