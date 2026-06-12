package site.addzero.core.network.json

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

/**
 * Compose [Color] 的 ARGB Int 序列化器。
 *
 * 默认输出 [Color.toArgb] 的有符号 Int，反序列化时兼容历史 `#RRGGBB`、
 * `#AARRGGBB` 和 `0xAARRGGBB` 字符串，方便旧主题 JSON 平滑迁移。
 */
object ComposeColorArgbIntSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("androidx.compose.ui.graphics.ColorArgbInt", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Color) {
        encoder.encodeInt(value.toArgb())
    }

    override fun deserialize(decoder: Decoder): Color {
        if (decoder is JsonDecoder) {
            val primitive = decoder.decodeJsonElement() as? JsonPrimitive
                ?: throw SerializationException("Color 必须是 ARGB Int 或十六进制字符串")
            val intValue = primitive.intOrNull
                ?: primitive.longOrNull?.toInt()
                ?: primitive.contentOrNull?.toColorArgbInt()
                ?: throw SerializationException("Color 必须是 ARGB Int 或十六进制字符串")
            return Color(intValue)
        }
        return Color(decoder.decodeInt())
    }
}

/**
 * Material3 [ColorScheme] 的 ARGB Int 对象序列化器。
 *
 * 每个 Material 颜色角色都独立存成 Int，避免业务模型再维护一套字符串 token。
 */
object MaterialColorSchemeArgbIntSerializer : KSerializer<ColorScheme> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(
        "androidx.compose.material3.ColorSchemeArgbInt",
    ) {
        ColorSchemeRoles.forEach { role ->
            element<Int>(role.name)
        }
    }

    override fun serialize(encoder: Encoder, value: ColorScheme) {
        encoder.encodeStructure(descriptor) {
            ColorSchemeRoles.forEachIndexed { index, role ->
                encodeSerializableElement(
                    descriptor = descriptor,
                    index = index,
                    serializer = ComposeColorArgbIntSerializer,
                    value = role.read(value),
                )
            }
        }
    }

    override fun deserialize(decoder: Decoder): ColorScheme {
        val colors = arrayOfNulls<Color>(ColorSchemeRoles.size)
        decoder.decodeStructure(descriptor) {
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break
                    in ColorSchemeRoles.indices -> colors[index] = decodeSerializableElement(
                        descriptor = descriptor,
                        index = index,
                        deserializer = ComposeColorArgbIntSerializer,
                    )
                    else -> throw SerializationException("未知 ColorScheme 字段索引：$index")
                }
            }
        }
        fun color(index: Int): Color = colors[index]
            ?: throw SerializationException("ColorScheme 缺少颜色字段：${ColorSchemeRoles[index].name}")
        return ColorScheme(
            primary = color(0),
            onPrimary = color(1),
            primaryContainer = color(2),
            onPrimaryContainer = color(3),
            inversePrimary = color(4),
            secondary = color(5),
            onSecondary = color(6),
            secondaryContainer = color(7),
            onSecondaryContainer = color(8),
            tertiary = color(9),
            onTertiary = color(10),
            tertiaryContainer = color(11),
            onTertiaryContainer = color(12),
            background = color(13),
            onBackground = color(14),
            surface = color(15),
            onSurface = color(16),
            surfaceVariant = color(17),
            onSurfaceVariant = color(18),
            surfaceTint = color(19),
            inverseSurface = color(20),
            inverseOnSurface = color(21),
            error = color(22),
            onError = color(23),
            errorContainer = color(24),
            onErrorContainer = color(25),
            outline = color(26),
            outlineVariant = color(27),
            scrim = color(28),
            surfaceBright = color(29),
            surfaceDim = color(30),
            surfaceContainer = color(31),
            surfaceContainerHigh = color(32),
            surfaceContainerHighest = color(33),
            surfaceContainerLow = color(34),
            surfaceContainerLowest = color(35),
            primaryFixed = color(36),
            primaryFixedDim = color(37),
            onPrimaryFixed = color(38),
            onPrimaryFixedVariant = color(39),
            secondaryFixed = color(40),
            secondaryFixedDim = color(41),
            onSecondaryFixed = color(42),
            onSecondaryFixedVariant = color(43),
            tertiaryFixed = color(44),
            tertiaryFixedDim = color(45),
            onTertiaryFixed = color(46),
            onTertiaryFixedVariant = color(47),
        )
    }
}

private data class ColorSchemeRole(
    val name: String,
    val read: (ColorScheme) -> Color,
)

private val ColorSchemeRoles = listOf(
    ColorSchemeRole("primary", ColorScheme::primary),
    ColorSchemeRole("onPrimary", ColorScheme::onPrimary),
    ColorSchemeRole("primaryContainer", ColorScheme::primaryContainer),
    ColorSchemeRole("onPrimaryContainer", ColorScheme::onPrimaryContainer),
    ColorSchemeRole("inversePrimary", ColorScheme::inversePrimary),
    ColorSchemeRole("secondary", ColorScheme::secondary),
    ColorSchemeRole("onSecondary", ColorScheme::onSecondary),
    ColorSchemeRole("secondaryContainer", ColorScheme::secondaryContainer),
    ColorSchemeRole("onSecondaryContainer", ColorScheme::onSecondaryContainer),
    ColorSchemeRole("tertiary", ColorScheme::tertiary),
    ColorSchemeRole("onTertiary", ColorScheme::onTertiary),
    ColorSchemeRole("tertiaryContainer", ColorScheme::tertiaryContainer),
    ColorSchemeRole("onTertiaryContainer", ColorScheme::onTertiaryContainer),
    ColorSchemeRole("background", ColorScheme::background),
    ColorSchemeRole("onBackground", ColorScheme::onBackground),
    ColorSchemeRole("surface", ColorScheme::surface),
    ColorSchemeRole("onSurface", ColorScheme::onSurface),
    ColorSchemeRole("surfaceVariant", ColorScheme::surfaceVariant),
    ColorSchemeRole("onSurfaceVariant", ColorScheme::onSurfaceVariant),
    ColorSchemeRole("surfaceTint", ColorScheme::surfaceTint),
    ColorSchemeRole("inverseSurface", ColorScheme::inverseSurface),
    ColorSchemeRole("inverseOnSurface", ColorScheme::inverseOnSurface),
    ColorSchemeRole("error", ColorScheme::error),
    ColorSchemeRole("onError", ColorScheme::onError),
    ColorSchemeRole("errorContainer", ColorScheme::errorContainer),
    ColorSchemeRole("onErrorContainer", ColorScheme::onErrorContainer),
    ColorSchemeRole("outline", ColorScheme::outline),
    ColorSchemeRole("outlineVariant", ColorScheme::outlineVariant),
    ColorSchemeRole("scrim", ColorScheme::scrim),
    ColorSchemeRole("surfaceBright", ColorScheme::surfaceBright),
    ColorSchemeRole("surfaceDim", ColorScheme::surfaceDim),
    ColorSchemeRole("surfaceContainer", ColorScheme::surfaceContainer),
    ColorSchemeRole("surfaceContainerHigh", ColorScheme::surfaceContainerHigh),
    ColorSchemeRole("surfaceContainerHighest", ColorScheme::surfaceContainerHighest),
    ColorSchemeRole("surfaceContainerLow", ColorScheme::surfaceContainerLow),
    ColorSchemeRole("surfaceContainerLowest", ColorScheme::surfaceContainerLowest),
    ColorSchemeRole("primaryFixed", ColorScheme::primaryFixed),
    ColorSchemeRole("primaryFixedDim", ColorScheme::primaryFixedDim),
    ColorSchemeRole("onPrimaryFixed", ColorScheme::onPrimaryFixed),
    ColorSchemeRole("onPrimaryFixedVariant", ColorScheme::onPrimaryFixedVariant),
    ColorSchemeRole("secondaryFixed", ColorScheme::secondaryFixed),
    ColorSchemeRole("secondaryFixedDim", ColorScheme::secondaryFixedDim),
    ColorSchemeRole("onSecondaryFixed", ColorScheme::onSecondaryFixed),
    ColorSchemeRole("onSecondaryFixedVariant", ColorScheme::onSecondaryFixedVariant),
    ColorSchemeRole("tertiaryFixed", ColorScheme::tertiaryFixed),
    ColorSchemeRole("tertiaryFixedDim", ColorScheme::tertiaryFixedDim),
    ColorSchemeRole("onTertiaryFixed", ColorScheme::onTertiaryFixed),
    ColorSchemeRole("onTertiaryFixedVariant", ColorScheme::onTertiaryFixedVariant),
)

private fun String.toColorArgbInt(): Int {
    val value = trim()
    val hex = when {
        value.startsWith("#") -> value.removePrefix("#")
        value.startsWith("0x") -> value.removePrefix("0x")
        value.startsWith("0X") -> value.removePrefix("0X")
        else -> return value.toIntOrNull()
            ?: throw SerializationException("Color 字符串必须是 ARGB Int 或十六进制格式：$this")
    }
    val argb = when (hex.length) {
        6 -> "FF$hex"
        8 -> hex
        else -> throw SerializationException("Color 十六进制必须是 RRGGBB 或 AARRGGBB：$this")
    }
    return argb.toLong(16).toInt()
}
