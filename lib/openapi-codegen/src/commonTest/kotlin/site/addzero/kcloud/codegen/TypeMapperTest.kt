package site.addzero.kcloud.codegen

import io.swagger.v3.oas.models.media.*
import site.addzero.kcloud.codegen.ir.TypeRef
import kotlin.test.Test
import kotlin.test.assertEquals

class TypeMapperTest {

    // --- string mappings ---

    @Test
    fun `string type maps to String`() {
        val schema = StringSchema()
        assertEquals(TypeRef.Primitive("String"), TypeMapper.map(schema))
    }

    @Test
    fun `string with date-time format maps to String`() {
        val schema = StringSchema().apply { format = "date-time" }
        assertEquals(TypeRef.Primitive("String"), TypeMapper.map(schema))
    }

    // --- integer mappings ---

    @Test
    fun `integer type maps to Int by default`() {
        val schema = IntegerSchema()
        assertEquals(TypeRef.Primitive("Int"), TypeMapper.map(schema))
    }

    @Test
    fun `integer with int32 format maps to Int`() {
        val schema = IntegerSchema().apply { format = "int32" }
        assertEquals(TypeRef.Primitive("Int"), TypeMapper.map(schema))
    }

    @Test
    fun `integer with int64 format maps to Long`() {
        val schema = IntegerSchema().apply { format = "int64" }
        assertEquals(TypeRef.Primitive("Long"), TypeMapper.map(schema))
    }

    // --- number mappings ---

    @Test
    fun `number type maps to Double by default`() {
        val schema = NumberSchema()
        assertEquals(TypeRef.Primitive("Double"), TypeMapper.map(schema))
    }

    @Test
    fun `number with double format maps to Double`() {
        val schema = NumberSchema().apply { format = "double" }
        assertEquals(TypeRef.Primitive("Double"), TypeMapper.map(schema))
    }

    @Test
    fun `number with float format maps to Float`() {
        val schema = NumberSchema().apply { format = "float" }
        assertEquals(TypeRef.Primitive("Float"), TypeMapper.map(schema))
    }

    // --- boolean ---

    @Test
    fun `boolean type maps to Boolean`() {
        val schema = BooleanSchema()
        assertEquals(TypeRef.Primitive("Boolean"), TypeMapper.map(schema))
    }

    // --- array ---

    @Test
    fun `array of strings maps to ListType of String`() {
        val schema = ArraySchema().apply { items = StringSchema() }
        assertEquals(TypeRef.ListType(TypeRef.Primitive("String")), TypeMapper.map(schema))
    }

    @Test
    fun `array without items maps to ListType of JsonElement`() {
        val schema = ArraySchema()
        assertEquals(TypeRef.ListType(TypeRef.JsonElement), TypeMapper.map(schema))
    }

    @Test
    fun `nested array maps correctly`() {
        val schema = ArraySchema().apply {
            items = ArraySchema().apply { items = IntegerSchema() }
        }
        val expected = TypeRef.ListType(TypeRef.ListType(TypeRef.Primitive("Int")))
        assertEquals(expected, TypeMapper.map(schema))
    }

    // --- $ref ---

    @Test
    fun `ref schema maps to Reference`() {
        val schema = Schema<Any>().apply { `$ref` = "#/components/schemas/FavoriteItem" }
        assertEquals(TypeRef.Reference("FavoriteItem"), TypeMapper.map(schema))
    }

    @Test
    fun `ref takes precedence over type`() {
        val schema = Schema<Any>().apply {
            `$ref` = "#/components/schemas/MyModel"
            type = "string"
        }
        assertEquals(TypeRef.Reference("MyModel"), TypeMapper.map(schema))
    }

    // --- unknown / missing ---

    @Test
    fun `null type maps to JsonElement`() {
        val schema = Schema<Any>()
        assertEquals(TypeRef.JsonElement, TypeMapper.map(schema))
    }

    @Test
    fun `unknown type maps to JsonElement`() {
        val schema = Schema<Any>().apply { type = "object" }
        assertEquals(TypeRef.JsonElement, TypeMapper.map(schema))
    }
}
