package site.addzero.addzero_common

import site.addzero.model.entity.BizNote
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import kotlin.reflect.full.declaredMemberProperties

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class SpringBootAppTests(
    val kSqlClient: KSqlClient,
) {
//    @Autowired
//    private lateinit var databaseMetadataService: DatabaseMetadataUtil

    //    @Autowired
//    private lateinit var postgresMetadataService: PostgresMetadataService


    @Test
    fun 测试数据库元数据(): Unit {
//        val columnsMetadata = databaseMetadataService.getColumnsMetadata()
//        val foreignKeysMetadata = databaseMetadataService.getForeignKeysMetadata()
//        val primaryKeysMetadata = databaseMetadataService.getPrimaryKeysMetadata()
        println()
    }


//    @Test
//    fun djoaisdjoij(): Unit {
//        val 测试笔记upsert = bizNoteController.测试笔记upsert()
//        println(测试笔记upsert)
//    }

    @Test
    fun `test biz notes upsert`() {
//        val save = kSqlClient.save(BizNote {
//            id = 1111222
//            title = "test title"
//            content = "test content"
//        })
        val createQuery = kSqlClient.createQuery(BizNote::class) {
            select(table)
        }.execute()
        println()


    }


}
