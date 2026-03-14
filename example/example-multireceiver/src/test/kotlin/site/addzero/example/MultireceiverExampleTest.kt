package site.addzero.example

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MultireceiverExampleTest {

  @Test
  fun `generates extension and context wrappers`() {
    assertEquals("<ok>", "ok".wrap())
    assertEquals("svc:3", context(Service("svc")) { render(3) })
    assertEquals("eng[ok]", with(Engine("eng")) { "ok".decorate() })

    val generatedTopLevelMethods = Class
      .forName("site.addzero.example.__GENERATED__CALLABLES__Kt")
      .declaredMethods
      .map { method -> method.name }
      .toSet()

    assertTrue(
      generatedTopLevelMethods.any { methodName ->
        methodName.startsWith("wrapByAddzeroExtension")
      },
      generatedTopLevelMethods.toString(),
    )
    assertTrue(
      generatedTopLevelMethods.any { methodName ->
        methodName.startsWith("renderByAddzeroContext")
      },
      generatedTopLevelMethods.toString(),
    )

    val engineMethods = Engine::class.java.declaredMethods
      .map { method -> method.name }
      .toSet()

    assertTrue(
      engineMethods.any { methodName ->
        methodName.startsWith("decorateByAddzeroExtension")
      },
      engineMethods.toString(),
    )
  }
}
