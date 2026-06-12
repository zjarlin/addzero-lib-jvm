package site.addzero.core.network.json

import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.InspectableValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 可落 JSON 的 Compose 形状描述。
 *
 * [Shape] 是运行时接口，不能把任意实现稳定写进配置；这里仅覆盖主题配置常用的矩形、
 * 圆形、圆角矩形和绝对圆角矩形，其它自定义形状应保存自己的业务 token。
 */
@Serializable
data class ComposeShapeSurrogate(
    val kind: ComposeShapeKind = ComposeShapeKind.Rounded,
    val topStart: ComposeCornerSizeSurrogate = ComposeCornerSizeSurrogate.dp(0f),
    val topEnd: ComposeCornerSizeSurrogate = topStart,
    val bottomEnd: ComposeCornerSizeSurrogate = topStart,
    val bottomStart: ComposeCornerSizeSurrogate = topStart,
) {
    fun toShape(): Shape =
        when (kind) {
            ComposeShapeKind.Rectangle -> RectangleShape
            ComposeShapeKind.Circle -> CircleShape
            ComposeShapeKind.Rounded -> RoundedCornerShape(
                topStart = topStart.toCornerSize(),
                topEnd = topEnd.toCornerSize(),
                bottomEnd = bottomEnd.toCornerSize(),
                bottomStart = bottomStart.toCornerSize(),
            )
            ComposeShapeKind.AbsoluteRounded -> AbsoluteRoundedCornerShape(
                topLeft = topStart.toCornerSize(),
                topRight = topEnd.toCornerSize(),
                bottomRight = bottomEnd.toCornerSize(),
                bottomLeft = bottomStart.toCornerSize(),
            )
        }

    companion object {
        fun rectangle(): ComposeShapeSurrogate =
            ComposeShapeSurrogate(kind = ComposeShapeKind.Rectangle)

        fun circle(): ComposeShapeSurrogate =
            ComposeShapeSurrogate(kind = ComposeShapeKind.Circle)

        fun rounded(all: Float): ComposeShapeSurrogate =
            rounded(ComposeCornerSizeSurrogate.dp(all))

        fun rounded(all: Dp): ComposeShapeSurrogate =
            rounded(ComposeCornerSizeSurrogate.dp(all.value))

        fun rounded(all: ComposeCornerSizeSurrogate): ComposeShapeSurrogate =
            ComposeShapeSurrogate(kind = ComposeShapeKind.Rounded, topStart = all)

        fun rounded(
            topStart: ComposeCornerSizeSurrogate,
            topEnd: ComposeCornerSizeSurrogate,
            bottomEnd: ComposeCornerSizeSurrogate,
            bottomStart: ComposeCornerSizeSurrogate,
        ): ComposeShapeSurrogate =
            ComposeShapeSurrogate(
                kind = ComposeShapeKind.Rounded,
                topStart = topStart,
                topEnd = topEnd,
                bottomEnd = bottomEnd,
                bottomStart = bottomStart,
            )
    }
}

@Serializable
enum class ComposeShapeKind {
    @SerialName("rectangle")
    Rectangle,

    @SerialName("circle")
    Circle,

    @SerialName("rounded")
    Rounded,

    @SerialName("absoluteRounded")
    AbsoluteRounded,
}

@Serializable
data class ComposeCornerSizeSurrogate(
    val unit: ComposeCornerSizeUnit = ComposeCornerSizeUnit.Dp,
    val value: Float = 0f,
) {
    fun toCornerSize(): CornerSize {
        require(value >= 0f) { "圆角尺寸不能为负数：$value" }
        return when (unit) {
            ComposeCornerSizeUnit.Dp -> CornerSize(value.dp)
            ComposeCornerSizeUnit.Px -> CornerSize(value)
            ComposeCornerSizeUnit.Percent -> CornerSize(value.toPercentInt())
        }
    }

    companion object {
        fun dp(value: Float): ComposeCornerSizeSurrogate =
            ComposeCornerSizeSurrogate(ComposeCornerSizeUnit.Dp, value)

        fun px(value: Float): ComposeCornerSizeSurrogate =
            ComposeCornerSizeSurrogate(ComposeCornerSizeUnit.Px, value)

        fun percent(value: Int): ComposeCornerSizeSurrogate =
            ComposeCornerSizeSurrogate(ComposeCornerSizeUnit.Percent, value.toFloat())
    }
}

@Serializable
enum class ComposeCornerSizeUnit {
    @SerialName("dp")
    Dp,

    @SerialName("px")
    Px,

    @SerialName("percent")
    Percent,
}

object ComposeShapeSerializer : KSerializer<Shape> {
    override val descriptor: SerialDescriptor = ComposeShapeSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Shape) {
        encoder.encodeSerializableValue(ComposeShapeSurrogate.serializer(), value.toSurrogate())
    }

    override fun deserialize(decoder: Decoder): Shape =
        decoder.decodeSerializableValue(ComposeShapeSurrogate.serializer()).toShape()
}

fun Shape.toComposeShapeSurrogate(): ComposeShapeSurrogate = toSurrogate()

private fun Shape.toSurrogate(): ComposeShapeSurrogate =
    when {
        this === RectangleShape -> ComposeShapeSurrogate.rectangle()
        this == CircleShape -> ComposeShapeSurrogate.circle()
        this is RoundedCornerShape -> cornerShapeToSurrogate(ComposeShapeKind.Rounded)
        this is AbsoluteRoundedCornerShape -> cornerShapeToSurrogate(ComposeShapeKind.AbsoluteRounded)
        else -> throw SerializationException(
            "不支持序列化 Shape 实现：${this::class}. 请使用 ComposeShapeSurrogate 或业务 shape token。",
        )
    }

private fun CornerBasedShape.cornerShapeToSurrogate(kind: ComposeShapeKind): ComposeShapeSurrogate =
    ComposeShapeSurrogate(
        kind = kind,
        topStart = topStart.toSurrogate(),
        topEnd = topEnd.toSurrogate(),
        bottomEnd = bottomEnd.toSurrogate(),
        bottomStart = bottomStart.toSurrogate(),
    )

private fun CornerSize.toSurrogate(): ComposeCornerSizeSurrogate {
    if (this == ZeroCornerSize) {
        return ComposeCornerSizeSurrogate.dp(0f)
    }
    val value = (this as? InspectableValue)?.valueOverride
    return when (value) {
        is Dp -> ComposeCornerSizeSurrogate.dp(value.value)
        is String -> value.toCornerSizeSurrogate()
        else -> toString().toCornerSizeSurrogateOrNull()
            ?: throw SerializationException("不支持序列化 CornerSize：$this")
    }
}

private fun String.toCornerSizeSurrogate(): ComposeCornerSizeSurrogate =
    toCornerSizeSurrogateOrNull()
        ?: throw SerializationException("不支持序列化 CornerSize：$this")

private fun String.toCornerSizeSurrogateOrNull(): ComposeCornerSizeSurrogate? {
    val value = trim()
    if (value == "ZeroCornerSize") {
        return ComposeCornerSizeSurrogate.dp(0f)
    }
    val raw = value
        .removePrefix("CornerSize(size = ")
        .removeSuffix(")")
    return when {
        raw.endsWith(".dp") -> raw.removeSuffix(".dp").toFloatOrNull()
            ?.let(ComposeCornerSizeSurrogate::dp)
        raw.endsWith("px") -> raw.removeSuffix("px").toFloatOrNull()
            ?.let(ComposeCornerSizeSurrogate::px)
        raw.endsWith("%") -> raw.removeSuffix("%").toFloatOrNull()
            ?.let { ComposeCornerSizeSurrogate(ComposeCornerSizeUnit.Percent, it) }
        else -> null
    }
}

private fun Float.toPercentInt(): Int {
    val intValue = toInt()
    if (this != intValue.toFloat() || intValue !in 0..100) {
        throw SerializationException("百分比圆角必须是 0 到 100 的整数：$this")
    }
    return intValue
}
