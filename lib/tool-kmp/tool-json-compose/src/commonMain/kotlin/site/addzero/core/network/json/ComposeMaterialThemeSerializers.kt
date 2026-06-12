package site.addzero.core.network.json

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val DefaultMaterialShapes = Shapes()

private val DefaultMaterialTypography = Typography()

/**
 * Material3 [Shapes] 的配置替身。
 *
 * 每个 shape 仍复用 [ComposeShapeSerializer]，只允许能还原为 [CornerBasedShape] 的形状进入
 * Material shape scale。任意自定义 [Shape] 应保存业务 token。
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Serializable
data class MaterialShapesSurrogate(
    @Serializable(with = ComposeShapeSerializer::class)
    val extraSmall: Shape = DefaultMaterialShapes.extraSmall,
    @Serializable(with = ComposeShapeSerializer::class)
    val small: Shape = DefaultMaterialShapes.small,
    @Serializable(with = ComposeShapeSerializer::class)
    val medium: Shape = DefaultMaterialShapes.medium,
    @Serializable(with = ComposeShapeSerializer::class)
    val large: Shape = DefaultMaterialShapes.large,
    @Serializable(with = ComposeShapeSerializer::class)
    val extraLarge: Shape = DefaultMaterialShapes.extraLarge,
    @Serializable(with = ComposeShapeSerializer::class)
    val largeIncreased: Shape = DefaultMaterialShapes.largeIncreased,
    @Serializable(with = ComposeShapeSerializer::class)
    val extraLargeIncreased: Shape = DefaultMaterialShapes.extraLargeIncreased,
    @Serializable(with = ComposeShapeSerializer::class)
    val extraExtraLarge: Shape = DefaultMaterialShapes.extraExtraLarge,
) {
    fun toShapes(): Shapes =
        Shapes(
            extraSmall = extraSmall.toCornerBasedShape("extraSmall"),
            small = small.toCornerBasedShape("small"),
            medium = medium.toCornerBasedShape("medium"),
            large = large.toCornerBasedShape("large"),
            extraLarge = extraLarge.toCornerBasedShape("extraLarge"),
            largeIncreased = largeIncreased.toCornerBasedShape("largeIncreased"),
            extraLargeIncreased = extraLargeIncreased.toCornerBasedShape("extraLargeIncreased"),
            extraExtraLarge = extraExtraLarge.toCornerBasedShape("extraExtraLarge"),
        )

    companion object {
        fun from(value: Shapes): MaterialShapesSurrogate =
            MaterialShapesSurrogate(
                extraSmall = value.extraSmall,
                small = value.small,
                medium = value.medium,
                large = value.large,
                extraLarge = value.extraLarge,
                largeIncreased = value.largeIncreased,
                extraLargeIncreased = value.extraLargeIncreased,
                extraExtraLarge = value.extraExtraLarge,
            )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
object MaterialShapesSerializer : KSerializer<Shapes> {
    override val descriptor: SerialDescriptor = MaterialShapesSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Shapes) {
        encoder.encodeSerializableValue(MaterialShapesSurrogate.serializer(), MaterialShapesSurrogate.from(value))
    }

    override fun deserialize(decoder: Decoder): Shapes =
        decoder.decodeSerializableValue(MaterialShapesSurrogate.serializer()).toShapes()
}

/**
 * Material3 [Typography] 的配置替身。
 *
 * 每个文本层级复用 [ComposeTextStyleSerializer] 的受控子集，避免把平台字体、阴影和运行时 brush
 * 直接写进主题 JSON。
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Serializable
data class MaterialTypographySurrogate(
    @Serializable(with = ComposeTextStyleSerializer::class)
    val displayLarge: TextStyle = DefaultMaterialTypography.displayLarge,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val displayMedium: TextStyle = DefaultMaterialTypography.displayMedium,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val displaySmall: TextStyle = DefaultMaterialTypography.displaySmall,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val headlineLarge: TextStyle = DefaultMaterialTypography.headlineLarge,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val headlineMedium: TextStyle = DefaultMaterialTypography.headlineMedium,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val headlineSmall: TextStyle = DefaultMaterialTypography.headlineSmall,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val titleLarge: TextStyle = DefaultMaterialTypography.titleLarge,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val titleMedium: TextStyle = DefaultMaterialTypography.titleMedium,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val titleSmall: TextStyle = DefaultMaterialTypography.titleSmall,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val bodyLarge: TextStyle = DefaultMaterialTypography.bodyLarge,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val bodyMedium: TextStyle = DefaultMaterialTypography.bodyMedium,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val bodySmall: TextStyle = DefaultMaterialTypography.bodySmall,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val labelLarge: TextStyle = DefaultMaterialTypography.labelLarge,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val labelMedium: TextStyle = DefaultMaterialTypography.labelMedium,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val labelSmall: TextStyle = DefaultMaterialTypography.labelSmall,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val displayLargeEmphasized: TextStyle = DefaultMaterialTypography.displayLargeEmphasized,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val displayMediumEmphasized: TextStyle = DefaultMaterialTypography.displayMediumEmphasized,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val displaySmallEmphasized: TextStyle = DefaultMaterialTypography.displaySmallEmphasized,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val headlineLargeEmphasized: TextStyle = DefaultMaterialTypography.headlineLargeEmphasized,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val headlineMediumEmphasized: TextStyle = DefaultMaterialTypography.headlineMediumEmphasized,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val headlineSmallEmphasized: TextStyle = DefaultMaterialTypography.headlineSmallEmphasized,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val titleLargeEmphasized: TextStyle = DefaultMaterialTypography.titleLargeEmphasized,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val titleMediumEmphasized: TextStyle = DefaultMaterialTypography.titleMediumEmphasized,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val titleSmallEmphasized: TextStyle = DefaultMaterialTypography.titleSmallEmphasized,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val bodyLargeEmphasized: TextStyle = DefaultMaterialTypography.bodyLargeEmphasized,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val bodyMediumEmphasized: TextStyle = DefaultMaterialTypography.bodyMediumEmphasized,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val bodySmallEmphasized: TextStyle = DefaultMaterialTypography.bodySmallEmphasized,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val labelLargeEmphasized: TextStyle = DefaultMaterialTypography.labelLargeEmphasized,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val labelMediumEmphasized: TextStyle = DefaultMaterialTypography.labelMediumEmphasized,
    @Serializable(with = ComposeTextStyleSerializer::class)
    val labelSmallEmphasized: TextStyle = DefaultMaterialTypography.labelSmallEmphasized,
) {
    fun toTypography(): Typography =
        Typography(
            displayLarge = displayLarge,
            displayMedium = displayMedium,
            displaySmall = displaySmall,
            headlineLarge = headlineLarge,
            headlineMedium = headlineMedium,
            headlineSmall = headlineSmall,
            titleLarge = titleLarge,
            titleMedium = titleMedium,
            titleSmall = titleSmall,
            bodyLarge = bodyLarge,
            bodyMedium = bodyMedium,
            bodySmall = bodySmall,
            labelLarge = labelLarge,
            labelMedium = labelMedium,
            labelSmall = labelSmall,
            displayLargeEmphasized = displayLargeEmphasized,
            displayMediumEmphasized = displayMediumEmphasized,
            displaySmallEmphasized = displaySmallEmphasized,
            headlineLargeEmphasized = headlineLargeEmphasized,
            headlineMediumEmphasized = headlineMediumEmphasized,
            headlineSmallEmphasized = headlineSmallEmphasized,
            titleLargeEmphasized = titleLargeEmphasized,
            titleMediumEmphasized = titleMediumEmphasized,
            titleSmallEmphasized = titleSmallEmphasized,
            bodyLargeEmphasized = bodyLargeEmphasized,
            bodyMediumEmphasized = bodyMediumEmphasized,
            bodySmallEmphasized = bodySmallEmphasized,
            labelLargeEmphasized = labelLargeEmphasized,
            labelMediumEmphasized = labelMediumEmphasized,
            labelSmallEmphasized = labelSmallEmphasized,
        )

    companion object {
        fun from(value: Typography): MaterialTypographySurrogate =
            MaterialTypographySurrogate(
                displayLarge = value.displayLarge,
                displayMedium = value.displayMedium,
                displaySmall = value.displaySmall,
                headlineLarge = value.headlineLarge,
                headlineMedium = value.headlineMedium,
                headlineSmall = value.headlineSmall,
                titleLarge = value.titleLarge,
                titleMedium = value.titleMedium,
                titleSmall = value.titleSmall,
                bodyLarge = value.bodyLarge,
                bodyMedium = value.bodyMedium,
                bodySmall = value.bodySmall,
                labelLarge = value.labelLarge,
                labelMedium = value.labelMedium,
                labelSmall = value.labelSmall,
                displayLargeEmphasized = value.displayLargeEmphasized,
                displayMediumEmphasized = value.displayMediumEmphasized,
                displaySmallEmphasized = value.displaySmallEmphasized,
                headlineLargeEmphasized = value.headlineLargeEmphasized,
                headlineMediumEmphasized = value.headlineMediumEmphasized,
                headlineSmallEmphasized = value.headlineSmallEmphasized,
                titleLargeEmphasized = value.titleLargeEmphasized,
                titleMediumEmphasized = value.titleMediumEmphasized,
                titleSmallEmphasized = value.titleSmallEmphasized,
                bodyLargeEmphasized = value.bodyLargeEmphasized,
                bodyMediumEmphasized = value.bodyMediumEmphasized,
                bodySmallEmphasized = value.bodySmallEmphasized,
                labelLargeEmphasized = value.labelLargeEmphasized,
                labelMediumEmphasized = value.labelMediumEmphasized,
                labelSmallEmphasized = value.labelSmallEmphasized,
            )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
object MaterialTypographySerializer : KSerializer<Typography> {
    override val descriptor: SerialDescriptor = MaterialTypographySurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Typography) {
        encoder.encodeSerializableValue(MaterialTypographySurrogate.serializer(), MaterialTypographySurrogate.from(value))
    }

    override fun deserialize(decoder: Decoder): Typography =
        decoder.decodeSerializableValue(MaterialTypographySurrogate.serializer()).toTypography()
}

private fun Shape.toCornerBasedShape(role: String): CornerBasedShape =
    this as? CornerBasedShape
        ?: throw SerializationException("Material Shapes.$role 必须是 CornerBasedShape，当前是 ${this::class}")
