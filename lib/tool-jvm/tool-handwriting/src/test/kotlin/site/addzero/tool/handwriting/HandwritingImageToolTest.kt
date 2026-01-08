package site.addzero.tool.handwriting

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.nio.file.Files

class HandwritingImageToolTest {

    @Test
    fun `render is deterministic when seed fixed`() {
        val text = "汉字手写体演示"
        val options = HandwritingRenderOptions(randomSeed = 42L)
        val pngA = HandwritingImageTool.encode(text, options = options)
        val pngB = HandwritingImageTool.encode(text, options = options)
        assertArrayEquals(pngA, pngB)
    }

    @Test
    fun `write to file produces png`() {
        val output = Files.createTempFile("handwriting", ".png")
        val options = HandwritingRenderOptions(randomSeed = 7L)
        HandwritingImageTool.writeToFile("封崇盛", output, options = options)
        println("手写体测试输出: $output")
        assertTrue(Files.size(output) > 0)
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun headless() {
            System.setProperty("java.awt.headless", "true")
        }
    }
}
