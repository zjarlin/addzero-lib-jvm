package com.addzero.autoddlstarter.context


data class DDLFLatContext(
    val tableChineseName: String,
    var tableEnglishName: String,
    val databaseType: String,
    val databaseName: String = "",

    var colName: String,
    val colType: String,
    val colComment: String,
    val colLength: String,
    val isPrimaryKey: String,
    val isSelfIncreasing: String,
)
