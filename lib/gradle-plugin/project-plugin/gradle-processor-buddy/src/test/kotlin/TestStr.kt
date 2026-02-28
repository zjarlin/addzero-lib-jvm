
import cn.hutool.core.util.StrUtil
import site.addzero.util.str.toLowCamelCase
import kotlin.test.Test
import kotlin.test.assertTrue

class TestStr {

  @Test
  fun `daoisjdo`() {
    val toLowCamelCase = "user.name".toLowCamelCase()
    assertTrue { toLowCamelCase == "userName" }
  }
  @Test
  fun `dojaisdjo`() {
    val value = ",".split(",")?.filter { it.isNotEmpty() }?.map { it } ?: listOf("src/test/dto", "src/test/dto1")
    println()
  }
}
