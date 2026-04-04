package site.addzero.themes

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val Slate950 = Color(0xFF0F172A)
private val Slate900 = Color(0xFF111827)
private val Slate800 = Color(0xFF1F2937)
private val Slate700 = Color(0xFF334155)
private val Slate600 = Color(0xFF475569)
private val Slate500 = Color(0xFF64748B)
private val Slate300 = Color(0xFFCBD5E1)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate100 = Color(0xFFF1F5F9)
private val Slate50 = Color(0xFFF8FAFC)
private val Sky700 = Color(0xFF0369A1)
private val Sky600 = Color(0xFF0284C7)
private val Sky500 = Color(0xFF0EA5E9)
private val Sky400 = Color(0xFF38BDF8)
private val Emerald400 = Color(0xFF34D399)
private val Emerald300 = Color(0xFF6EE7B7)
private val Red400 = Color(0xFFF87171)
private val Red300 = Color(0xFFFCA5A5)
private val Amber400 = Color(0xFFFBBF24)

val DefaultMaterialDarkColorScheme = darkColorScheme(
    primary = Sky400,
    onPrimary = Slate950,
    primaryContainer = Sky700,
    onPrimaryContainer = Slate50,
    secondary = Slate300,
    onSecondary = Slate900,
    secondaryContainer = Slate700,
    onSecondaryContainer = Slate50,
    tertiary = Emerald300,
    onTertiary = Slate950,
    tertiaryContainer = Emerald400.copy(alpha = 0.24f),
    onTertiaryContainer = Slate50,
    background = Slate950,
    onBackground = Slate50,
    surface = Slate900,
    onSurface = Slate50,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate300,
    surfaceTint = Sky400,
    inverseSurface = Slate100,
    inverseOnSurface = Slate900,
    error = Red300,
    onError = Slate950,
    errorContainer = Red400.copy(alpha = 0.24f),
    onErrorContainer = Slate50,
    outline = Slate600,
    outlineVariant = Slate700,
    scrim = Color.Black.copy(alpha = 0.72f),
)

val DefaultMaterialLightColorScheme = lightColorScheme(
    primary = Sky600,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9F1FD),
    onPrimaryContainer = Slate950,
    secondary = Slate700,
    onSecondary = Color.White,
    secondaryContainer = Slate100,
    onSecondaryContainer = Slate900,
    tertiary = Emerald400,
    onTertiary = Slate950,
    tertiaryContainer = Color(0xFFDDFBEF),
    onTertiaryContainer = Slate900,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Slate500,
    surfaceTint = Sky500,
    inverseSurface = Slate900,
    inverseOnSurface = Slate50,
    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
    outline = Slate200,
    outlineVariant = Slate100,
    scrim = Color.Black.copy(alpha = 0.14f),
)
