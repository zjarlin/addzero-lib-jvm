package com.addzero.ui.infra.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.addzero.constant.Colors
import com.addzero.generated.enums.EnumSysTheme


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
    fun getColorScheme(themeType:EnumSysTheme): ColorScheme {
        return when (themeType) {
            EnumSysTheme.LIGHT_DEFAULT -> LightDefaultScheme
            EnumSysTheme.DARK_DEFAULT -> DarkDefaultScheme
            EnumSysTheme.LIGHT_BLUE -> LightBlueScheme
            EnumSysTheme.DARK_BLUE -> DarkBlueScheme
            EnumSysTheme.LIGHT_GREEN -> LightGreenScheme
            EnumSysTheme.DARK_GREEN -> DarkGreenScheme
            EnumSysTheme.LIGHT_PURPLE -> LightPurpleScheme
            EnumSysTheme.DARK_PURPLE -> DarkPurpleScheme
        }
    }

}
