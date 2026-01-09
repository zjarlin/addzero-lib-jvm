package site.addzero.tool.handwriting

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.max

/**
 * Utility that renders a single Hanzi (or the first character in a string) into an avatar image.
 */
object HanziAvatarTool {

    @JvmStatic
    @JvmOverloads
    fun render(
        text: String,
        options: HanziAvatarOptions = HanziAvatarOptions()
    ): BufferedImage {
        val normalized = text.trim()
        require(normalized.isNotEmpty()) { "text must not be blank." }
        val hanzi = normalized.first().toString()
        val size = options.size.coerceIn(32, 2048)
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        configure(graphics)

        val backgroundColor = options.backgroundColor ?: pickColor(hanzi, options)
        drawBackground(graphics, size, backgroundColor, options)

        val font = options.fontSource.resolve(max((size * options.fontSizeRatio).toInt(), 12))
        graphics.font = font
        graphics.color = options.textColor ?: pickTextColor(backgroundColor)
        val metrics = graphics.fontMetrics
        val x = (size - metrics.stringWidth(hanzi)) / 2
        val y = (size - metrics.height) / 2 + metrics.ascent
        graphics.drawString(hanzi, x, y)
        graphics.dispose()
        return image
    }

    @JvmStatic
    @JvmOverloads
    fun writeToFile(
        text: String,
        output: Path,
        format: String = "png",
        options: HanziAvatarOptions = HanziAvatarOptions()
    ): Path {
        require(format.isNotBlank()) { "format must not be blank." }
        val image = render(text, options)
        output.parent?.let { Files.createDirectories(it) }
        ImageIO.write(image, format, output.toFile())
        return output
    }

    @JvmStatic
    @JvmOverloads
    fun encode(
        text: String,
        format: String = "png",
        options: HanziAvatarOptions = HanziAvatarOptions()
    ): ByteArray {
        require(format.isNotBlank()) { "format must not be blank." }
        val image = render(text, options)
        val out = ByteArrayOutputStream()
        out.use {
            ImageIO.write(image, format, it)
        }
        return out.toByteArray()
    }

    private fun configure(graphics: Graphics2D) {
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    }

    private fun drawBackground(
        graphics: Graphics2D,
        size: Int,
        color: Color,
        options: HanziAvatarOptions
    ) {
        graphics.color = color
        if (options.circle) {
            val ellipse = Ellipse2D.Double(0.0, 0.0, size.toDouble(), size.toDouble())
            graphics.fill(ellipse)
            if (options.borderWidth > 0) {
                graphics.color = options.borderColor
                graphics.stroke = BasicStroke(options.borderWidth.toFloat())
                graphics.draw(ellipse)
            }
        } else {
            graphics.fillRect(0, 0, size, size)
            if (options.borderWidth > 0) {
                graphics.color = options.borderColor
                graphics.stroke = BasicStroke(options.borderWidth.toFloat())
                graphics.drawRect(0, 0, size - 1, size - 1)
            }
        }
    }

    private fun pickColor(hanzi: String, options: HanziAvatarOptions): Color {
        val palette = options.palette.ifEmpty { HanziAvatarOptions.DEFAULT_COLORS }
        val index = abs(hanzi.hashCode()) % palette.size
        return palette[index]
    }

    private fun pickTextColor(background: Color): Color {
        val luminance = (0.299 * background.red + 0.587 * background.green + 0.114 * background.blue) / 255.0
        return if (luminance > 0.6) {
            Color(0x20, 0x1F, 0x1E)
        } else {
            Color(0xFF, 0xFF, 0xFF)
        }
    }
}

data class HanziAvatarOptions(
    val size: Int = 320,
    val backgroundColor: Color? = null,
    val palette: List<Color> = DEFAULT_COLORS,
    val textColor: Color? = null,
    val circle: Boolean = true,
    val borderWidth: Int = 4,
    val borderColor: Color = Color(255, 255, 255, 150),
    val fontSource: HandwritingFontSource = HandwritingFontSource.systemDefault(),
    val fontSizeRatio: Double = 0.66
) {
    init {
        require(size > 0) { "size must be positive." }
        require(fontSizeRatio in 0.2..0.9) { "fontSizeRatio must be between 0.2 and 0.9" }
        require(borderWidth >= 0) { "borderWidth must be >= 0" }
    }

    companion object {
        val DEFAULT_COLORS: List<Color> = listOf(
            Color(0x4C, 0xAF, 0x50),
            Color(0x1E, 0x88, 0xE5),
            Color(0xFF, 0xA0, 0x00),
            Color(0xE5, 0x39, 0x35),
            Color(0x8E, 0x24, 0xAA),
            Color(0x00, 0x96, 0x88)
        )
    }
}
