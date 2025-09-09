package com.addzero

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.addzero.component.toast.ToastListener
import com.addzero.di.NavgationViewModel
import com.addzero.events.EventBusConsumer
import com.addzero.ui.auth.LoginScreen
import com.addzero.ui.infra.MainLayout
import com.addzero.ui.infra.model.favorite.FavoriteTabsViewModel
import com.addzero.ui.infra.model.navigation.RecentTabsManagerViewModel
import com.addzero.ui.infra.theme.AppThemes
import com.addzero.ui.infra.theme.ThemeViewModel
import com.addzero.viewmodel.ChatViewModel
import com.addzero.viewmodel.LoginViewModel
import com.addzero.viewmodel.SysRouteViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.startKoin
import org.koin.ksp.generated.defaultModule


@Composable
fun App() {
    InitKoin()

    MaterialTheme(
        colorScheme = AppThemes.getColorScheme(koinViewModel<ThemeViewModel>().currentTheme),
        shapes = MaterialTheme.shapes,
        typography = MaterialTheme.typography,
        content = {
            MainLayoutWithLogin()
        },
    )
    val listOf = listOf(
        @Composable { EventBusConsumer() },
        @Composable { ToastListener() },
    )

    listOf.forEach { it() }
}


@Composable
private fun MainLayoutWithLogin() {
    val loginViewModel = koinViewModel<LoginViewModel>()
    if (loginViewModel.currentToken == null) {
        LoginScreen()
    } else {
        context(
            NavgationViewModel,
            koinViewModel<RecentTabsManagerViewModel>(),
            koinViewModel<ThemeViewModel>(),
            koinViewModel<ChatViewModel>(),
            koinViewModel<SysRouteViewModel>(),
            koinViewModel<FavoriteTabsViewModel>()

        ) {
            MainLayout()
        }

    }
}

@Composable
private fun InitKoin() {
    startKoin {
        printLogger()
        modules(
            defaultModule,
        )
    }
}

