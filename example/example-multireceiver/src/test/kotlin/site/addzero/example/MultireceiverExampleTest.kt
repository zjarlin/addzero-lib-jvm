package site.addzero.example

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MultireceiverExampleTest {

  @Test
  fun `generates extension and context wrappers`() {
    assertEquals("<ok>", "ok".wrap())
    assertEquals("value", Box("value").unwrap())
    assertEquals("svc:3", context(Service("svc")) { render(3) })
    assertEquals("[svc:5]", context(Service("svc"), Envelope("[", "]")) { renderWrapped(5) })
    assertEquals("eng[ok]", with(Engine("eng")) { "ok".decorate() })

    val generatedMethods = Class
      .forName("site.addzero.example.ExampleTargetsMultireceiverKt")
      .declaredMethods
      .map { method -> method.name }
      .toSet()

    assertTrue("wrap" in generatedMethods, generatedMethods.toString())
    assertTrue("unwrap" in generatedMethods, generatedMethods.toString())
    assertTrue("render" in generatedMethods, generatedMethods.toString())
    assertTrue("renderWrapped" in generatedMethods, generatedMethods.toString())
    assertTrue("decorate" in generatedMethods, generatedMethods.toString())
  }
}
