package com.addzero.kmp.component.table.header.column

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.addzero.kmp.component.table.model.AddCleanColumn

/**
 * 渲染单元格
 * @param [column]
 * @param [item]
 */
@Composable
fun <T> RenderCell(
    column: AddCleanColumn<T>, item: T
) {
    // 使用Box来确保内容居中对齐，并且背景透明
    Box(
        modifier = Modifier
            .width((1f * 150).dp)
            .fillMaxHeight()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        // 渲染自定义内容
        column.customCellRender(item)
    }
}
