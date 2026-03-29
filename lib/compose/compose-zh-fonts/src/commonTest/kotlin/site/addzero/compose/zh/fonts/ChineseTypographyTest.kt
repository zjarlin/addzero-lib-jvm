package site.addzero.compose.zh.fonts

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import kotlin.test.Test
import kotlin.test.assertEquals

class ChineseTypographyTest {
    @Test
    fun withFontFamily_applies_to_all_material_styles() {
        val fontFamily = FontFamily.SansSerif
        val typography = Typography().withFontFamily(fontFamily)

        assertEquals(fontFamily, typography.displayLarge.fontFamily)
        assertEquals(fontFamily, typography.displayMedium.fontFamily)
        assertEquals(fontFamily, typography.displaySmall.fontFamily)
        assertEquals(fontFamily, typography.headlineLarge.fontFamily)
        assertEquals(fontFamily, typography.headlineMedium.fontFamily)
        assertEquals(fontFamily, typography.headlineSmall.fontFamily)
        assertEquals(fontFamily, typography.titleLarge.fontFamily)
        assertEquals(fontFamily, typography.titleMedium.fontFamily)
        assertEquals(fontFamily, typography.titleSmall.fontFamily)
        assertEquals(fontFamily, typography.bodyLarge.fontFamily)
        assertEquals(fontFamily, typography.bodyMedium.fontFamily)
        assertEquals(fontFamily, typography.bodySmall.fontFamily)
        assertEquals(fontFamily, typography.labelLarge.fontFamily)
        assertEquals(fontFamily, typography.labelMedium.fontFamily)
        assertEquals(fontFamily, typography.labelSmall.fontFamily)
    }
}
