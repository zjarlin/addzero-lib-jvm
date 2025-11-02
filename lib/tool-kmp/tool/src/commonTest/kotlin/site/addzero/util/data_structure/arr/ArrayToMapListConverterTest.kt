package site.addzero.util.data_structure.arr

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArrayToMapListConverterTest {

    @Test
    fun testConvertWithFirstRowAsKeys() {
        // Given
        val array = arrayOf(
            arrayOf("ID", "Name", "Age"),
            arrayOf("1", "Alice", "30"),
            arrayOf("2", "Bob", "25")
        )

        // When
        val result = convertWithFirstRowAsKeys(array)

        // Then
        assertEquals(2, result.size)
        assertEquals("1", result[0]["ID"])
        assertEquals("Alice", result[0]["Name"])
        assertEquals("30", result[0]["Age"])
        assertEquals("2", result[1]["ID"])
        assertEquals("Bob", result[1]["Name"])
        assertEquals("25", result[1]["Age"])
    }

    @Test
    fun testConvertWithFirstRowAsKeys_emptyArray() {
        // Given
        val array = emptyArray<Array<String>>()

        // When
        val result = convertWithFirstRowAsKeys(array)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun testConvertWithKeys() {
        // Given
        val array = arrayOf(
            arrayOf("1", "Alice", "30"),
            arrayOf("2", "Bob", "25")
        )
        val keys = arrayOf("ID", "Name", "Age")

        // When
        val result = convertWithKeys(array, keys)

        // Then
        assertEquals(2, result.size)
        assertEquals("1", result[0]["ID"])
        assertEquals("Alice", result[0]["Name"])
        assertEquals("30", result[0]["Age"])
        assertEquals("2", result[1]["ID"])
        assertEquals("Bob", result[1]["Name"])
        assertEquals("25", result[1]["Age"])
    }

    @Test
    fun testConvertWithKeys_emptyArray() {
        // Given
        val array = emptyArray<Array<String>>()
        val keys = arrayOf("ID", "Name", "Age")

        // When
        val result = convertWithKeys(array, keys)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun testConvertWithKeys_mismatchedSizes() {
        // Given
        val array = arrayOf(
            arrayOf("1", "Alice"),
            arrayOf("2", "Bob", "25", "Extra")
        )
        val keys = arrayOf("ID", "Name", "Age")

        // When
        val result = convertWithKeys(array, keys)

        // Then
        assertEquals(2, result.size)
        assertEquals("1", result[0]["ID"])
        assertEquals("Alice", result[0]["Name"])
        assertEquals(null, result[0]["Age"]) // Age should not be present
        assertEquals("2", result[1]["ID"])
        assertEquals("Bob", result[1]["Name"])
        assertEquals("25", result[1]["Age"]) // Extra value is ignored
    }
}
