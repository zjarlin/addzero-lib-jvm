package com.addzero.kmp.component.table.model

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.addzero.kmp.component.form.DynamicFormItem
import com.addzero.kmp.entity.low_table.ColumnMetadata

@Stable
data class AddCleanColumn<T>(
    val getValueFun: (T) -> Any?, val setValueFun: T.(Any?) -> T, val columnMetadata: ColumnMetadata
) {
    var customFormRender: @Composable ((T) -> Unit) = { item ->
        DynamicFormItem(
            value = getValueFun(item), onValueChange = {
                // 确保item不为null时才设置值
                if (item != null) {
                    item.setValueFun(it)
                }
            }, title = columnMetadata.comment, kmpType = columnMetadata.kmpType

        )
    }
    var customCellRender: @Composable ((T) -> Unit) = { item ->
        Text(getValueFun(item).toString())
    }




}

