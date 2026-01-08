package site.addzero.tool.handwriting

import java.awt.AlphaComposite
import java.awt.BasicStroke
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import java.awt.geom.FlatteningPathIterator
import java.awt.geom.GeneralPath
import java.awt.geom.PathIterator
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.Random
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Facade that turns Chinese text into handwriting-style images.
 */
object HandwritingImageTool {

    @JvmStatic
    @JvmOverloads
    fun render(text: String, options: HandwritingRenderOptions = HandwritingRenderOptions()): BufferedImage {
        val normalized = text.replace("\r\n", "\n")
        val font = options.fontSource.resolve(options.fontSize)
        val metrics = obtainFontMetrics(font)
        val jitterRandom = options.jitterRandom()
        val layout = layoutText(normalized, metrics, options, jitterRandom)

        val width = layout.width + options.margin.left + options.margin.right
        val height = layout.height + options.margin.top + options.margin.bottom
        val canvas = BufferedImage(width.coerceAtLeast(1), height.coerceAtLeast(1), BufferedImage.TYPE_INT_ARGB)
        val graphics = canvas.createGraphics()
        configure(graphics)
        options.background.paint(graphics, canvas.width, canvas.height, options.textureRandom())
        graphics.font = font
        graphics.color = options.textColor
        val strokeRandom = if (options.strokeStyle.enabled) options.strokeRandom() else null
        drawGlyphs(graphics, layout.glyphs, options, strokeRandom)
        graphics.dispose()
        return canvas
    }

    @JvmStatic
    @JvmOverloads
    fun writeToFile(
        text: String,
        output: Path,
        format: String = "png",
        options: HandwritingRenderOptions = HandwritingRenderOptions()
    ): Path {
        require(format.isNotBlank()) { "format must not be blank." }
        val image = render(text, options)
        output.parent?.let { Files.createDirectories(it) }
        val success = ImageIO.write(image, format, output.toFile())
        check(success) { "ImageIO could not find a writer for format: $format" }
        return output
    }

    @JvmStatic
    @JvmOverloads
    fun encode(
        text: String,
        format: String = "png",
        options: HandwritingRenderOptions = HandwritingRenderOptions()
    ): ByteArray {
        require(format.isNotBlank()) { "format must not be blank." }
        val image = render(text, options)
        val buffer = ByteArrayOutputStream()
        buffer.use {
            val success = ImageIO.write(image, format, it)
            check(success) { "ImageIO could not find a writer for format: $format" }
        }
        return buffer.toByteArray()
    }

    private fun drawGlyphs(
        graphics: Graphics2D,
        glyphs: List<Glyph>,
        options: HandwritingRenderOptions,
        strokeRandom: Random?
    ) {
        val ascent = graphics.fontMetrics.ascent.toDouble()
        val fontRenderContext = graphics.fontRenderContext
        val strokeStyle = options.strokeStyle
        glyphs.forEach { glyph ->
            val drawX = glyph.x + options.margin.left
            val drawY = glyph.baseline + options.margin.top
            if (strokeRandom != null && strokeStyle.enabled) {
                drawHandwritingOutline(
                    graphics = graphics,
                    glyph = glyph,
                    drawX = drawX,
                    drawY = drawY,
                    options = options,
                    random = strokeRandom,
                    fontRenderContext = fontRenderContext
                )
            } else {
                if (glyph.rotationDegrees != 0.0) {
                    val original = graphics.transform
                    val centerX = drawX + glyph.width / 2.0
                    val centerY = drawY - ascent / 2.0
                    graphics.rotate(Math.toRadians(glyph.rotationDegrees), centerX, centerY)
                    graphics.drawString(glyph.text, drawX.toFloat(), drawY.toFloat())
                    graphics.transform = original
                } else {
                    graphics.drawString(glyph.text, drawX.toFloat(), drawY.toFloat())
                }
            }
        }
    }

    private fun layoutText(
        text: String,
        metrics: java.awt.FontMetrics,
        options: HandwritingRenderOptions,
        random: Random
    ): LayoutResult {
        val glyphs = mutableListOf<Glyph>()
        val indentWidth = if (options.indentFirstLine && options.indentChars > 0) {
            metrics.stringWidth("　") * options.indentChars
        } else {
            0
        }
        val minimumWidth = metrics.stringWidth("国").coerceAtLeast(metrics.height)
        val wrapLimit = max(options.maxContentWidth, indentWidth + minimumWidth).toDouble()
        var baseline = 0.0
        var x = 0.0
        var needsIndent = options.indentFirstLine
        var maxWidth = 0.0
        var previousBreakExplicit = false

        fun newLine(extraSpacing: Int, indent: Boolean) {
            maxWidth = max(maxWidth, x)
            x = 0.0
            baseline += metrics.height + extraSpacing
            needsIndent = indent
        }

        val iterator = text.codePoints().iterator()
        while (iterator.hasNext()) {
            val codePoint = iterator.nextInt()
            when (codePoint) {
                '\r'.code -> continue
                '\n'.code -> {
                    val spacing = if (previousBreakExplicit) {
                        max(options.paragraphSpacing, options.lineSpacing)
                    } else {
                        options.lineSpacing
                    }
                    newLine(spacing, options.indentFirstLine)
                    previousBreakExplicit = true
                }

                '\t'.code -> {
                    x += metrics.charWidth(' ') * 4
                    previousBreakExplicit = false
                }

                else -> {
                    val glyph = String(Character.toChars(codePoint))
                    val glyphWidth = metrics.stringWidth(glyph).takeIf { it > 0 } ?: metrics.height
                    if (x > 0 && x + glyphWidth > wrapLimit) {
                        newLine(options.lineSpacing, false)
                    }
                    if (x == 0.0 && needsIndent) {
                        x += indentWidth
                        needsIndent = false
                    }
                    val jitterX = random.nextIntRange(options.horizontalJitter)
                    val jitterY = random.nextIntRange(options.baselineJitter)
                    val rotation = random.nextDoubleRange(options.rotationJitter)
                    val baselineY = baseline + metrics.ascent + jitterY
                    glyphs += Glyph(
                        text = glyph,
                        x = x + jitterX,
                        baseline = baselineY,
                        width = glyphWidth.toDouble(),
                        rotationDegrees = rotation
                    )
                    x += glyphWidth + options.charSpacing
                    maxWidth = max(maxWidth, x)
                    previousBreakExplicit = false
                }
            }
        }

        maxWidth = max(maxWidth, x)
        val contentHeight = (baseline + metrics.height).roundToInt().coerceAtLeast(metrics.height)
        val contentWidth = max(maxWidth.roundToInt(), minimumWidth)
        return LayoutResult(contentWidth, contentHeight, glyphs)
    }

    private fun configure(graphics: Graphics2D) {
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    }

    private fun obtainFontMetrics(font: java.awt.Font): java.awt.FontMetrics {
        val scratch = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
        val g2d = scratch.createGraphics()
        g2d.font = font
        val metrics = g2d.fontMetrics
        g2d.dispose()
        return metrics
    }

    private data class Glyph(
        val text: String,
        val x: Double,
        val baseline: Double,
        val width: Double,
        val rotationDegrees: Double
    )

    private data class LayoutResult(
        val width: Int,
        val height: Int,
        val glyphs: List<Glyph>
    )

    private fun drawHandwritingOutline(
        graphics: Graphics2D,
        glyph: Glyph,
        drawX: Double,
        drawY: Double,
        options: HandwritingRenderOptions,
        random: Random,
        fontRenderContext: FontRenderContext
    ) {
        val font = graphics.font
        val glyphVector = font.createGlyphVector(fontRenderContext, glyph.text)
        var outline: Shape = glyphVector.getOutline(drawX.toFloat(), drawY.toFloat())
        if (glyph.rotationDegrees != 0.0) {
            val bounds = outline.bounds2D
            val transform = AffineTransform()
            transform.rotate(Math.toRadians(glyph.rotationDegrees), bounds.centerX, bounds.centerY)
            outline = transform.createTransformedShape(outline)
        }
        val jittered = jitterShape(outline, options, random)
        val originalComposite = graphics.composite
        val originalStroke = graphics.stroke
        val style = options.strokeStyle
        val fillAlpha = style.fillAlpha.coerceIn(0f, 1f)
        if (fillAlpha > 0f) {
            graphics.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fillAlpha)
            graphics.fill(jittered)
        } else {
            graphics.fill(jittered)
        }
        val strokeAlpha = style.strokeAlpha.coerceIn(0f, 1f)
        if (strokeAlpha > 0f) {
            val strokeWidth = random.nextDoubleRange(style.strokeWidthRange).toFloat()
            graphics.stroke = BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            graphics.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, strokeAlpha)
            graphics.draw(jittered)
        }
        graphics.composite = originalComposite
        graphics.stroke = originalStroke
    }

    private fun jitterShape(
        outline: Shape,
        options: HandwritingRenderOptions,
        random: Random
    ): Shape {
        val style = options.strokeStyle
        val flattening = style.flattening
        val amplitudeBase = style.outlineJitter
        if (amplitudeBase <= 0 || flattening <= 0) {
            return outline
        }
        val iterator = FlatteningPathIterator(outline.getPathIterator(null), flattening)
        val coords = DoubleArray(6)
        val result = GeneralPath(GeneralPath.WIND_EVEN_ODD)
        val fontScale = (options.fontSize.coerceAtLeast(24) / 48.0).coerceAtLeast(0.3)
        val amplitude = amplitudeBase * fontScale
        fun jitter(value: Double): Float {
            val noise = random.nextGaussian() * amplitude
            return (value + noise).toFloat()
        }
        while (!iterator.isDone) {
            when (iterator.currentSegment(coords)) {
                PathIterator.SEG_MOVETO -> result.moveTo(jitter(coords[0]), jitter(coords[1]))
                PathIterator.SEG_LINETO -> result.lineTo(jitter(coords[0]), jitter(coords[1]))
                PathIterator.SEG_QUADTO -> {
                    result.quadTo(
                        jitter(coords[0]),
                        jitter(coords[1]),
                        jitter(coords[2]),
                        jitter(coords[3])
                    )
                }

                PathIterator.SEG_CUBICTO -> {
                    result.curveTo(
                        jitter(coords[0]),
                        jitter(coords[1]),
                        jitter(coords[2]),
                        jitter(coords[3]),
                        jitter(coords[4]),
                        jitter(coords[5])
                    )
                }

                PathIterator.SEG_CLOSE -> result.closePath()
            }
            iterator.next()
        }
        return result
    }
}
