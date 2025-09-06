package com.addzero.ui.infra.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.addzero.constant.Colors

/**
 * 渐变背景配置
 */
data class GradientConfig(
    val colors: List<Color>,
    val startX: Float = 0f,
    val startY: Float = 0f,
    val endX: Float = 1000f,
    val endY: Float = 1000f
)

/**
 * 应用主题类型
 */
@Deprecated(
    message = "改用字典",
    replaceWith = ReplaceWith("EnumSysTheme")
)
enum class AppThemeType {
    LIGHT_DEFAULT,
    DARK_DEFAULT,
    LIGHT_BLUE,
    DARK_BLUE,
    LIGHT_GREEN,
    DARK_GREEN,
    LIGHT_PURPLE,
    DARK_PURPLE;

    /**
     * 获取主题名称
     */
    fun getDisplayName(): String {
        return when (this) {
            LIGHT_DEFAULT -> "默认亮色"
            DARK_DEFAULT -> "默认暗色"
            LIGHT_BLUE -> "蓝色亮色"
            DARK_BLUE -> "蓝色暗色"
            LIGHT_GREEN -> "绿色亮色"
            DARK_GREEN -> "绿色暗色"
            LIGHT_PURPLE -> "紫色亮色"
            DARK_PURPLE -> "紫色暗色"
        }
    }

    /**
     * 是否为暗色主题
     */
    fun isDark(): Boolean {
        return this == DARK_DEFAULT || this == DARK_BLUE || this == DARK_GREEN || this == DARK_PURPLE
    }
}

/**
 * 应用主题配置
 */
object AppThemes {

    // 默认亮色主题
    private val LightDefaultScheme = lightColorScheme()

    // 默认暗色主题
    private val DarkDefaultScheme = darkColorScheme()

    // 蓝色主题
    private val LightBlueScheme = lightColorScheme(
        primary = Colors.DeepBlue,
        primaryContainer = Colors.LightBlueContainer,
        secondary = Colors.DarkSecondaryBlue,
        secondaryContainer = Colors.SecondaryBlueContainer,
        surface = Colors.LightBlueSurface,
        background = Colors.VeryLightBlueBackground,
        onPrimary = Color.White,
        onSecondary = Color.White
    )

    private val DarkBlueScheme = darkColorScheme(
        primary = Colors.DarkBluePrimary,
        primaryContainer = Colors.DarkBluePrimaryContainer,
        secondary = Colors.DarkBlueSecondary,
        secondaryContainer = Colors.DarkBlueSecondaryContainer,
        onPrimary = Colors.Black,
        onSecondary = Colors.Black
    )

    // 绿色主题
    private val LightGreenScheme = lightColorScheme(
        primary = Colors.GreenPrimary,
        primaryContainer = Colors.GreenContainer,
        secondary = Colors.DarkGreenSecondary,
        secondaryContainer = Colors.GreenSecondaryContainer,
        onPrimary = Color.White,
        onSecondary = Color.White
    )

    private val DarkGreenScheme = darkColorScheme(
        primary = Colors.DarkGreenPrimary,
        primaryContainer = Colors.DarkGreenPrimaryContainer,
        secondary = Colors.DarkGreenSecondaryColor,
        secondaryContainer = Colors.DarkGreenSecondaryContainer,
        onPrimary = Colors.Black,
        onSecondary = Colors.Black
    )

    // 紫色主题
    private val LightPurpleScheme = lightColorScheme(
        primary = Colors.PurplePrimary,
        primaryContainer = Colors.PurpleContainer,
        secondary = Colors.DarkPurpleSecondary,
        secondaryContainer = Colors.PurpleSecondaryContainer,
        onPrimary = Color.White,
        onSecondary = Color.White
    )

    private val DarkPurpleScheme = darkColorScheme(
        primary = Colors.DarkPurplePrimary,
        primaryContainer = Colors.DarkPurplePrimaryContainer,
        secondary = Colors.DarkPurpleSecondaryColor,
        secondaryContainer = Colors.DarkPurpleSecondaryContainer,
        onPrimary = Colors.Black,
        onSecondary = Colors.Black
    )

    /**
     * 根据主题类型获取对应的颜色方案
     */
    fun getColorScheme(themeType: AppThemeType): ColorScheme {
        return when (themeType) {
            AppThemeType.LIGHT_DEFAULT -> LightDefaultScheme
            AppThemeType.DARK_DEFAULT -> DarkDefaultScheme
            AppThemeType.LIGHT_BLUE -> LightBlueScheme
            AppThemeType.DARK_BLUE -> DarkBlueScheme
            AppThemeType.LIGHT_GREEN -> LightGreenScheme
            AppThemeType.DARK_GREEN -> DarkGreenScheme
            AppThemeType.LIGHT_PURPLE -> LightPurpleScheme
            AppThemeType.DARK_PURPLE -> DarkPurpleScheme
        }
    }

    /**
     * 获取渐变配置
     */
    fun getGradientConfig(themeType: AppThemeType): GradientConfig? {
        return null
    }
}
