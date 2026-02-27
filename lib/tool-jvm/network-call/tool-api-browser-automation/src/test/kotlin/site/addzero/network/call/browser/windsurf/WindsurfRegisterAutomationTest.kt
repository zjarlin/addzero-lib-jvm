package site.addzero.network.call.browser.windsurf

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import site.addzero.network.call.browser.core.BrowserAutomationOptions

@Tag("integration")
//@Disabled("Depends on external website; enable manually to run")
class WindsurfRegisterAutomationTest {

  private lateinit var automation: WindsurfRegisterAutomation

  @BeforeEach
  fun setUp() {
    automation = WindsurfRegisterAutomation()
  }

  @Test
  fun `should open register page and fill two-step form`() {
    val form = WindsurfRegisterForm(
      email = "demo@example.com",
      password = "StrongPass123!",
      firstName = "Demo",
      lastName = "User",
    )
    val options = WindsurfRegisterOptions(
      autoSubmit = false,
      automation = BrowserAutomationOptions(
        headless = false,
        timeoutMs = 60_000.0,
        slowMoMs = 500.0,
        debug = true,
        artifactsDir = "/tmp/windsurf-playwright",
      ),
    )

    automation.openAndFill(form, options)

    assertTrue(true, "Two-step form filled without exception")
  }

  @Test
  fun `should fill only required fields when optional fields are null`() {
    val form = WindsurfRegisterForm(
      email = "minimal@example.com",
      password = "MinimalPass1!",
    )
    val options = WindsurfRegisterOptions(
      autoSubmit = false,
      automation = BrowserAutomationOptions(
        headless = true,
        timeoutMs = 30_000.0,
      ),
    )

    automation.openAndFill(form, options)

    assertTrue(true, "Minimal form filled without exception")
  }
}
