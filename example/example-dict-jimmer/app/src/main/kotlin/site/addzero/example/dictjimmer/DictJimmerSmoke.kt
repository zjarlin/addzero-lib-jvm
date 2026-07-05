package site.addzero.example.dictjimmer

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.springframework.context.support.StaticApplicationContext
import site.addzero.aop.dicttrans.anno.Dict
import site.addzero.aop.dicttrans.dictaop.entity.DictModel
import site.addzero.aop.dicttrans.inter.TPredicate
import site.addzero.aop.dicttrans.inter.TransApi
import site.addzero.aop.dicttrans.strategy.CollectionStrategy
import site.addzero.aop.dicttrans.strategy.TStrategy
import site.addzero.aop.dicttrans.util.SpringUtil

@Entity
interface DictJimmerUser {
    @Id
    val id: Long

    @Dict(dicCode = "user_status", serializationAlias = "statusName")
    val status: String

    @Dict(dicCode = "user_role", serializationAlias = "roleNames")
    val roles: String

    @Dict(tab = "sys_dept", codeColumn = "deptId", nameColumn = "deptName", serializationAlias = "deptName")
    val deptId: String
}

data class DictPage<R>(
    val total: Long,
    val records: List<R>,
)

fun main() {
    installFakeTransApi()

    val user = DictJimmerUser {
        id = 1L
        status = "1"
        roles = "admin,user"
        deptId = "D1"
    }

    val collectionResult = CollectionStrategy().trans(listOf(user)).single().asStringMap()
    assertTranslated(collectionResult)

    val singleStrategy = TStrategy(object : TPredicate {
        override fun tBlackList(): List<Class<out Any>> = emptyList()
    })
    check(singleStrategy.support(user)) { "TStrategy 未支持单个 Jimmer 实体：${user.javaClass.name}" }
    val singleResult = singleStrategy.trans(user).asStringMap()
    assertTranslated(singleResult)

    val nestedPage = DictPage(total = 1L, records = listOf(user))
    val nestedStrategy = TStrategy(object : TPredicate {
        override fun tBlackList(): List<Class<out Any>> = emptyList()
    })
    check(nestedStrategy.support(nestedPage)) { "TStrategy 未支持 T<List<R>> 包装对象：${nestedPage.javaClass.name}" }
    val nestedResult = nestedStrategy.trans(nestedPage)
    assertNestedPageTranslated(nestedResult)

    println("DICT_JIMMER_SMOKE_OK ${collectionResult.toSortedMap()}")
}

private fun installFakeTransApi() {
    val context = StaticApplicationContext()
    context.beanFactory.registerSingleton("exampleTransApi", InMemoryTransApi())
    SpringUtil().setApplicationContext(context)
}

private fun Any?.asStringMap(): Map<String, Any?> {
    check(this is Map<*, *>) { "期望 Jimmer 实体被翻译成 Map，实际是 ${this?.javaClass?.name}" }
    @Suppress("UNCHECKED_CAST")
    return this as Map<String, Any?>
}

private fun assertNestedPageTranslated(value: Any?) {
    check(value is DictPage<*>) { "期望包装对象保持 DictPage 类型，实际是 ${value?.javaClass?.name}" }
    check(value.total == 1L) { "分页 total 未保留：$value" }
    val nestedRecord = value.records.single().asStringMap()
    assertTranslated(nestedRecord)
}

private fun assertTranslated(value: Map<String, Any?>) {
    check(value["id"] == 1L) { "id 未保留：$value" }
    check(value["status"] == "1") { "原始 status 未保留：$value" }
    check(value["statusName"] == "启用") { "内置字典单值翻译失败：$value" }
    check(value["roles"] == "admin,user") { "原始 roles 未保留：$value" }
    check(value["roleNames"] == "管理员,普通用户") { "内置字典多值翻译失败：$value" }
    check(value["deptId"] == "D1") { "原始 deptId 未保留：$value" }
    check(value["deptName"] == "研发部") { "任意表翻译失败：$value" }
}

private class InMemoryTransApi : TransApi {
    private val dictRows = listOf(
        DictModel(dictCode = "user_status", value = "1", label = "启用"),
        DictModel(dictCode = "user_status", value = "0", label = "禁用"),
        DictModel(dictCode = "user_role", value = "admin", label = "管理员"),
        DictModel(dictCode = "user_role", value = "user", label = "普通用户"),
    )

    override fun translateDictBatchCode2name(dictCodes: String, keys: String?): List<DictModel> {
        val requestedCodes = dictCodes.csvSet()
        val requestedKeys = keys.csvSet()
        return dictRows.filter { row ->
            row.dictCode in requestedCodes && (requestedKeys.isEmpty() || row.value in requestedKeys)
        }
    }

    override fun translateTableBatchCode2name(
        table: String,
        text: String,
        code: String,
        keys: String,
    ): List<Map<String, Any?>> {
        if (table != "sys_dept") {
            return emptyList()
        }
        val requestedKeys = keys.csvSet()
        return listOf(mapOf(code to "D1", text to "研发部"))
            .filter { row -> requestedKeys.isEmpty() || row[code] in requestedKeys }
    }
}

private fun String?.csvSet(): Set<String> =
    this.orEmpty()
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toSet()
