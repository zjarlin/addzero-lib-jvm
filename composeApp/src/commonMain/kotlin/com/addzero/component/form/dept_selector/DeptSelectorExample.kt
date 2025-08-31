package com.addzero.component.form.dept_selector

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import com.addzero.annotation.Route
import com.addzero.component.form.selector.AddDeptSelector
import com.addzero.generated.isomorphic.SysDeptIso

/**
 * 🏢 部门选择器使用示例
 *
 * 展示 AddDeptSelector 组件的各种使用方式
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Route
fun DeptSelectorExample() {
    var selectedDepts1 by remember { mutableStateOf<List<SysDeptIso>>(emptyList()) }

    AddDeptSelector(
        value = selectedDepts1,
        onValueChange = { selectedDepts1 = it },
    )

}
