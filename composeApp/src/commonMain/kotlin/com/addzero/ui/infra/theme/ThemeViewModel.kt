package com.addzero.ui.infra.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.koin.android.annotation.KoinViewModel

/**
 * 主题视图模型
 * @author zjarlin
 * @date 2025/04/11
 */
@KoinViewModel
class ThemeViewModel : ViewModel() {
    // 当前主题类型，默认为蓝色亮色主题
    var currentTheme by mutableStateOf(AppThemeType.LIGHT_BLUE)
        private set

    val isDarkMode: Boolean
        get() = currentTheme.isDark()

    // 切换明暗主题 - 保留此方法以保持兼容性
    fun toggleTheme() {
        currentTheme = if (isDarkMode) {
            // 如果当前是暗色主题，切换为对应的亮色主题
            when (currentTheme) {
                AppThemeType.DARK_DEFAULT -> AppThemeType.LIGHT_DEFAULT
                AppThemeType.DARK_BLUE -> AppThemeType.LIGHT_BLUE
                AppThemeType.DARK_GREEN -> AppThemeType.LIGHT_GREEN
                AppThemeType.DARK_PURPLE -> AppThemeType.LIGHT_PURPLE
                else -> AppThemeType.LIGHT_BLUE
            }
        } else {
            // 如果当前是亮色主题，切换为对应的暗色主题
            when (currentTheme) {
                AppThemeType.LIGHT_DEFAULT -> AppThemeType.DARK_DEFAULT
                AppThemeType.LIGHT_BLUE -> AppThemeType.DARK_BLUE
                AppThemeType.LIGHT_GREEN -> AppThemeType.DARK_GREEN
                AppThemeType.LIGHT_PURPLE -> AppThemeType.DARK_PURPLE
                else -> AppThemeType.DARK_BLUE
            }
        }
    }

    // 设置特定主题
    fun setTheme(themeType: AppThemeType) {
        currentTheme = themeType
    }

    // 获取所有可用主题
    fun getAllThemes(): List<AppThemeType> {
        return AppThemeType.entries
    }
}
