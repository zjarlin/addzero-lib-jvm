//package site.addzero.lib_adpator
//
//import site.addzero.aop.dicttrans.dictaop.entity.DictModel
//import site.addzero.aop.dicttrans.inter.TransApi
//import site.addzero.common.consts.sql
//import site.addzero.model.entity.*
//import org.babyfish.jimmer.sql.kt.ast.expression.`valueIn?`
//import org.springframework.jdbc.core.JdbcTemplate
//import org.springframework.stereotype.Component
//
//@Component
//class TransApiImpl(val jdbcTemplate: JdbcTemplate) : TransApi {
//    override fun translateDictBatchCode2name(
//        dictCodes: String,
//        keys: String?
//    ): List<DictModel> {
//        val split1 = keys?.split(",")
//        val executeQuery = sql.executeQuery(SysDict::class) {
//            //字典类型在这个集合
//            where(table.dictCode `valueIn?` dictCodes.split(","))
//            where(
//                table.sysDictItems {
//                    itemValue `valueIn?` split1
//                }
//            )
//            select(table.fetchBy {
//                allScalarFields()
//               sysDictItems {
//                   allScalarFields()
//               }
//            })
//        }
//        val flatMap = executeQuery.
//
//        flatMap {
//            val dictCode = it.dictCode
//            it.sysDictItems.map {
//                DictModel(
//                    dictCode = dictCode,
//                    value = it.itemValue,
//                    label = it.itemText
//                )
//            }
//        }
//        return flatMap
//    }
//
//    override fun translateTableBatchCode2name(
//        table: String,
//        text: String,
//        code: String,
//        keys: String
//    ): List<Map<String, Any?>> {
//        val keyList = keys.split(",")
//        val placeholders = keyList.joinToString(",") { "?" }
//        val sql = """
//            SELECT $code, $text FROM $table
//            WHERE $code IN ($placeholders)
//        """.trimIndent()
//
//        // 使用参数化查询防止SQL注入
//        val args = keyList.toTypedArray()
//        val queryForList = jdbcTemplate.queryForList(sql, *args)
//        return queryForList
//    }
//}
