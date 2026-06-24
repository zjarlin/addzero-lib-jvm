package site.addzero.jimmer.lowquery.processor

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import kotlin.test.Test
import kotlin.test.assertContains
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies

class JimmerLowQueryGeneratorTest {
    @Test
    fun `generator emits typed query extension`() {
        val codeGenerator = RecordingCodeGenerator()
        val entity = LowQueryEntityMeta(
            packageName = "demo.system",
            simpleName = "SystemConfig",
            qualifiedName = "demo.system.SystemConfig",
            functionName = "query",
            clientFunctionName = "createLowQuery",
            visibility = LowQueryVisibility.PRIVATE,
            clientVisibility = LowQueryVisibility.PUBLIC,
            fetcher = LowQueryFetcher.ALL_SCALAR_FIELDS,
            params = listOf(
                LowQueryParamMeta(
                    propertyName = "configKey",
                    parameterName = "key",
                    typeName = "String",
                    operator = LowQueryOperator.EQ,
                    nullable = false,
                ),
            ),
        )

        JimmerLowQueryGenerator(codeGenerator).generate(setOf(entity), generatedPackage = null)

        val code = codeGenerator.generated.values.single().toString(Charsets.UTF_8.name())
        assertContains(code, "package demo.system.generated.lowquery")
        assertContains(code, "import demo.system.configKey")
        assertContains(code, "private fun KMutableRootQuery.ForEntity<SystemConfig>.query(")
        assertContains(code, "key: String")
        assertContains(code, "where(table.configKey `eq?` key)")
        assertContains(code, "return select(table.fetchBy { allScalarFields() })")
        assertContains(code, "@JvmName(\"createLowQueryForSystemConfigByEntity\")")
        assertContains(code, "public fun KSqlClient.createLowQuery(")
        assertContains(code, "entity: SystemConfig")
        assertContains(code, "return createQuery(SystemConfig::class) {")
        assertContains(code, "if (ImmutableObjects.isLoaded(entity, \"configKey\"))")
        assertContains(code, "where(table.configKey `eq?` entity.configKey)")
        assertContains(code, "select(table.fetchBy { allScalarFields() })")
    }

    @Test
    fun `generator skips nullable condition when null`() {
        val codeGenerator = RecordingCodeGenerator()
        val entity = LowQueryEntityMeta(
            packageName = "demo.device",
            simpleName = "Device",
            qualifiedName = "demo.device.Device",
            functionName = "queryByName",
            clientFunctionName = "createLowQuery",
            visibility = LowQueryVisibility.PUBLIC,
            clientVisibility = LowQueryVisibility.PUBLIC,
            fetcher = LowQueryFetcher.ALL_TABLE_FIELDS,
            params = listOf(
                LowQueryParamMeta(
                    propertyName = "name",
                    parameterName = "name",
                    typeName = "String?",
                    operator = LowQueryOperator.LIKE,
                    nullable = true,
                ),
            ),
        )

        JimmerLowQueryGenerator(codeGenerator).generate(setOf(entity), generatedPackage = "demo.generated")

        val code = codeGenerator.generated.values.single().toString(Charsets.UTF_8.name())
        assertContains(code, "package demo.generated")
        assertContains(code, "name: String? = null")
        assertContains(code, "where(table.name.`ilike?`(name, LikeMode.ANYWHERE))")
        assertContains(code, "return select(table.fetchBy { allTableFields() })")
    }

    @Test
    fun `generator emits collection parameter for in operator`() {
        val codeGenerator = RecordingCodeGenerator()
        val entity = LowQueryEntityMeta(
            packageName = "demo.system",
            simpleName = "SystemConfig",
            qualifiedName = "demo.system.SystemConfig",
            functionName = "queryByIds",
            clientFunctionName = "createLowQuery",
            visibility = LowQueryVisibility.PUBLIC,
            clientVisibility = LowQueryVisibility.PUBLIC,
            fetcher = LowQueryFetcher.TABLE,
            params = listOf(
                LowQueryParamMeta(
                    propertyName = "id",
                    parameterName = "ids",
                    typeName = "Collection<Long>",
                    operator = LowQueryOperator.IN,
                    nullable = false,
                ),
            ),
        )

        JimmerLowQueryGenerator(codeGenerator).generate(setOf(entity), generatedPackage = null)

        val code = codeGenerator.generated.values.single().toString(Charsets.UTF_8.name())
        assertContains(code, "ids: Collection<Long>")
        assertContains(code, "where(table.id `valueIn?` ids)")
        assertContains(code, "return select(table)")
    }
}

private class RecordingCodeGenerator : CodeGenerator {
    val generated = linkedMapOf<String, ByteArrayOutputStream>()

    override val generatedFile: Collection<java.io.File>
        get() = emptyList()

    override fun createNewFile(
        dependencies: Dependencies,
        packageName: String,
        fileName: String,
        extensionName: String,
    ): OutputStream {
        val stream = ByteArrayOutputStream()
        generated["$packageName.$fileName.$extensionName"] = stream
        return stream
    }

    override fun createNewFileByPath(
        dependencies: Dependencies,
        path: String,
        extensionName: String,
    ): OutputStream {
        val stream = ByteArrayOutputStream()
        generated["$path.$extensionName"] = stream
        return stream
    }

    override fun associate(
        sources: List<com.google.devtools.ksp.symbol.KSFile>,
        packageName: String,
        fileName: String,
        extensionName: String,
    ) = Unit

    override fun associateByPath(
        sources: List<com.google.devtools.ksp.symbol.KSFile>,
        path: String,
        extensionName: String,
    ) = Unit

    override fun associateWithClasses(
        classes: List<com.google.devtools.ksp.symbol.KSClassDeclaration>,
        packageName: String,
        fileName: String,
        extensionName: String,
    ) = Unit
}
