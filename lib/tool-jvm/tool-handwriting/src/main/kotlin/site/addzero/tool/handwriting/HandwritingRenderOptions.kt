package site.addzero.tool.handwriting

import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Font
import java.awt.GradientPaint
import java.awt.Graphics2D
import java.awt.GraphicsEnvironment
import java.awt.Insets
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.Random
import kotlin.math.max
import kotlin.math.min

/**
 * Immutable configuration describing how handwriting images should be rendered.
 */
data class HandwritingRenderOptions(
    val fontSource: HandwritingFontSource = HandwritingFontSource.systemDefault(),
    val fontSize: Int = 48,
    val maxContentWidth: Int = 960,
    val margin: Insets = Insets(48, 72, 56, 72),
    val lineSpacing: Int = 12,
    val paragraphSpacing: Int = 32,
    val charSpacing: Int = 6,
    val baselineJitter: IntRange = -3..3,
    val horizontalJitter: IntRange = -2..2,
    val rotationJitter: ClosedFloatingPointRange<Double> = -2.2..2.2,
    val randomSeed: Long? = null,
    val textColor: Color = Color(0x34, 0x2F, 0x2B),
    val background: HandwritingPaperTexture = HandwritingPaperTexture(),
    val indentFirstLine: Boolean = true,
    val indentChars: Int = 2,
    val strokeStyle: HandwritingStrokeStyle = HandwritingStrokeStyle()
) {
    init {
        require(fontSize > 0) { "fontSize must be positive." }
        require(maxContentWidth > 0) { "maxContentWidth must be positive." }
        require(lineSpacing >= 0) { "lineSpacing cannot be negative." }
        require(paragraphSpacing >= 0) { "paragraphSpacing cannot be negative." }
        require(charSpacing >= 0) { "charSpacing cannot be negative." }
        require(indentChars >= 0) { "indentChars cannot be negative." }
        require(baselineJitter.first <= baselineJitter.last) { "baselineJitter is invalid." }
        require(horizontalJitter.first <= horizontalJitter.last) { "horizontalJitter is invalid." }
        require(rotationJitter.start <= rotationJitter.endInclusive) { "rotationJitter is invalid." }
        require(margin.top >= 0 && margin.left >= 0 && margin.bottom >= 0 && margin.right >= 0) {
            "All margins must be non-negative."
        }
    }

    internal fun jitterRandom(): Random {
        val baseSeed = randomSeed ?: System.nanoTime()
        return Random(baseSeed)
    }

    internal fun textureRandom(): Random {
        val baseSeed = randomSeed ?: System.nanoTime()
        return Random(baseSeed xor 0x61C8_8646_80B5_83EBL)
    }

    internal fun strokeRandom(): Random {
        val baseSeed = randomSeed ?: System.nanoTime()
        return Random(baseSeed xor 0x3C4F_57A2_FBCB_A473L)
    }
}

/**
 * Controls how the virtual paper looks (background tint, guidelines, subtle noise).
 */
data class HandwritingPaperTexture(
    val backgroundColor: Color = Color(0xFD, 0xFB, 0xF6),
    val gradientColor: Color = Color(0xF2, 0xEC, 0xDE),
    val gradientStrength: Float = 0.35f,
    val noiseColor: Color = Color(0, 0, 0, 25),
    val noiseDensity: Double = 0.015,
    val guidelineColor: Color? = Color(0xE7, 0xD9, 0xC2),
    val guidelineSpacing: Int = 140,
    val edgeVignetteStrength: Float = 0.08f
) {
    init {
        require(noiseDensity >= 0.0) { "noiseDensity must be >= 0." }
        require(guidelineSpacing > 0 || guidelineColor == null) { "guidelineSpacing must be > 0 when guidelines are enabled." }
        require(gradientStrength in 0f..1f) { "gradientStrength must be inside 0..1." }
        require(edgeVignetteStrength in 0f..1f) { "edgeVignetteStrength must be inside 0..1." }
    }

    fun paint(g: Graphics2D, width: Int, height: Int, random: Random) {
        g.color = backgroundColor
        g.fillRect(0, 0, width, height)

        if (gradientStrength > 0f) {
            val gradient = GradientPaint(
                0f,
                0f,
                gradientColor,
                width.toFloat(),
                height.toFloat(),
                backgroundColor,
                true
            )
            val alpha = gradientStrength.coerceIn(0f, 1f)
            val originalComposite = g.composite
            g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
            val previousPaint = g.paint
            g.paint = gradient
            g.fillRect(0, 0, width, height)
            g.paint = previousPaint
            g.composite = originalComposite
        }

        if (guidelineColor != null) {
            val originalStroke = g.stroke
            g.color = guidelineColor
            var y = guidelineSpacing / 2
            while (y < height) {
                g.drawLine(0, y, width, y)
                y += guidelineSpacing
            }
            g.stroke = originalStroke
        }

        if (noiseDensity > 0.0 && noiseColor.alpha > 0) {
            val points = (width * height * noiseDensity).toInt().coerceAtLeast(width + height)
            g.color = noiseColor
            repeat(points) {
                val x = random.nextInt(width)
                val y = random.nextInt(height)
                g.fillRect(x, y, 1, 1)
            }
        }

        if (edgeVignetteStrength > 0f) {
            val alpha = edgeVignetteStrength.coerceIn(0f, 1f)
            val originalComposite = g.composite
            g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
            val vignetteColor = Color(0, 0, 0, (35 * alpha).toInt().coerceAtLeast(5))
            g.color = vignetteColor
            g.drawRect(0, 0, width - 1, height - 1)
            g.composite = originalComposite
        }
    }
}

/**
 * Adds pseudo-handwritten stroke rendering on top of standard fonts.
 */
data class HandwritingStrokeStyle(
    val enabled: Boolean = true,
    val outlineJitter: Double = 0.9,
    val flattening: Double = 0.35,
    val strokeWidthRange: ClosedFloatingPointRange<Double> = 1.4..2.8,
    val fillAlpha: Float = 0.94f,
    val strokeAlpha: Float = 0.78f
) {
    init {
        require(outlineJitter >= 0) { "outlineJitter must be >= 0." }
        require(flattening > 0) { "flattening must be > 0." }
        require(strokeWidthRange.start > 0 && strokeWidthRange.endInclusive > 0) { "strokeWidthRange must be positive." }
        require(fillAlpha in 0f..1f) { "fillAlpha must be in 0..1." }
        require(strokeAlpha in 0f..1f) { "strokeAlpha must be in 0..1." }
    }
}

/**
 * Provides handwriting fonts from embedded bytes, files, or system fonts.
 */
class HandwritingFontSource private constructor(
    private val fontBytes: ByteArray?,
    private val fontFormat: Int,
    private val familyCandidates: List<String>,
    private val explicitFont: Font?
) {
    fun resolve(pointSize: Int): Font {
        explicitFont?.let {
            return it.deriveFont(pointSize.toFloat())
        }
        fontBytes?.let {
            val stream = ByteArrayInputStream(it)
            val created = runCatching { Font.createFont(fontFormat, stream) }.getOrNull()
            if (created != null) {
                return created.deriveFont(pointSize.toFloat())
            }
        }
        val candidate = selectAvailableFamily()
        if (candidate != null) {
            return Font(candidate, Font.PLAIN, pointSize)
        }
        return Font(Font.SERIF, Font.PLAIN, pointSize)
    }

    private fun selectAvailableFamily(): String? {
        val families = runCatching {
            GraphicsEnvironment.getLocalGraphicsEnvironment()
                .availableFontFamilyNames
                .toSet()
        }.getOrElse { emptySet() }
        for (candidate in familyCandidates) {
            if (families.any { it.equals(candidate, ignoreCase = true) }) {
                return candidate
            }
        }
        return null
    }

    companion object {
        private val DEFAULT_FAMILIES = listOf(
            "HYXueJunJ",
            "HYXueJunTi",
            "FZXiJinLJW",
            "KaiTi",
            "STKaiti",
            "DFKai-SB",
            "FZKTJW"
        )

        fun systemDefault(candidates: List<String> = DEFAULT_FAMILIES): HandwritingFontSource {
            return HandwritingFontSource(null, Font.TRUETYPE_FONT, candidates, null)
        }

        fun fromFont(font: Font): HandwritingFontSource {
            return HandwritingFontSource(null, Font.TRUETYPE_FONT, emptyList(), font)
        }

        fun fromPath(path: Path, format: Int = Font.TRUETYPE_FONT): HandwritingFontSource {
            val bytes = Files.readAllBytes(path)
            return fromBytes(bytes, format)
        }

        fun fromInputStream(stream: InputStream, format: Int = Font.TRUETYPE_FONT): HandwritingFontSource {
            val bytes = stream.use { it.readBytes() }
            return fromBytes(bytes, format)
        }

        fun fromBytes(bytes: ByteArray, format: Int = Font.TRUETYPE_FONT): HandwritingFontSource {
            return HandwritingFontSource(bytes.copyOf(), format, emptyList(), null)
        }
    }
}

internal fun Random.nextIntRange(range: IntRange): Double {
    if (range.isEmpty()) {
        return range.first.toDouble()
    }
    val span = range.last - range.first
    if (span == 0) {
        return range.first.toDouble()
    }
    return range.first + nextInt(span + 1).toDouble()
}

internal fun Random.nextDoubleRange(range: ClosedFloatingPointRange<Double>): Double {
    if (range.start == range.endInclusive) {
        return range.start
    }
    val start = min(range.start, range.endInclusive)
    val end = max(range.start, range.endInclusive)
    return start + nextDouble() * (end - start)
}
