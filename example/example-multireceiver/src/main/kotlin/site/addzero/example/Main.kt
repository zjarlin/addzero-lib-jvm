package site.addzero.example

fun main() {
  println("wrap => ${"ok".wrap()}")
  println("unwrap => ${Box("value").unwrap()}")
  println("render => ${context(Service("svc")) { render(3) }}")
  println("renderWrapped => ${context(Service("svc"), Envelope("[", "]")) { renderWrapped(5) }}")
  println("decorate => ${with(Engine("eng")) { "ok".decorate() }}")

  val generatedMethods = Class
    .forName("site.addzero.example.ExampleTargetsMultireceiverKt")
    .declaredMethods
    .map { method -> method.name }
    .sorted()

  println("generated KSP wrappers => ${generatedMethods.joinToString()}")
}
