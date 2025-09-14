package site.addzero.entity.low_table

data class TableMetadata( val rowheight: Int = 36, var showActions: Boolean=true)

data class ColumnMetadata(val columnName: String, val comment: String, val kmpType: String = "String",)
