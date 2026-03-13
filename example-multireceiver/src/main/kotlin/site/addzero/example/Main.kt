package site.addzero.example

fun main() {
  println("wrap => ${"ok".wrap()}")
  println("render => ${context(Service("svc")) { render(3) }}")
  println("decorate => ${with(Engine("eng")) { "ok".decorate() }}")

  val generatedTopLevelMethods = Class
    .forName("site.addzero.example.__GENERATED__CALLABLES__Kt")
    .declaredMethods
    .map { method -> method.name }
    .filter { methodName -> "ByAddzero" in methodName }
    .sorted()

  println("generated top-level methods => ${generatedTopLevelMethods.joinToString()}")

  val engineMethods = Engine::class.java.declaredMethods
    .map { method -> method.name }
    .filter { methodName -> "ByAddzero" in methodName }
    .sorted()

  println("generated member methods => ${engineMethods.joinToString()}")
}
