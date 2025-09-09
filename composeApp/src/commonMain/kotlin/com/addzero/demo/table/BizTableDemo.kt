package com.addzero.demo.table

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.addzero.annotation.Route
import com.addzero.component.table.biz.BizTable
import com.addzero.component.table.original.entity.ColumnConfig

@Route
@Composable
fun TableBigDataTest3() {
    val (bigDataSet, bigColumns) = mockData()

    BizTable(
        data = bigDataSet,
        columns = bigColumns,
        getColumnKey = { it.key },
//        getRowId = { "${it.field001}_${it.field002}" }, // 使用复合ID确保唯一性
//        columnConfigs = listOf(
//            ColumnConfig("field001", "字段1"),
//            ColumnConfig("field002", "字段2"),
//            ColumnConfig("field003", "字段3"),
//            ColumnConfig("field004", "字段4"),
//        ),
        getColumnLabel = { config ->
            Text(text = config.label)
        },
//        buttonSlot = {},
//        topSlot = {
//            //一般渲染搜索区(搜索框,字段高级搜索)
//        },

//        bottomSlot = {
//            //一般渲染分页控件
//        },
//        emptyContentSlot = {
//            //当表格数据为空时,渲染自定义内容(可以是动画)
//        },
//        getCellContent = { row, col ->
//            //自定义渲染
//            when (col.key) {
//                "field001" -> Text(text = row.field001)
//                "field002" -> Text(text = row.field002)
//                "field003" -> Text(text = row.field003)
//                "field004" -> Text(text = row.field004)
//                "field005" -> Text(text = row.field005)
//                "field006" -> Text(text = row.field006)
//                "field007" -> Text(text = row.field007)
//            }
//        },
//        rowLeftSlot = { row, index ->
////           一般是每一个左侧的复选框(开启多选模式)
//        },
//        rowActionSlot = {
//            //操作区(编辑和删除)
//            AddEditDeleteButton(
//                onEditClick = {},
//                onDeleteClick = {}
//            )
//
//        },
//        buttonSlot = {}
    )


}
