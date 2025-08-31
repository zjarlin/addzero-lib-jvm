package com.addzero

import androidx.compose.runtime.Composable
import com.addzero.events.EventBusConsumer
import com.addzero.events.emitEventBus
import com.addzero.ui.auth.LoginScreen
import com.addzero.ui.infra.MainLayout
import com.addzero.ui.infra.theme.AppThemes
import com.addzero.ui.infra.theme.FollowSystemTheme
import com.addzero.ui.infra.theme.ThemeViewModel
import com.addzero.viewmodel.LoginViewModel
import com.addzero.component.toast.ToastListener
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

@Module
@ComponentScan("com.addzero.kmp")
class MyModule

@Composable
fun App() {
    initKoin()
    emitEventBus()
    EventBusConsumer()
    val themeViewModel = koinViewModel<ThemeViewModel>()
    // 已登录时渲染主界面
    val currentTheme = themeViewModel.currentTheme
    val colorScheme = AppThemes.getColorScheme(currentTheme)

    FollowSystemTheme(colorScheme = colorScheme) {
//        GradientThemeWrapper(themeType = currentTheme) {
            MainLayoutWithLogin()
//        MainLayout()
        com.addzero.component.toast.ToastListener()
//        }
    }
}

@Composable
private fun MainLayoutWithLogin() {
    val loginViewModel = koinViewModel<LoginViewModel>()
    if (loginViewModel.currentToken == null
//        && AddHttpClient .getCurrentToken()==null
    ) {
        // 未登录时只渲染登录页
        LoginScreen()
//            LoginUtil.cleanViewModel()
    } else {
        MainLayout()
    }
}

@Composable
private fun initKoin() {
    startKoin {
        printLogger()
        modules(
            MyModule().module
        )
    }
}

