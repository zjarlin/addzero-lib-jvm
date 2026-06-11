package site.addzero.core.network.json

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@Serializable
sealed class ShapeConfig {
    abstract val name: String

    @Serializable
    data object Empty : ShapeConfig() {
        override val name: String = "empty"
    }
}

@Serializable
data class RectConfig(override val name: String, val width: Int, val height: Int) : ShapeConfig()

@Serializable
data class CircleConfig(override val name: String, val radius: Int) : ShapeConfig()

class SealedSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    @Test
    fun testSerializeAndDeserializeRect() {
        val shapeConfig: ShapeConfig = ShapeConfig.Empty
        val encoded = json.encodeToString(shapeConfig)
        println("Encoded Empty: $encoded")

        val decoded = json.decodeFromString<ShapeConfig>(encoded)
        println("Decoded Empty: $decoded")

        assertIs<ShapeConfig.Empty>(decoded)
        assertEquals(shapeConfig, decoded)
    }

    @Test
    fun testSerializeAndDeserializeCircle() {
        val original: ShapeConfig = CircleConfig(name = "circle", radius = 50)
        val encoded = json.encodeToString(original)
        println("Encoded Circle: $encoded")

        val decoded = json.decodeFromString<ShapeConfig>(encoded)
        println("Decoded Circle: $decoded")

        assertIs<CircleConfig>(decoded)
        assertEquals(original, decoded)
    }

    @Test
    fun testDeserializeIgnoreUnknownKeys() {
        val jsonWithExtra = """{"type":"site.addzero.core.network.json.RectConfig","name":"rect","width":10,"height":20,"extraField":"ignored"}"""
        val decoded = json.decodeFromString<ShapeConfig>(jsonWithExtra)
        assertIs<RectConfig>(decoded)
        assertEquals(10, decoded.width)
    }
}
