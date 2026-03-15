package site.addzero.example

import site.addzero.kcp.annotations.GenerateExtension
import site.addzero.kcp.annotations.Receiver

data class Service(
  val prefix: String,
)

data class Envelope(
  val left: String,
  val right: String,
)

data class Box<T>(
  val value: T,
)

@GenerateExtension
fun wrap(param: String): String = "<$param>"

@GenerateExtension
fun <T> unwrap(param: Box<T>): T = param.value

@GenerateExtension
fun render(
  @Receiver service: Service,
  value: Int,
): String = "${service.prefix}:$value"

@GenerateExtension
fun renderWrapped(
  @Receiver service: Service,
  @Receiver envelope: Envelope,
  value: Int,
): String = "${envelope.left}${service.prefix}:$value${envelope.right}"

class Engine(
  private val prefix: String,
) {
  @GenerateExtension
  fun decorate(param: String): String = "$prefix[$param]"
}

fun main() {
  val wrap = "aaaa".wrap()
  val unwrap = Box("bbbb").unwrap()
  val render = context(Service("svc")) { render(3) }
  val renderWrapped = context(Service("svc"), Envelope("[", "]")) { renderWrapped(5) }
  val originalDecorate = Engine(wrap).decorate("aaa")
  val generatedDecorate = with(Engine(wrap)) { "aaa".decorate() }
  println(wrap)
  println(unwrap)
  println(render)
  println(renderWrapped)
  println(originalDecorate)
  println(generatedDecorate)
}
