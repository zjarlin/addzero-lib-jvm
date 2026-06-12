package site.addzero.core.network.json

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull

/**
 * Compose [Dp] 的配置序列化器。
 *
 * 默认输出数值，反序列化兼容 `"12.dp"`、`"hairline"`、`"infinity"` 和 `"unspecified"`。
 */
object ComposeDpSerializer : KSerializer<Dp> {
    override val descriptor: SerialDescriptor =
        kotlinx.serialization.descriptors.PrimitiveSerialDescriptor(
            "androidx.compose.ui.unit.DpValue",
            kotlinx.serialization.descriptors.PrimitiveKind.FLOAT,
        )

    override fun serialize(encoder: Encoder, value: Dp) {
        encoder.encodeFloat(value.value)
    }

    override fun deserialize(decoder: Decoder): Dp {
        if (decoder is JsonDecoder) {
            val primitive = decoder.decodeJsonElement() as? JsonPrimitive
                ?: throw SerializationException("Dp 必须是数值或字符串")
            primitive.floatOrNull?.let { return Dp(it) }
            return primitive.contentOrNull?.toComposeDp()
                ?: throw SerializationException("Dp 必须是数值或字符串")
        }
        return Dp(decoder.decodeFloat())
    }
}

@Serializable
data class ComposeTextUnitSurrogate(
    val unit: ComposeTextUnitKind = ComposeTextUnitKind.Unspecified,
    val value: Float? = null,
) {
    fun toTextUnit(): TextUnit =
        when (unit) {
            ComposeTextUnitKind.Unspecified -> TextUnit.Unspecified
            ComposeTextUnitKind.Sp -> TextUnit(value ?: throw SerializationException("sp TextUnit 缺少 value"), TextUnitType.Sp)
            ComposeTextUnitKind.Em -> TextUnit(value ?: throw SerializationException("em TextUnit 缺少 value"), TextUnitType.Em)
        }

    companion object {
        fun from(value: TextUnit): ComposeTextUnitSurrogate =
            when (value.type) {
                TextUnitType.Unspecified -> ComposeTextUnitSurrogate()
                TextUnitType.Sp -> ComposeTextUnitSurrogate(ComposeTextUnitKind.Sp, value.value)
                TextUnitType.Em -> ComposeTextUnitSurrogate(ComposeTextUnitKind.Em, value.value)
                else -> throw SerializationException("不支持的 TextUnit 类型：${value.type}")
            }
    }
}

@Serializable
enum class ComposeTextUnitKind {
    @SerialName("unspecified")
    Unspecified,

    @SerialName("sp")
    Sp,

    @SerialName("em")
    Em,
}

/**
 * Compose [TextUnit] 的配置序列化器。
 *
 * 输出 `{ "unit": "sp", "value": 14 }`，反序列化兼容 `"14.sp"`、`"1.2.em"` 和 `"unspecified"`。
 */
object ComposeTextUnitSerializer : KSerializer<TextUnit> {
    override val descriptor: SerialDescriptor = ComposeTextUnitSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: TextUnit) {
        encoder.encodeSerializableValue(ComposeTextUnitSurrogate.serializer(), ComposeTextUnitSurrogate.from(value))
    }

    override fun deserialize(decoder: Decoder): TextUnit {
        if (decoder is JsonDecoder) {
            val element = decoder.decodeJsonElement()
            if (element is JsonPrimitive) {
                return element.contentOrNull?.toComposeTextUnit()
                    ?: throw SerializationException("TextUnit 必须是对象或字符串")
            }
            return decoder.json.decodeFromJsonElement(ComposeTextUnitSurrogate.serializer(), element).toTextUnit()
        }
        return decoder.decodeSerializableValue(ComposeTextUnitSurrogate.serializer()).toTextUnit()
    }
}

@Serializable
data class ComposePaddingValuesSurrogate(
    val kind: ComposePaddingValuesKind = ComposePaddingValuesKind.Relative,
    @Serializable(with = ComposeDpSerializer::class)
    val start: Dp = 0.dp,
    @Serializable(with = ComposeDpSerializer::class)
    val top: Dp = 0.dp,
    @Serializable(with = ComposeDpSerializer::class)
    val end: Dp = 0.dp,
    @Serializable(with = ComposeDpSerializer::class)
    val bottom: Dp = 0.dp,
) {
    fun toPaddingValues(): PaddingValues =
        when (kind) {
            ComposePaddingValuesKind.Relative -> PaddingValues(start = start, top = top, end = end, bottom = bottom)
            ComposePaddingValuesKind.Absolute -> PaddingValues.Absolute(left = start, top = top, right = end, bottom = bottom)
        }

    companion object {
        fun relative(start: Dp = 0.dp, top: Dp = 0.dp, end: Dp = 0.dp, bottom: Dp = 0.dp): ComposePaddingValuesSurrogate =
            ComposePaddingValuesSurrogate(ComposePaddingValuesKind.Relative, start, top, end, bottom)

        fun absolute(left: Dp = 0.dp, top: Dp = 0.dp, right: Dp = 0.dp, bottom: Dp = 0.dp): ComposePaddingValuesSurrogate =
            ComposePaddingValuesSurrogate(ComposePaddingValuesKind.Absolute, left, top, right, bottom)

        fun from(value: PaddingValues): ComposePaddingValuesSurrogate {
            val ltrLeft = value.calculateLeftPadding(LayoutDirection.Ltr)
            val ltrRight = value.calculateRightPadding(LayoutDirection.Ltr)
            val rtlLeft = value.calculateLeftPadding(LayoutDirection.Rtl)
            val rtlRight = value.calculateRightPadding(LayoutDirection.Rtl)
            val top = value.calculateTopPadding()
            val bottom = value.calculateBottomPadding()
            return if (ltrLeft == rtlLeft && ltrRight == rtlRight) {
                absolute(left = ltrLeft, top = top, right = ltrRight, bottom = bottom)
            } else {
                relative(start = ltrLeft, top = top, end = ltrRight, bottom = bottom)
            }
        }
    }
}

@Serializable
enum class ComposePaddingValuesKind {
    @SerialName("relative")
    Relative,

    @SerialName("absolute")
    Absolute,
}

object ComposePaddingValuesSerializer : KSerializer<PaddingValues> {
    override val descriptor: SerialDescriptor = ComposePaddingValuesSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: PaddingValues) {
        encoder.encodeSerializableValue(ComposePaddingValuesSurrogate.serializer(), ComposePaddingValuesSurrogate.from(value))
    }

    override fun deserialize(decoder: Decoder): PaddingValues =
        decoder.decodeSerializableValue(ComposePaddingValuesSurrogate.serializer()).toPaddingValues()
}

@Serializable
data class ComposeBorderStrokeSurrogate(
    @Serializable(with = ComposeDpSerializer::class)
    val width: Dp,
    @Serializable(with = ComposeColorArgbIntSerializer::class)
    val color: Color,
) {
    fun toBorderStroke(): BorderStroke = BorderStroke(width, color)

    companion object {
        fun from(value: BorderStroke): ComposeBorderStrokeSurrogate =
            ComposeBorderStrokeSurrogate(
                width = value.width,
                color = value.brush.toSolidColorOrThrow(),
            )
    }
}

/**
 * Compose [BorderStroke] 的配置序列化器。
 *
 * 仅支持纯色边框；渐变和自定义 [Brush] 应保存业务 token，而不是尝试序列化运行时绘制对象。
 */
object ComposeBorderStrokeSerializer : KSerializer<BorderStroke> {
    override val descriptor: SerialDescriptor = ComposeBorderStrokeSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: BorderStroke) {
        encoder.encodeSerializableValue(ComposeBorderStrokeSurrogate.serializer(), ComposeBorderStrokeSurrogate.from(value))
    }

    override fun deserialize(decoder: Decoder): BorderStroke =
        decoder.decodeSerializableValue(ComposeBorderStrokeSurrogate.serializer()).toBorderStroke()
}

@Serializable
data class ComposeTextIndentSurrogate(
    @Serializable(with = ComposeTextUnitSerializer::class)
    val firstLine: TextUnit = TextUnit(0f, TextUnitType.Sp),
    @Serializable(with = ComposeTextUnitSerializer::class)
    val restLine: TextUnit = TextUnit(0f, TextUnitType.Sp),
) {
    fun toTextIndent(): TextIndent = TextIndent(firstLine = firstLine, restLine = restLine)

    companion object {
        fun from(value: TextIndent): ComposeTextIndentSurrogate =
            ComposeTextIndentSurrogate(firstLine = value.firstLine, restLine = value.restLine)
    }
}

@Serializable
data class ComposeTextStyleSurrogate(
    @Serializable(with = ComposeColorArgbIntSerializer::class)
    val color: Color = Color.Unspecified,
    @Serializable(with = ComposeTextUnitSerializer::class)
    val fontSize: TextUnit = TextUnit.Unspecified,
    val fontWeight: Int? = null,
    val fontStyle: ComposeFontStyle? = null,
    val fontSynthesis: ComposeFontSynthesis? = null,
    val fontFeatureSettings: String? = null,
    @Serializable(with = ComposeTextUnitSerializer::class)
    val letterSpacing: TextUnit = TextUnit.Unspecified,
    @Serializable(with = ComposeColorArgbIntSerializer::class)
    val background: Color = Color.Unspecified,
    val textDecorationMask: Int? = null,
    val textAlign: Int = TextAlign.Unspecified.value,
    val textDirection: Int = TextDirection.Unspecified.value,
    @Serializable(with = ComposeTextUnitSerializer::class)
    val lineHeight: TextUnit = TextUnit.Unspecified,
    val textIndent: ComposeTextIndentSurrogate? = null,
) {
    fun toTextStyle(): TextStyle =
        TextStyle(
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight?.let(::FontWeight),
            fontStyle = fontStyle?.toFontStyle(),
            fontSynthesis = fontSynthesis?.toFontSynthesis(),
            fontFeatureSettings = fontFeatureSettings,
            letterSpacing = letterSpacing,
            background = background,
            textDecoration = textDecorationMask?.let(TextDecoration::valueOf),
            textAlign = TextAlign.valueOf(textAlign),
            textDirection = TextDirection.valueOf(textDirection),
            lineHeight = lineHeight,
            textIndent = textIndent?.toTextIndent(),
        )

    companion object {
        fun from(value: TextStyle): ComposeTextStyleSurrogate =
            ComposeTextStyleSurrogate(
                color = value.color,
                fontSize = value.fontSize,
                fontWeight = value.fontWeight?.weight,
                fontStyle = value.fontStyle?.let(ComposeFontStyle::from),
                fontSynthesis = value.fontSynthesis?.let(ComposeFontSynthesis::from),
                fontFeatureSettings = value.fontFeatureSettings,
                letterSpacing = value.letterSpacing,
                background = value.background,
                textDecorationMask = value.textDecoration?.mask,
                textAlign = value.textAlign.value,
                textDirection = value.textDirection.value,
                lineHeight = value.lineHeight,
                textIndent = value.textIndent?.let(ComposeTextIndentSurrogate::from),
            )
    }
}

@Serializable
enum class ComposeFontStyle {
    @SerialName("normal")
    Normal,

    @SerialName("italic")
    Italic;

    fun toFontStyle(): FontStyle =
        when (this) {
            Normal -> FontStyle.Normal
            Italic -> FontStyle.Italic
        }

    companion object {
        fun from(value: FontStyle): ComposeFontStyle =
            when (value) {
                FontStyle.Normal -> Normal
                FontStyle.Italic -> Italic
                else -> throw SerializationException("不支持的 FontStyle：$value")
            }
    }
}

@Serializable
enum class ComposeFontSynthesis {
    @SerialName("none")
    None,

    @SerialName("weight")
    Weight,

    @SerialName("style")
    Style,

    @SerialName("all")
    All;

    fun toFontSynthesis(): FontSynthesis =
        when (this) {
            None -> FontSynthesis.None
            Weight -> FontSynthesis.Weight
            Style -> FontSynthesis.Style
            All -> FontSynthesis.All
        }

    companion object {
        fun from(value: FontSynthesis): ComposeFontSynthesis =
            when (value) {
                FontSynthesis.None -> None
                FontSynthesis.Weight -> Weight
                FontSynthesis.Style -> Style
                FontSynthesis.All -> All
                else -> throw SerializationException("不支持的 FontSynthesis：$value")
            }
    }
}

/**
 * Compose [TextStyle] 的配置序列化器。
 *
 * 只覆盖可稳定落库的文本样式子集；[androidx.compose.ui.text.font.FontFamily]、阴影、
 * draw style、平台样式和任意 brush 不在通用 JSON 边界内，应通过业务 token 或代码侧主题解析。
 */
object ComposeTextStyleSerializer : KSerializer<TextStyle> {
    override val descriptor: SerialDescriptor = ComposeTextStyleSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: TextStyle) {
        encoder.encodeSerializableValue(ComposeTextStyleSurrogate.serializer(), ComposeTextStyleSurrogate.from(value))
    }

    override fun deserialize(decoder: Decoder): TextStyle =
        decoder.decodeSerializableValue(ComposeTextStyleSurrogate.serializer()).toTextStyle()
}

object ComposeTextDecorationSerializer : KSerializer<TextDecoration> {
    override val descriptor: SerialDescriptor =
        kotlinx.serialization.descriptors.PrimitiveSerialDescriptor(
            "androidx.compose.ui.text.style.TextDecorationMask",
            kotlinx.serialization.descriptors.PrimitiveKind.INT,
        )

    override fun serialize(encoder: Encoder, value: TextDecoration) {
        encoder.encodeInt(value.mask)
    }

    override fun deserialize(decoder: Decoder): TextDecoration =
        TextDecoration.valueOf(decoder.decodeInt())
}

object ComposeFontWeightSerializer : KSerializer<FontWeight> {
    override val descriptor: SerialDescriptor =
        kotlinx.serialization.descriptors.PrimitiveSerialDescriptor(
            "androidx.compose.ui.text.font.FontWeightValue",
            kotlinx.serialization.descriptors.PrimitiveKind.INT,
        )

    override fun serialize(encoder: Encoder, value: FontWeight) {
        encoder.encodeInt(value.weight)
    }

    override fun deserialize(decoder: Decoder): FontWeight =
        FontWeight(decoder.decodeInt())
}

object ComposeTextAlignSerializer : KSerializer<TextAlign> {
    override val descriptor: SerialDescriptor =
        kotlinx.serialization.descriptors.PrimitiveSerialDescriptor(
            "androidx.compose.ui.text.style.TextAlignValue",
            kotlinx.serialization.descriptors.PrimitiveKind.INT,
        )

    override fun serialize(encoder: Encoder, value: TextAlign) {
        encoder.encodeInt(value.value)
    }

    override fun deserialize(decoder: Decoder): TextAlign =
        TextAlign.valueOf(decoder.decodeInt())
}

object ComposeTextDirectionSerializer : KSerializer<TextDirection> {
    override val descriptor: SerialDescriptor =
        kotlinx.serialization.descriptors.PrimitiveSerialDescriptor(
            "androidx.compose.ui.text.style.TextDirectionValue",
            kotlinx.serialization.descriptors.PrimitiveKind.INT,
        )

    override fun serialize(encoder: Encoder, value: TextDirection) {
        encoder.encodeInt(value.value)
    }

    override fun deserialize(decoder: Decoder): TextDirection =
        TextDirection.valueOf(decoder.decodeInt())
}

object ComposeTextIndentSerializer : KSerializer<TextIndent> {
    override val descriptor: SerialDescriptor = ComposeTextIndentSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: TextIndent) {
        encoder.encodeSerializableValue(ComposeTextIndentSurrogate.serializer(), ComposeTextIndentSurrogate.from(value))
    }

    override fun deserialize(decoder: Decoder): TextIndent =
        decoder.decodeSerializableValue(ComposeTextIndentSurrogate.serializer()).toTextIndent()
}

private fun String.toComposeDp(): Dp {
    val raw = trim().lowercase()
    return when (raw) {
        "hairline", "dp.hairline" -> Dp.Hairline
        "infinity", "dp.infinity" -> Dp.Infinity
        "unspecified", "dp.unspecified" -> Dp.Unspecified
        else -> {
            val value = raw.removeSuffix(".dp").toFloatOrNull()
                ?: throw SerializationException("Dp 字符串必须是数值、12.dp、hairline、infinity 或 unspecified：$this")
            Dp(value)
        }
    }
}

private fun String.toComposeTextUnit(): TextUnit {
    val raw = trim().lowercase()
    if (raw == "unspecified" || raw == "textunit.unspecified") {
        return TextUnit.Unspecified
    }
    return when {
        raw.endsWith(".sp") -> TextUnit(raw.removeSuffix(".sp").toFloatOrNullOrThrow(this), TextUnitType.Sp)
        raw.endsWith(".em") -> TextUnit(raw.removeSuffix(".em").toFloatOrNullOrThrow(this), TextUnitType.Em)
        else -> throw SerializationException("TextUnit 字符串必须是 14.sp、1.2.em 或 unspecified：$this")
    }
}

private fun String.toFloatOrNullOrThrow(source: String): Float =
    toFloatOrNull() ?: throw SerializationException("TextUnit 字符串数值非法：$source")

private fun Brush.toSolidColorOrThrow(): Color {
    if (this is SolidColor) {
        return value
    }
    throw SerializationException("BorderStroke 只支持纯色 SolidColor，当前 brush=$this")
}
