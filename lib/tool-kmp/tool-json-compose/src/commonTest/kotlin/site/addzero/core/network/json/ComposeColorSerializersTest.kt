package site.addzero.core.network.json

import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals

class ComposeColorSerializersTest {
    @Test
    fun contextualColorUsesComposeJsonSerializersModule() {
        val source = ContextualColorPayload(color = Color(0xFF09090B))

        val encoded = composeJson.encodeToString(source)
        val decoded = composeJson.decodeFromString<ContextualColorPayload>(encoded)

        assertEquals(source.color.toArgb(), decoded.color.toArgb())
    }

    @Test
    fun contextualColorSchemeUsesComposeJsonSerializersModule() {
        val source = ContextualColorSchemePayload(
            scheme = darkColorScheme(
                primary = Color(0xFF91C5FF),
                surface = Color(0xFF111827),
                error = Color(0xFFFCA5A5),
            ),
        )

        val encoded = composeJson.encodeToString(source)
        val decoded = composeJson.decodeFromString<ContextualColorSchemePayload>(encoded)

        assertColorSchemeEquals(source.scheme, decoded.scheme)
    }

    @Test
    fun contextualShapeUsesComposeJsonSerializersModule() {
        val shapes = listOf(
            RectangleShape,
            CircleShape,
            RoundedCornerShape(12.dp),
            RoundedCornerShape(topStart = 4.dp, topEnd = 8.dp, bottomEnd = 12.dp, bottomStart = 16.dp),
            AbsoluteRoundedCornerShape(topLeft = 3.dp, topRight = 6.dp, bottomRight = 9.dp, bottomLeft = 12.dp),
        )

        shapes.forEach { shape ->
            val source = ContextualShapePayload(shape = shape)

            val encoded = composeJson.encodeToString(source)
            val decoded = composeJson.decodeFromString<ContextualShapePayload>(encoded)

            assertEquals(shape, decoded.shape)
        }
    }

    @Test
    fun contextualDpUsesComposeJsonSerializersModule() {
        val source = ContextualDpPayload(dp = 12.dp)

        val encoded = composeJson.encodeToString(source)
        val decoded = composeJson.decodeFromString<ContextualDpPayload>(encoded)

        assertEquals(source.dp, decoded.dp)
    }

    @Test
    fun contextualTextUnitUsesComposeJsonSerializersModule() {
        val source = ContextualTextUnitPayload(fontSize = 14.sp, letterSpacing = 0.08.em)

        val encoded = composeJson.encodeToString(source)
        val decoded = composeJson.decodeFromString<ContextualTextUnitPayload>(encoded)

        assertEquals(source.fontSize, decoded.fontSize)
        assertEquals(source.letterSpacing, decoded.letterSpacing)
    }

    @Test
    fun contextualPaddingValuesUsesComposeJsonSerializersModule() {
        val source = ContextualPaddingValuesPayload(
            padding = PaddingValues(start = 4.dp, top = 6.dp, end = 8.dp, bottom = 10.dp),
        )

        val encoded = composeJson.encodeToString(source)
        val decoded = composeJson.decodeFromString<ContextualPaddingValuesPayload>(encoded)

        assertEquals(4.dp, decoded.padding.calculateLeftPadding(LayoutDirection.Ltr))
        assertEquals(8.dp, decoded.padding.calculateRightPadding(LayoutDirection.Ltr))
        assertEquals(6.dp, decoded.padding.calculateTopPadding())
        assertEquals(10.dp, decoded.padding.calculateBottomPadding())
    }

    @Test
    fun contextualBorderStrokeUsesComposeJsonSerializersModule() {
        val source = ContextualBorderStrokePayload(border = BorderStroke(1.dp, Color(0xFF334155)))

        val encoded = composeJson.encodeToString(source)
        val decoded = composeJson.decodeFromString<ContextualBorderStrokePayload>(encoded)

        assertEquals(source.border, decoded.border)
    }

    @Test
    fun contextualTextStyleUsesComposeJsonSerializersModule() {
        val source = ContextualTextStylePayload(
            style = TextStyle(
                color = Color(0xFF111827),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Italic,
                letterSpacing = 0.02.em,
                background = Color(0xFFE5E7EB),
                textDecoration = TextDecoration.Underline,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
            ),
        )

        val encoded = composeJson.encodeToString(source)
        val decoded = composeJson.decodeFromString<ContextualTextStylePayload>(encoded)

        assertEquals(source.style.color.toArgb(), decoded.style.color.toArgb())
        assertEquals(source.style.fontSize, decoded.style.fontSize)
        assertEquals(source.style.fontWeight, decoded.style.fontWeight)
        assertEquals(source.style.fontStyle, decoded.style.fontStyle)
        assertEquals(source.style.letterSpacing, decoded.style.letterSpacing)
        assertEquals(source.style.background.toArgb(), decoded.style.background.toArgb())
        assertEquals(source.style.textDecoration, decoded.style.textDecoration)
        assertEquals(source.style.textAlign, decoded.style.textAlign)
        assertEquals(source.style.lineHeight, decoded.style.lineHeight)
    }
}

@Serializable
private data class ContextualColorPayload(
    @Contextual
    val color: Color,
)

@Serializable
private data class ContextualColorSchemePayload(
    @Contextual
    val scheme: ColorScheme,
)

@Serializable
private data class ContextualShapePayload(
    @Contextual
    val shape: Shape,
)

@Serializable
private data class ContextualDpPayload(
    @Contextual
    val dp: Dp,
)

@Serializable
private data class ContextualTextUnitPayload(
    @Contextual
    val fontSize: TextUnit,
    @Contextual
    val letterSpacing: TextUnit,
)

@Serializable
private data class ContextualPaddingValuesPayload(
    @Contextual
    val padding: PaddingValues,
)

@Serializable
private data class ContextualBorderStrokePayload(
    @Contextual
    val border: BorderStroke,
)

@Serializable
private data class ContextualTextStylePayload(
    @Contextual
    val style: TextStyle,
)

private fun assertColorSchemeEquals(expected: ColorScheme, actual: ColorScheme) {
    assertEquals(expected.primary.toArgb(), actual.primary.toArgb(), "primary")
    assertEquals(expected.onPrimary.toArgb(), actual.onPrimary.toArgb(), "onPrimary")
    assertEquals(expected.primaryContainer.toArgb(), actual.primaryContainer.toArgb(), "primaryContainer")
    assertEquals(expected.onPrimaryContainer.toArgb(), actual.onPrimaryContainer.toArgb(), "onPrimaryContainer")
    assertEquals(expected.inversePrimary.toArgb(), actual.inversePrimary.toArgb(), "inversePrimary")
    assertEquals(expected.secondary.toArgb(), actual.secondary.toArgb(), "secondary")
    assertEquals(expected.onSecondary.toArgb(), actual.onSecondary.toArgb(), "onSecondary")
    assertEquals(expected.secondaryContainer.toArgb(), actual.secondaryContainer.toArgb(), "secondaryContainer")
    assertEquals(expected.onSecondaryContainer.toArgb(), actual.onSecondaryContainer.toArgb(), "onSecondaryContainer")
    assertEquals(expected.tertiary.toArgb(), actual.tertiary.toArgb(), "tertiary")
    assertEquals(expected.onTertiary.toArgb(), actual.onTertiary.toArgb(), "onTertiary")
    assertEquals(expected.tertiaryContainer.toArgb(), actual.tertiaryContainer.toArgb(), "tertiaryContainer")
    assertEquals(expected.onTertiaryContainer.toArgb(), actual.onTertiaryContainer.toArgb(), "onTertiaryContainer")
    assertEquals(expected.background.toArgb(), actual.background.toArgb(), "background")
    assertEquals(expected.onBackground.toArgb(), actual.onBackground.toArgb(), "onBackground")
    assertEquals(expected.surface.toArgb(), actual.surface.toArgb(), "surface")
    assertEquals(expected.onSurface.toArgb(), actual.onSurface.toArgb(), "onSurface")
    assertEquals(expected.surfaceVariant.toArgb(), actual.surfaceVariant.toArgb(), "surfaceVariant")
    assertEquals(expected.onSurfaceVariant.toArgb(), actual.onSurfaceVariant.toArgb(), "onSurfaceVariant")
    assertEquals(expected.surfaceTint.toArgb(), actual.surfaceTint.toArgb(), "surfaceTint")
    assertEquals(expected.inverseSurface.toArgb(), actual.inverseSurface.toArgb(), "inverseSurface")
    assertEquals(expected.inverseOnSurface.toArgb(), actual.inverseOnSurface.toArgb(), "inverseOnSurface")
    assertEquals(expected.error.toArgb(), actual.error.toArgb(), "error")
    assertEquals(expected.onError.toArgb(), actual.onError.toArgb(), "onError")
    assertEquals(expected.errorContainer.toArgb(), actual.errorContainer.toArgb(), "errorContainer")
    assertEquals(expected.onErrorContainer.toArgb(), actual.onErrorContainer.toArgb(), "onErrorContainer")
    assertEquals(expected.outline.toArgb(), actual.outline.toArgb(), "outline")
    assertEquals(expected.outlineVariant.toArgb(), actual.outlineVariant.toArgb(), "outlineVariant")
    assertEquals(expected.scrim.toArgb(), actual.scrim.toArgb(), "scrim")
    assertEquals(expected.surfaceBright.toArgb(), actual.surfaceBright.toArgb(), "surfaceBright")
    assertEquals(expected.surfaceDim.toArgb(), actual.surfaceDim.toArgb(), "surfaceDim")
    assertEquals(expected.surfaceContainer.toArgb(), actual.surfaceContainer.toArgb(), "surfaceContainer")
    assertEquals(expected.surfaceContainerHigh.toArgb(), actual.surfaceContainerHigh.toArgb(), "surfaceContainerHigh")
    assertEquals(expected.surfaceContainerHighest.toArgb(), actual.surfaceContainerHighest.toArgb(), "surfaceContainerHighest")
    assertEquals(expected.surfaceContainerLow.toArgb(), actual.surfaceContainerLow.toArgb(), "surfaceContainerLow")
    assertEquals(expected.surfaceContainerLowest.toArgb(), actual.surfaceContainerLowest.toArgb(), "surfaceContainerLowest")
    assertEquals(expected.primaryFixed.toArgb(), actual.primaryFixed.toArgb(), "primaryFixed")
    assertEquals(expected.primaryFixedDim.toArgb(), actual.primaryFixedDim.toArgb(), "primaryFixedDim")
    assertEquals(expected.onPrimaryFixed.toArgb(), actual.onPrimaryFixed.toArgb(), "onPrimaryFixed")
    assertEquals(expected.onPrimaryFixedVariant.toArgb(), actual.onPrimaryFixedVariant.toArgb(), "onPrimaryFixedVariant")
    assertEquals(expected.secondaryFixed.toArgb(), actual.secondaryFixed.toArgb(), "secondaryFixed")
    assertEquals(expected.secondaryFixedDim.toArgb(), actual.secondaryFixedDim.toArgb(), "secondaryFixedDim")
    assertEquals(expected.onSecondaryFixed.toArgb(), actual.onSecondaryFixed.toArgb(), "onSecondaryFixed")
    assertEquals(expected.onSecondaryFixedVariant.toArgb(), actual.onSecondaryFixedVariant.toArgb(), "onSecondaryFixedVariant")
    assertEquals(expected.tertiaryFixed.toArgb(), actual.tertiaryFixed.toArgb(), "tertiaryFixed")
    assertEquals(expected.tertiaryFixedDim.toArgb(), actual.tertiaryFixedDim.toArgb(), "tertiaryFixedDim")
    assertEquals(expected.onTertiaryFixed.toArgb(), actual.onTertiaryFixed.toArgb(), "onTertiaryFixed")
    assertEquals(expected.onTertiaryFixedVariant.toArgb(), actual.onTertiaryFixedVariant.toArgb(), "onTertiaryFixedVariant")
}
