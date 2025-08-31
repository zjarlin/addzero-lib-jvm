package com.addzero.autoddlstarter.context

data class DDLContext(
    val tableChineseName: String,
    var tableEnglishName: String,
    val databaseType: String,
    val databaseName: String = "",
    val dto: List<DDlRangeContext>,
)


data class DDlRangeContext(
    val ktName:String,
    var colName: String,
    val colType: String,
    val colComment: String,
    val colLength: String,
    val primaryKeyFlag: String,
    val selfIncreasingFlag: String,
    val nullableFlag: String="",
    val ktType: String,
)

