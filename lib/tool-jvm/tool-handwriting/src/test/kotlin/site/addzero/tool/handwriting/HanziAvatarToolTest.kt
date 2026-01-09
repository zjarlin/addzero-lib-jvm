package site.addzero.tool.handwriting

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.nio.file.Files

class HanziAvatarToolTest {

  @Test
  fun `render produces square avatar`() {
    val options = HanziAvatarOptions(size = 200)
    val image = HanziAvatarTool.render("零", options)
    assertEquals(200, image.width)
    assertEquals(200, image.height)
  }

  @Test
  fun `writeToFile creates avatar png`() {
    val output = Files.createTempFile("hanzi-avatar", ".png")
    HanziAvatarTool.writeToFile("zjarlin", output)
    assertTrue(Files.size(output) > 0)
    println("头像测试输出: $output")
  }

  companion object {
    @JvmStatic
    @BeforeAll
    fun enableHeadless() {
      System.setProperty("java.awt.headless", "true")
    }
  }
}
