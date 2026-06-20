package site.addzero.core.network.json

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ComposeSurfaceSerializersTest {
    @Test
    fun contextualSurfaceFieldsUseComposeJsonSerializersModule() {
        val source = SurfaceDefaultsPayload(
            border = BorderStroke(1.dp, Color(0xFF334155)),
            shadowElevation = 12.dp,
            contentPadding = PaddingValues(start = 4.dp, top = 6.dp, end = 8.dp, bottom = 10.dp),
        )

        val encoded = composeJson.encodeToString(source)
        val decoded = composeJson.decodeFromString<SurfaceDefaultsPayload>(encoded)

        assertEquals(source.border, decoded.border)
        assertEquals(source.shadowElevation, decoded.shadowElevation)
        assertEquals(4.dp, decoded.contentPadding.calculateLeftPadding(LayoutDirection.Ltr))
        assertEquals(8.dp, decoded.contentPadding.calculateRightPadding(LayoutDirection.Ltr))
        assertEquals(8.dp, decoded.contentPadding.calculateLeftPadding(LayoutDirection.Rtl))
        assertEquals(4.dp, decoded.contentPadding.calculateRightPadding(LayoutDirection.Rtl))
        assertEquals(6.dp, decoded.contentPadding.calculateTopPadding())
        assertEquals(10.dp, decoded.contentPadding.calculateBottomPadding())
    }

    @Test
    fun contextualNullableBorderStrokeSupportsNull() {
        val source = SurfaceDefaultsPayload(
            border = null,
            shadowElevation = 0.dp,
            contentPadding = PaddingValues(0.dp),
        )

        val decoded = composeJson.decodeFromString<SurfaceDefaultsPayload>(composeJson.encodeToString(source))

        assertNull(decoded.border)
        assertEquals(source.shadowElevation, decoded.shadowElevation)
    }

    @Test
    fun contextualDpSerializesSpecialValuesAsStableStrings() {
        val unspecified = DpPayload(value = Dp.Unspecified)
        val infinity = DpPayload(value = Dp.Infinity)

        val encodedUnspecified = composeJson.encodeToString(unspecified)
        val encodedInfinity = composeJson.encodeToString(infinity)

        assertTrue("\"unspecified\"" in encodedUnspecified)
        assertTrue("\"infinity\"" in encodedInfinity)
        assertTrue(composeJson.decodeFromString<DpPayload>(encodedUnspecified).value.value.isNaN())
        assertEquals(Dp.Infinity, composeJson.decodeFromString<DpPayload>(encodedInfinity).value)
    }
}

@Serializable
private data class SurfaceDefaultsPayload(
    @Contextual
    val border: BorderStroke?,
    @Contextual
    val shadowElevation: Dp,
    @Contextual
    val contentPadding: PaddingValues,
)

@Serializable
private data class DpPayload(
    @Contextual
    val value: Dp,
)
