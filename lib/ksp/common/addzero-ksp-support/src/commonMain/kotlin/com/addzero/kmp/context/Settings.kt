package com.addzero.kmp.context

data class Settings(
   val baseEntityPackage :String="",
    // 数据库相关配置
    val dbType: String = "",
    val idType: String = "",
    val id: String = "",
    val createBy: String = "",
    val updateBy: String = "",
    val createTime: String = "",
    val updateTime: String = "",

    val composeSourceDir: String = "",
    val composeBuildDir: String = "",
    val sharedSourceDir: String = "",
    val sharedBuildDir: String = "",
    val serverSourceDir: String = "",
    val serverBuildDir: String = "",


    val modelSourceDir: String = "",
    val modelBuildDir: String = "",
    val modelPackageName: String = "",

    // 包名配置（小驼峰命名）
    val isomorphicPackageName: String = "com.addzero.kmp.generated.isomorphic",
    val formPackageName: String = "com.addzero.kmp.generated.forms",
    val enumOutputPackage: String = "com.addzero.kmp.generated.enums",
    val apiClientPackageName: String = "com.addzero.kmp.generated.api",

    // 其他配置
    val isomorphicClassSuffix: String = "Iso",

    // JDBC 配置
    val jdbcUrl: String = "",
    val jdbcUsername: String = "",
    val jdbcPassword: String = "",
    val jdbcSchema: String = "",
    val jdbcDriver: String = "",

    // 字典表配置
    val dictTableName: String = "sys_dict",
    val dictIdColumn: String = "id",
    val dictCodeColumn: String = "dict_code",
    val dictNameColumn: String = "dict_name",
    val dictItemTableName: String = "sys_dict_item",
    val dictItemForeignKeyColumn: String = "dict_id",
    val dictItemCodeColumn: String = "item_value",
    val dictItemNameColumn: String = "item_text"
) {
    /**
     * 同构体输出目录（shared 源码目录）
     */
    val isomorphicOutputDir: String
        get() = "$sharedSourceDir/${isomorphicPackageName.replace(".", "/")}"

    /**
     * 表单输出目录（composeApp 源码目录）
     */
    val formOutputDir: String
        get() = "$composeSourceDir/${formPackageName.replace(".", "/")}"

    /**
     * 枚举输出目录（shared 编译目录）
     */
    val enumOutputDir: String
        get() {
            sharedSourceDir.ifBlank { throw IllegalStateException("sharedBuildDir 不能为空") }
            return "$sharedSourceDir/${enumOutputPackage.replace(".", "/")}"
        }

    /**
     * API客户端输出目录（shared 源码目录）
     */
    val apiClientOutputDir: String
        get() = "$sharedSourceDir/${apiClientPackageName.replace(".", "/")}"


    /**
     * 模型输出目录
     */
    val modelOutputDir: String
        get() = "${modelSourceDir}/${modelPackageName.replace(".", "/")}"


}
