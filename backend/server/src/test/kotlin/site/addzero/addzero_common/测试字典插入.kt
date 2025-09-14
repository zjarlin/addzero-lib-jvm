package site.addzero.addzero_common

import site.addzero.model.entity.*
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.`ilike?`
import org.babyfish.jimmer.sql.kt.ast.expression.or
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.babyfish.jimmer.sql.runtime.LogicalDeletedBehavior
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor


@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class 测试字典插入(
    val sql: KSqlClient,
) {


    @Test
    fun 测试查询回收站字段() {
        val executeQuery = sql
            .filters {
                setBehavior(LogicalDeletedBehavior.IGNORED)
            }.findAll(newFetcher(SysDict::class).by {})

        println(executeQuery)


    }


    /**
     * 查询已经逻辑删除的
     */
    @Test
    fun 测试查询回收站字段1() {
        val executeQuery = sql
            .filters {
                setBehavior(LogicalDeletedBehavior.IGNORED)
            }
            .executeQuery(SysDict::class) {
                select(table)
            }
        println(executeQuery)


    }


    @Test
    fun testaa() {

        //这里dict_code是key
        val map1 = (1..5).map {
            SysDict {
                dictName = "dictName$it"
                dictCode = "aa"
            }
        }
        val saveEntities = sql.saveEntities(map1) {
            setMode(SaveMode.NON_IDEMPOTENT_UPSERT)
        }
        println()


    }


    @Test
    fun testSelect() {
        val keyword = "男"


        val createQuery = site.addzero.common.consts.sql.executeQuery(SysDict::class) {
            where(
                or(
                    table.dictName `ilike?` keyword, table.sysDictItems {
                        itemText `ilike?` keyword
                    }

                ))

            select(
                table.fetchBy {
                    allScalarFields()
                    sysDictItems {
                        allScalarFields()
                    }
                })
        }


//        val createQuery = sql.executeQuery(SysDict::class) {
//            select(
//                table.fetchBy {
//                    allScalarFields()
//                    sysDictItems {
//                        allScalarFields()
//                    }
//
//                })
//        }
        println(createQuery)
    }


}
