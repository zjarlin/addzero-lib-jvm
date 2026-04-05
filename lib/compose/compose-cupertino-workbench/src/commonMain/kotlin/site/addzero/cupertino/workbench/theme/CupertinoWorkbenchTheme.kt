package site.addzero.cupertino.workbench.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import io.github.robinpcrd.cupertino.adaptive.AdaptiveTheme
import io.github.robinpcrd.cupertino.adaptive.CupertinoThemeSpec
import io.github.robinpcrd.cupertino.adaptive.ExperimentalAdaptiveApi
import io.github.robinpcrd.cupertino.adaptive.MaterialThemeSpec
import io.github.robinpcrd.cupertino.adaptive.Theme
import io.github.robinpcrd.cupertino.theme.Shapes as CupertinoShapes
import io.github.robinpcrd.cupertino.theme.Typography as CupertinoTypography
import io.github.robinpcrd.cupertino.theme.darkColorScheme as darkCupertinoColorScheme
import io.github.robinpcrd.cupertino.theme.lightColorScheme as lightCupertinoColorScheme

typealias WorkbenchColorScheme = ColorScheme
typealias WorkbenchTypography = Typography
typealias WorkbenchShapes = Shapes

object WorkbenchTheme {
  val colorScheme
      @Composable
    @ReadOnlyComposable
    get() = androidx.compose.material3.MaterialTheme.colorScheme

  val typography
      @Composable
    @ReadOnlyComposable
    get() = androidx.compose.material3.MaterialTheme.typography

  val shapes
      @Composable
    @ReadOnlyComposable
    get() = androidx.compose.material3.MaterialTheme.shapes
}

@OptIn(ExperimentalCupertinoApi::class, ExperimentalAdaptiveApi::class)
@Composable
fun CupertinoWorkbenchTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val materialColorScheme = remember(darkTheme) {
    if (darkTheme) {
      darkWorkbenchMaterialColorScheme()
    } else {
      lightWorkbenchMaterialColorScheme()
    }
  }
  val materialTypography = remember {
    workbenchMaterialTypography()
  }
  val materialShapes = remember {
    Shapes()
  }
  val cupertinoColorScheme = remember(darkTheme) {
    if (darkTheme) {
      darkWorkbenchCupertinoColorScheme()
    } else {
      lightWorkbenchCupertinoColorScheme()
    }
  }
  val cupertinoTypography = remember {
    workbenchCupertinoTypography()
  }
  val cupertinoShapes = remember {
    CupertinoShapes()
  }

  AdaptiveTheme(
    target = Theme.Cupertino,
    material = MaterialThemeSpec(
      colorScheme = materialColorScheme,
      shapes = materialShapes,
      typography = materialTypography,
    ),
    cupertino = CupertinoThemeSpec(
      colorScheme = cupertinoColorScheme,
      shapes = cupertinoShapes,
      typography = cupertinoTypography,
    ),
    content = content,
  )
}

private fun lightWorkbenchMaterialColorScheme(): ColorScheme {
  return lightColorScheme(
    primary = Color(0xFF0A84FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7ECFF),
    onPrimaryContainer = Color(0xFF0E2940),
    secondary = Color(0xFF30B0C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD5F4F8),
    onSecondaryContainer = Color(0xFF0D3038),
    tertiary = Color(0xFF32D74B),
    onTertiary = Color(0xFF05240A),
    tertiaryContainer = Color(0xFFD8F9DE),
    onTertiaryContainer = Color(0xFF0A2D10),
    error = Color(0xFFFF453A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF4F5F8),
    onBackground = Color(0xFF16181D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF16181D),
    surfaceVariant = Color(0xFFF0F2F7),
    onSurfaceVariant = Color(0xFF5B6270),
    outline = Color(0xFFBDC5D2),
    outlineVariant = Color(0xFFD8DEE8),
  )
}

private fun darkWorkbenchMaterialColorScheme(): ColorScheme {
  return darkColorScheme(
    primary = Color(0xFF5AC8FA),
    onPrimary = Color(0xFF06233A),
    primaryContainer = Color(0xFF0D375A),
    onPrimaryContainer = Color(0xFFD7ECFF),
    secondary = Color(0xFF7DD3E0),
    onSecondary = Color(0xFF08252C),
    secondaryContainer = Color(0xFF123F47),
    onSecondaryContainer = Color(0xFFD5F4F8),
    tertiary = Color(0xFF66E27A),
    onTertiary = Color(0xFF07260C),
    tertiaryContainer = Color(0xFF13381A),
    onTertiaryContainer = Color(0xFFD8F9DE),
    error = Color(0xFFFF6961),
    onError = Color(0xFF360001),
    errorContainer = Color(0xFF5A1212),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF111317),
    onBackground = Color(0xFFF3F4F7),
    surface = Color(0xFF1A1D23),
    onSurface = Color(0xFFF3F4F7),
    surfaceVariant = Color(0xFF232730),
    onSurfaceVariant = Color(0xFFB5BEC9),
    outline = Color(0xFF6F7886),
    outlineVariant = Color(0xFF3A404C),
  )
}

private fun lightWorkbenchCupertinoColorScheme() =
  lightCupertinoColorScheme(
    accent = Color(0xFF0A84FF),
    link = Color(0xFF0A84FF),
  )

private fun darkWorkbenchCupertinoColorScheme() =
  darkCupertinoColorScheme(
    accent = Color(0xFF5AC8FA),
    link = Color(0xFF5AC8FA),
  )

private fun workbenchMaterialTypography(): Typography {
  val base = Typography()
  return base.copy(
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    titleSmall = base.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    labelMedium = base.labelMedium.copy(fontWeight = FontWeight.Medium),
    labelSmall = base.labelSmall.copy(fontWeight = FontWeight.Medium),
  )
}

private fun workbenchCupertinoTypography(): CupertinoTypography {
  val base = CupertinoTypography()
  return base.copy(
    title1 = base.title1.copy(fontWeight = FontWeight.SemiBold),
    title2 = base.title2.copy(fontWeight = FontWeight.SemiBold),
    title3 = base.title3.copy(fontWeight = FontWeight.SemiBold),
    headline = base.headline.copy(fontWeight = FontWeight.SemiBold),
    body = base.body.merge(TextStyle(fontWeight = FontWeight.Normal)),
  )
}
