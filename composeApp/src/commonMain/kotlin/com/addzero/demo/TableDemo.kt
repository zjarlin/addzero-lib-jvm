package com.addzero.demo//package com.addzero.demo
//
//import androidx.compose.runtime.*
//import com.addzero.annotation.Route
//import com.addzero.component.table.AddGenericTable
//import com.addzero.component.table.clean.AddCleanTableViewModel
//import com.addzero.component.table.model.AddCleanColumn
//import com.addzero.entity.low_table.ColumnMetadata
//import com.addzero.entity.low_table.TableMetadata
//import com.addzero.generated.api.ApiProvider.jdbcTableMetadataApi
//import com.addzero.generated.api.ApiProvider.sysDictApi
//import com.addzero.generated.isomorphic.JdbcColumnMetadataIso
//import com.addzero.generated.isomorphic.JdbcTableMetadataIso
//import com.addzero.generated.isomorphic.SysDictIso
//
//
//@Composable
//@Route("组件示例", "测试表格")
//fun AddGenericTableExample() {
//    val tableName = "sys_dict"
//    var data by remember { mutableStateOf(emptyList<SysDictIso>()) }
//    var tablemeta by remember { mutableStateOf(emptyList<JdbcTableMetadataIso>()) }
//    var map by remember { mutableStateOf(mapOf<String, JdbcColumnMetadataIso>()) }
//    var mycolumns by remember { mutableStateOf(emptyList<AddCleanColumn<SysDictIso>>()) }
//
//    LaunchedEffect(Unit) {
//        //随便一个list接口试一下表格组件
//        val tree = sysDictApi.tree("")
//        val tableMetadata = jdbcTableMetadataApi.getTableMetadata(tableName)
//        tablemeta = tableMetadata.toMutableList()
//        data = tree.toMutableList()
//        val associateBy = tablemeta.flatMap { it.columnConfigs }.associateBy { it.columnName }
//        map = associateBy.toMutableMap()
//
//
//        val elements = AddCleanColumn<SysDictIso>(
//            getValueFun = { it.dictCode },
//            setValueFun = { this.copy(dictCode = it.toString()) },
//            columnMetadata = ColumnMetadata(
//                columnName = "dict_code",
//                comment = "字典编码",
//                kmpType = "String"
//            )
//        )
//
//
//        val elements1 = AddCleanColumn<SysDictIso>(
//            getValueFun = { it.dictName },
//            setValueFun = { this.copy(dictName = it.toString()) },
//            columnMetadata = ColumnMetadata(
//                columnName = "dict_name",
//                comment = "字典名称",
//                kmpType = "String"
//            )
//        )
//
//
//        val listOf = listOf(
//            elements,
//            elements1,
//        )
//        mycolumns = listOf.toMutableList()
//
//
//    }
//
//    val addCleanTableViewModel = AddCleanTableViewModel(
//        data = data,
//        columnConfigs = mycolumns,
//        getIdFun = { it.id.toString() },
//        baseCrudApi = null,
//        tableMetadata = TableMetadata()
//    )
//    with(addCleanTableViewModel) {
//        AddGenericTable()
//    }
//
//
//
//}
//
