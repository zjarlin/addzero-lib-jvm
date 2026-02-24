import org.gradle.util.internal.TextUtil.toCamelCase
import org.gradle.util.internal.TextUtil.toLowerCamelCase
import kotlin.test.Test

class TestStr {

  @Test
  fun `daoisjdo`() {
    val toCamelCase = toLowerCamelCase("userName")
    val toCamelCase1 = toLowerCamelCase("user.name")
    println(toCamelCase)

  }
  @Test
  fun `dojaisdjo`() {
    val value = ",".split(",")?.filter { it.isNotEmpty() }?.map { it } ?: listOf("src/test/dto", "src/test/dto1")


    println()
  }
}
