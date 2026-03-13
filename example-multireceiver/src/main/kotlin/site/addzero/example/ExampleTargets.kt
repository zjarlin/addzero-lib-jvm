package site.addzero.example

import site.addzero.kcp.annotations.AddGenerateExtension
import site.addzero.kcp.annotations.Receiver

data class Service(
  val prefix: String,
)

@AddGenerateExtension
fun wrap(param: String): String = "<$param>"

@AddGenerateExtension
fun render(
  @Receiver service: Service,
  value: Int,
): String = "${service.prefix}:$value"

class Engine(
  private val prefix: String,
) {
  @AddGenerateExtension
  fun decorate(param: String): String = "$prefix[$param]"
}
