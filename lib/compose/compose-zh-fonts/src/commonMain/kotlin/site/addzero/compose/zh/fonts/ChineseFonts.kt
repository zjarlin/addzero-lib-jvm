package site.addzero.compose.zh.fonts

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily

@Composable
expect fun rememberChineseUiFontFamilyOrNull(): FontFamily?

@Composable
fun rememberChineseTypography(
    base: Typography = Typography(),
): Typography = rememberChineseTypographyOrNull(base) ?: base

@Composable
fun rememberChineseTypographyOrNull(
    base: Typography = Typography(),
): Typography? {
    val fontFamily = rememberChineseUiFontFamilyOrNull() ?: return null
    return remember(base, fontFamily) { base.withFontFamily(fontFamily) }
}

fun Typography.withFontFamily(fontFamily: FontFamily): Typography =
    Typography(
        displayLarge = displayLarge.withFontFamily(fontFamily),
        displayMedium = displayMedium.withFontFamily(fontFamily),
        displaySmall = displaySmall.withFontFamily(fontFamily),
        headlineLarge = headlineLarge.withFontFamily(fontFamily),
        headlineMedium = headlineMedium.withFontFamily(fontFamily),
        headlineSmall = headlineSmall.withFontFamily(fontFamily),
        titleLarge = titleLarge.withFontFamily(fontFamily),
        titleMedium = titleMedium.withFontFamily(fontFamily),
        titleSmall = titleSmall.withFontFamily(fontFamily),
        bodyLarge = bodyLarge.withFontFamily(fontFamily),
        bodyMedium = bodyMedium.withFontFamily(fontFamily),
        bodySmall = bodySmall.withFontFamily(fontFamily),
        labelLarge = labelLarge.withFontFamily(fontFamily),
        labelMedium = labelMedium.withFontFamily(fontFamily),
        labelSmall = labelSmall.withFontFamily(fontFamily),
    )

private fun TextStyle.withFontFamily(fontFamily: FontFamily): TextStyle = copy(fontFamily = fontFamily)
