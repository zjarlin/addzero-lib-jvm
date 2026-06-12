package site.addzero.core.network.json

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
