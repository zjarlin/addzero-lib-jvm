package site.addzero.autoddlstarter.generator

import site.addzero.autoddlstarter.context.DDLContext


abstract class DatabaseDDLGenerator : IDatabaseGenerator {
    abstract val defaultStringType: String

    /** 类型映射 */
    abstract val selfMappingTypeTable: Map<String, String>

    /**建表语句
     * @param [ddlContext]
     * @return [String]
     */
    abstract fun generateCreateTableDDL(ddlContext: DDLContext): String

    /**
     * 加列语句
     * @param [ddlContext]
     * @return [String]
     */
    abstract fun generateAddColDDL(ddlContext: DDLContext): String



}
