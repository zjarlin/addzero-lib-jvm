package com.addzero

import androidx.compose.runtime.Composable
import com.addzero.component.toast.ToastListener
import com.addzero.di.NavgationViewModel
import com.addzero.events.EventBusConsumer
import com.addzero.ui.auth.LoginScreen
import com.addzero.ui.infra.MainLayout
import com.addzero.ui.infra.model.favorite.FavoriteTabsViewModel
import com.addzero.viewmodel.SysRouteViewModel
import com.addzero.ui.infra.model.navigation.RecentTabsManagerViewModel
import com.addzero.ui.infra.theme.AppThemes
import com.addzero.ui.infra.theme.FollowSystemTheme
import com.addzero.ui.infra.theme.ThemeViewModel
import com.addzero.viewmodel.ChatViewModel
import com.addzero.viewmodel.LoginViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.startKoin
import org.koin.ksp.generated.defaultModule


@Composable
fun App() {
    InitKoin()
//    emitEventBus()
    EventBusConsumer()
    val themeViewModel = koinViewModel<ThemeViewModel>()
    // 已登录时渲染主界面
    val currentTheme = themeViewModel.currentTheme
    val colorScheme = AppThemes.getColorScheme(currentTheme)

    FollowSystemTheme(colorScheme = colorScheme) {
//        GradientThemeWrapper(themeType = currentTheme) {
        MainLayoutWithLogin()
//        MainLayout()
        ToastListener()
//        }
    }
}

@Composable
private fun MainLayoutWithLogin() {
    val loginViewModel = koinViewModel<LoginViewModel>()
    if (loginViewModel.currentToken == null
    ) {
        LoginScreen()
    } else {
        with(NavgationViewModel) {
            with(koinViewModel<RecentTabsManagerViewModel>()) {
                with(koinViewModel<ThemeViewModel>()) {
                    with(koinViewModel<ChatViewModel>()) {
                        with(koinViewModel<SysRouteViewModel>()) {
                            with(koinViewModel<FavoriteTabsViewModel>()) {
                                MainLayout()
                            }
                        }
                    }

                }


            }

        }

    }
}

@Composable
private fun InitKoin() {
    startKoin {
        printLogger()
        modules(
            defaultModule
        )
    }
}

