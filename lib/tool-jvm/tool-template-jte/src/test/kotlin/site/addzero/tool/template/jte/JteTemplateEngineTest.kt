package site.addzero.tool.template.jte

import gg.jte.ContentType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JteTemplateEngineTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var engine: JteTemplateEngine

    @BeforeEach
    fun setUp() {
        tempDir.resolve("greeting.jte").writeText("@param String name\nHello, \${name}!")
        tempDir.resolve("user.jte").writeText("@param java.util.Map<String, Object> model\nUser: \${model.get(\"name\")}, Age: \${model.get(\"age\")}")
        engine = JteTemplateEngine.fromDirectory(tempDir)
    }

    @Test
    fun `test render with typed model`() {
        val result = engine.render("greeting.jte", "World")
        assertEquals("Hello, World!", result)
    }

    @Test
    fun `test render with map`() {
        val params = mapOf("name" to "Alice", "age" to 25)
        val result = engine.renderWithMap("user.jte", params)
        assertEquals("User: Alice, Age: 25", result)
    }

    @Test
    fun `test renderOrNull returns null on error`() {
        val result = engine.renderOrNull("nonexistent.jte", "test")
        assertNull(result)
    }

    @Test
    fun `test renderOrNull returns result on success`() {
        val result = engine.renderOrNull("greeting.jte", "Test")
        assertNotNull(result)
        assertEquals("Hello, Test!", result)
    }

    @Test
    fun `test renderOrElse returns default on error`() {
        val result = engine.renderOrElse("nonexistent.jte", "test") { "fallback" }
        assertEquals("fallback", result)
    }

    @Test
    fun `test renderOrElse returns rendered result on success`() {
        val result = engine.renderOrElse("greeting.jte", "Success") { "fallback" }
        assertEquals("Hello, Success!", result)
    }

    @Test
    fun `test hasTemplate returns true for existing template`() {
        assertTrue(engine.hasTemplate("greeting.jte"))
    }

    @Test
    fun `test hasTemplate returns false for nonexistent template`() {
        assertFalse(engine.hasTemplate("nonexistent.jte"))
    }

    @Test
    fun `test fromDirectory with Html content type`() {
        tempDir.resolve("html.jte").writeText("@param String name\n<p>Hello, \${name}!</p>")
        val htmlEngine = JteTemplateEngine.fromDirectory(tempDir, ContentType.Html)
        val result = htmlEngine.render("html.jte", "World")
        assertEquals("<p>Hello, World!</p>", result)
        assertEquals(ContentType.Html, htmlEngine.contentType)
    }

    @Test
    fun `test static render convenience method`() {
        val result = JteTemplateEngine.render(tempDir, "greeting.jte", "Static")
        assertEquals("Hello, Static!", result)
    }
}
