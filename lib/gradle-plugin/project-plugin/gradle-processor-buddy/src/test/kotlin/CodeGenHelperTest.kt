import site.addzero.gradle.plugin.CodeGenHelper
import kotlin.test.Test
import kotlin.test.assertEquals

class CodeGenHelperTest {

    // ─────────────────────────────────────────────
    // inferType
    // ─────────────────────────────────────────────

    @Test
    fun `inferType - comma-only value is List String`() {
        assertEquals("List<String>", CodeGenHelper.inferType(","))
    }

    @Test
    fun `inferType - boolean true`() {
        assertEquals("Boolean", CodeGenHelper.inferType("true"))
    }

    @Test
    fun `inferType - boolean false`() {
        assertEquals("Boolean", CodeGenHelper.inferType("false"))
    }

    @Test
    fun `inferType - plain string`() {
        assertEquals("String", CodeGenHelper.inferType("static"))
    }

    @Test
    fun `inferType - listOf with real quotes is List String`() {
        // This is the key bug case: value = listOf("src/main/dto")
        val value = """listOf("src/main/dto")"""
        assertEquals("List<String>", CodeGenHelper.inferType(value))
    }

    @Test
    fun `inferType - listOf with multiple string elements`() {
        val value = """listOf("src/main/dto", "src/test/dto")"""
        assertEquals("List<String>", CodeGenHelper.inferType(value))
    }

    @Test
    fun `inferType - listOf with escaped quotes (backslash form)`() {
        // In Kotlin source: "listOf(\"src/main/dto\")" — at runtime the string is listOf("src/main/dto")
        // But if someone passes the raw escaped form as a literal string value:
        val value = "listOf(\\\"src/main/dto\\\")"
        assertEquals("List<String>", CodeGenHelper.inferType(value))
    }

    @Test
    fun `inferType - triple-quoted listOf`() {
        val value = "\"\"\"listOf(\"src/main/dto\")\"\"\""
        assertEquals("List<String>", CodeGenHelper.inferType(value))
    }

    @Test
    fun `inferType - emptyList`() {
        assertEquals("List<Any>", CodeGenHelper.inferType("emptyList()"))
    }

    @Test
    fun `inferType - integer`() {
        assertEquals("Int", CodeGenHelper.inferType("42"))
    }

    @Test
    fun `inferType - double`() {
        assertEquals("Double", CodeGenHelper.inferType("3.14"))
    }

    @Test
    fun `inferType - comma-separated booleans is List Boolean`() {
        assertEquals("List<Boolean>", CodeGenHelper.inferType("true,false,true"))
    }

    @Test
    fun `inferType - comma-separated integers is List Int`() {
        assertEquals("List<Int>", CodeGenHelper.inferType("1,2,3"))
    }

    // ─────────────────────────────────────────────
    // toDefaultValueExpression
    // ─────────────────────────────────────────────

    @Test
    fun `toDefaultValueExpression - listOf with real quotes returns as-is`() {
        val value = """listOf("src/main/dto")"""
        val result = CodeGenHelper.toDefaultValueExpression(value, "List<String>")
        assertEquals("""listOf("src/main/dto")""", result)
    }

    @Test
    fun `toDefaultValueExpression - listOf with multiple elements returns as-is`() {
        val value = """listOf("src/main/dto", "src/test/dto")"""
        val result = CodeGenHelper.toDefaultValueExpression(value, "List<String>")
        assertEquals("""listOf("src/main/dto", "src/test/dto")""", result)
    }

    @Test
    fun `toDefaultValueExpression - escaped listOf unescapes correctly`() {
        val value = "listOf(\\\"src/main/dto\\\")"
        val result = CodeGenHelper.toDefaultValueExpression(value, "List<String>")
        assertEquals("""listOf("src/main/dto")""", result)
    }

    @Test
    fun `toDefaultValueExpression - comma-only becomes emptyList`() {
        val result = CodeGenHelper.toDefaultValueExpression(",", "List<String>")
        assertEquals("emptyList()", result)
    }

    @Test
    fun `toDefaultValueExpression - boolean true`() {
        assertEquals("true", CodeGenHelper.toDefaultValueExpression("true", "Boolean"))
    }

    @Test
    fun `toDefaultValueExpression - boolean false`() {
        assertEquals("false", CodeGenHelper.toDefaultValueExpression("false", "Boolean"))
    }

    @Test
    fun `toDefaultValueExpression - plain string wraps in quotes`() {
        assertEquals("\"static\"", CodeGenHelper.toDefaultValueExpression("static", "String"))
    }

    @Test
    fun `toDefaultValueExpression - comma-separated strings becomes listOf`() {
        val result = CodeGenHelper.toDefaultValueExpression("a,b,c", "List<String>")
        assertEquals("""listOf("a", "b", "c")""", result)
    }

    @Test
    fun `toDefaultValueExpression - integer`() {
        assertEquals("42", CodeGenHelper.toDefaultValueExpression("42", "Int"))
    }

    // ─────────────────────────────────────────────
    // toSerializationExpression
    // ─────────────────────────────────────────────

    @Test
    fun `toSerializationExpression - List type uses joinToString`() {
        val result = CodeGenHelper.toSerializationExpression("jimmer.dto.dirs", "jimmerDtoDirs", "List<String>")
        assertEquals("""        "jimmer.dto.dirs" to jimmerDtoDirs.joinToString(",")""", result)
    }

    @Test
    fun `toSerializationExpression - String type uses toString`() {
        val result = CodeGenHelper.toSerializationExpression("jimmer.dto.defaultNullableInputModifier", "jimmerDtoDefaultNullableInputModifier", "String")
        assertEquals("""        "jimmer.dto.defaultNullableInputModifier" to jimmerDtoDefaultNullableInputModifier.toString()""", result)
    }

    @Test
    fun `toSerializationExpression - Boolean type uses toString`() {
        val result = CodeGenHelper.toSerializationExpression("jimmer.dto.mutable", "jimmerDtoMutable", "Boolean")
        assertEquals("""        "jimmer.dto.mutable" to jimmerDtoMutable.toString()""", result)
    }

    // ─────────────────────────────────────────────
    // toFromOptionsExpression
    // ─────────────────────────────────────────────

    @Test
    fun `toFromOptionsExpression - List String with listOf default`() {
        val value = """listOf("src/main/dto")"""
        val result = CodeGenHelper.toFromOptionsExpression("jimmer.dto.dirs", "jimmerDtoDirs", "List<String>", value)
        assertEquals(
            """        this.jimmerDtoDirs = options["jimmer.dto.dirs"]?.split(",")?.filter { it.isNotEmpty() }?.map { it } ?: listOf("src/main/dto")""",
            result
        )
    }

    @Test
    fun `toFromOptionsExpression - List String with comma default becomes emptyList`() {
        val result = CodeGenHelper.toFromOptionsExpression("jimmer.source.includes", "jimmerSourceIncludes", "List<String>", ",")
        assertEquals(
            """        this.jimmerSourceIncludes = options["jimmer.source.includes"]?.split(",")?.filter { it.isNotEmpty() }?.map { it } ?: emptyList()""",
            result
        )
    }

    @Test
    fun `toFromOptionsExpression - Boolean`() {
        val result = CodeGenHelper.toFromOptionsExpression("jimmer.dto.mutable", "jimmerDtoMutable", "Boolean", "true")
        assertEquals(
            """        this.jimmerDtoMutable = options["jimmer.dto.mutable"]?.toBoolean() ?: true""",
            result
        )
    }

    @Test
    fun `toFromOptionsExpression - String`() {
        val result = CodeGenHelper.toFromOptionsExpression("jimmer.dto.defaultNullableInputModifier", "jimmerDtoDefaultNullableInputModifier", "String", "static")
        assertEquals(
            """        this.jimmerDtoDefaultNullableInputModifier = options["jimmer.dto.defaultNullableInputModifier"] ?: "static"""",
            result
        )
    }

    // ─────────────────────────────────────────────
    // Full jimmer mustMap scenario
    // ─────────────────────────────────────────────

    @Test
    fun `full jimmer mustMap - all types inferred correctly`() {
        val mustMap = mapOf(
            "jimmer.source.includes" to ",",
            "jimmer.source.excludes" to ",",
            "jimmer.dto.defaultNullableInputModifier" to "static",
            "jimmer.dto.dirs" to """listOf("src/main/dto")""",
            "jimmer.dto.testDirs" to """listOf("src/test/dto")""",
            "jimmer.dto.mutable" to "true",
            "jimmer.client.checkedException" to "true",
            "jimmer.excludedUserAnnotationPrefixes" to ",",
            "jimmer.immutable.isModuleRequired" to "true",
            "jimmer.dto.hibernateValidatorEnhancement" to "true",
            "jimmer.buddy.ignoreResourceGeneration" to "true",
        )

        val expected = mapOf(
            "jimmer.source.includes" to "List<String>",
            "jimmer.source.excludes" to "List<String>",
            "jimmer.dto.defaultNullableInputModifier" to "String",
            "jimmer.dto.dirs" to "List<String>",
            "jimmer.dto.testDirs" to "List<String>",
            "jimmer.dto.mutable" to "Boolean",
            "jimmer.client.checkedException" to "Boolean",
            "jimmer.excludedUserAnnotationPrefixes" to "List<String>",
            "jimmer.immutable.isModuleRequired" to "Boolean",
            "jimmer.dto.hibernateValidatorEnhancement" to "Boolean",
            "jimmer.buddy.ignoreResourceGeneration" to "Boolean",
        )

        mustMap.forEach { (key, value) ->
            val inferred = CodeGenHelper.inferType(value)
            assertEquals(expected[key], inferred, "Key '$key' with value '$value'")
        }
    }

    @Test
    fun `full jimmer mustMap - toSerializationExpression for dirs uses joinToString`() {
        val value = """listOf("src/main/dto")"""
        val type = CodeGenHelper.inferType(value)
        assertEquals("List<String>", type)

        val serExpr = CodeGenHelper.toSerializationExpression("jimmer.dto.dirs", "jimmerDtoDirs", type)
        assertEquals("""        "jimmer.dto.dirs" to jimmerDtoDirs.joinToString(",")""", serExpr)
    }

    @Test
    fun `full jimmer mustMap - toDefaultValueExpression for dirs preserves listOf expression`() {
        val value = """listOf("src/main/dto")"""
        val type = CodeGenHelper.inferType(value)
        val defaultExpr = CodeGenHelper.toDefaultValueExpression(value, type)
        assertEquals("""listOf("src/main/dto")""", defaultExpr)
    }
}
