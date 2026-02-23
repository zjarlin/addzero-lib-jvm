import org.gradle.util.internal.TextUtil.toCamelCase
import org.gradle.util.internal.TextUtil.toLowerCamelCase
import site.addzero.util.str.toCamelCase
import kotlin.test.Test

class TestStr {

  @Test
  fun `daoisjdo`() {
    val toCamelCase = toLowerCamelCase("userName")
    val toCamelCase1 = toLowerCamelCase("user.name")
    println(toCamelCase)

  }
}
