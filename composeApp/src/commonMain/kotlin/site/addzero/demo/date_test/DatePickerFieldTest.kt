@file:OptIn(ExperimentalMaterial3Api::class)

package site.addzero.demo.date_test

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import site.addzero.annotation.Route


/**
 * 日期选择demo
 */
@Composable
@Route("组件示例", "日期选择器")
fun DatePickerFieldTest() {
    val rememberDatePickerState = rememberDatePickerState().apply {
        displayMode = DisplayMode.Input
    }
    Column {
        DatePicker(
            state = rememberDatePickerState
        )
        Text(rememberDatePickerState.selectedDateMillis.toString())
    }

}
