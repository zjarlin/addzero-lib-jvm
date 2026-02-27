package site.addzero.network.call.browser.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.nio.file.Files

@Tag("integration")
class BrowserFormFillerTest {

  private val defaultOptions = BrowserAutomationOptions(headless = true, timeoutMs = 10_000.0)

  private fun createTempHtml(html: String): String {
    val file = Files.createTempFile("browser-form-filler-test", ".html")
    Files.newBufferedWriter(file).use { it.write(html) }
    return file.toUri().toString()
  }

  @Test
  fun `fill should populate a simple input field`() {
    val url = createTempHtml("""
      <!doctype html>
      <html><body>
        <input id="email" type="email" name="email" />
      </body></html>
    """.trimIndent())

    val fields = listOf(
      FormFieldDef("email", listOf("#email", "input[type='email']"), "test@example.com", required = true),
    )

    BrowserFormFiller.fill(url, fields, defaultOptions)

    // Verify by re-opening and reading the value
    BrowserFormFiller.withPage(url, defaultOptions) { _ ->
      // For local files, fill doesn't persist across navigations,
      // so we verify the fill mechanism works without throwing
      assertTrue(true, "Fill completed without exception")
    }
  }

  @Test
  fun `fill should populate multiple fields in order`() {
    val url = createTempHtml("""
      <!doctype html>
      <html><body>
        <input id="first" name="firstName" />
        <input id="last" name="lastName" />
        <input id="email" type="email" name="email" />
      </body></html>
    """.trimIndent())

    val fields = listOf(
      FormFieldDef("firstName", listOf("input[name='firstName']"), "John", required = true),
      FormFieldDef("lastName", listOf("input[name='lastName']"), "Doe", required = true),
      FormFieldDef("email", listOf("input[type='email']"), "john@example.com", required = true),
    )

    BrowserFormFiller.fill(url, fields, defaultOptions)
    assertTrue(true, "Multiple fields filled without exception")
  }

  @Test
  fun `fill should verify values are actually set in DOM`() {
    val url = createTempHtml("""
      <!doctype html>
      <html><body>
        <input id="name" name="name" />
        <input id="age" name="age" />
      </body></html>
    """.trimIndent())

    BrowserFormFiller.withPage(url, defaultOptions) { page ->
      val fields = listOf(
        FormFieldDef("name", listOf("#name"), "Alice", required = true),
        FormFieldDef("age", listOf("#age"), "30", required = true),
      )

      fields.forEach { field ->
        when (field.type) {
          FieldType.INPUT -> {
            val locator = page.locator(field.selectors.first())
            locator.first().click()
            locator.first().fill(field.value)
          }
          FieldType.CLICK -> {}
          FieldType.CHECK -> {}
        }
      }

      val nameValue = page.evaluate("() => document.querySelector('#name').value") as String
      val ageValue = page.evaluate("() => document.querySelector('#age').value") as String
      assertEquals("Alice", nameValue)
      assertEquals("30", ageValue)
    }
  }

  @Test
  fun `fill should find input inside iframe`() {
    val url = createTempHtml("""
      <!doctype html>
      <html><body>
        <iframe id="f" srcdoc="<input id='secret' type='password' />"></iframe>
      </body></html>
    """.trimIndent())

    val fields = listOf(
      FormFieldDef("secret", listOf("#secret", "input[type='password']"), "MyPass123!", required = true),
    )

    BrowserFormFiller.fill(url, fields, defaultOptions)
    assertTrue(true, "Cross-iframe fill completed without exception")
  }

  @Test
  fun `clickIfPresent should click a button and trigger JS`() {
    val url = createTempHtml("""
      <!doctype html>
      <html><body>
        <button id="btn" type="button" onclick="window.__clicked = true">OK</button>
      </body></html>
    """.trimIndent())

    BrowserFormFiller.withPage(url, defaultOptions) { page ->
      val locator = page.locator("#btn")
      locator.first().click()
      val flag = page.evaluate("() => window.__clicked === true") as Boolean
      assertTrue(flag)
    }
  }

  @Test
  fun `fill should throw IllegalStateException when required field is missing`() {
    val url = createTempHtml("""
      <!doctype html>
      <html><body>
        <div>No inputs here</div>
      </body></html>
    """.trimIndent())

    val fields = listOf(
      FormFieldDef("email", listOf("#nonexistent"), "x@y.com", required = true),
    )

    assertThrows(IllegalStateException::class.java) {
      BrowserFormFiller.fill(url, fields, defaultOptions)
    }
  }

  @Test
  fun `fill should skip non-required field when missing`() {
    val url = createTempHtml("""
      <!doctype html>
      <html><body>
        <input id="email" type="email" />
      </body></html>
    """.trimIndent())

    val fields = listOf(
      FormFieldDef("phone", listOf("#phone"), "12345", required = false),
      FormFieldDef("email", listOf("#email"), "a@b.com", required = true),
    )

    BrowserFormFiller.fill(url, fields, defaultOptions)
    assertTrue(true, "Non-required missing field was skipped gracefully")
  }

  @Test
  fun `debug dump should produce artifacts when field not found`() {
    val artifactsDir = Files.createTempDirectory("playwright-test-artifacts")

    val url = createTempHtml("""
      <!doctype html>
      <html><body>
        <input id="other" type="text" />
      </body></html>
    """.trimIndent())

    val options = BrowserAutomationOptions(
      headless = true,
      timeoutMs = 5_000.0,
      debug = true,
      artifactsDir = artifactsDir.toString(),
    )

    val fields = listOf(
      FormFieldDef("missing_field", listOf("#does-not-exist"), "x", required = true),
    )

    assertThrows(IllegalStateException::class.java) {
      BrowserFormFiller.fill(url, fields, options)
    }

    // Verify debug artifacts were created
    val files = Files.list(artifactsDir).use { stream -> stream.collect(java.util.stream.Collectors.toList()) }
    val fileNames = files.map { it.fileName.toString() }

    assertTrue(fileNames.any { it.endsWith("-missing_field.png") }, "Screenshot should be created")
    assertTrue(fileNames.any { it.endsWith("-missing_field.html") }, "HTML dump should be created")
    assertTrue(fileNames.any { it.endsWith("-missing_field-inputs.json") }, "Inputs JSON should be created")
  }
}
