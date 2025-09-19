package site.addzero

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import site.addzero.component.toast.ToastListener
import site.addzero.di.NavgationViewModel
import site.addzero.events.EventBusConsumer
import site.addzero.ui.auth.LoginScreen
import site.addzero.ui.infra.MainLayout
import site.addzero.ui.infra.model.favorite.FavoriteTabsViewModel
import site.addzero.ui.infra.model.navigation.RecentTabsManagerViewModel
import site.addzero.ui.infra.theme.AppThemes
import site.addzero.ui.infra.theme.ThemeViewModel
import site.addzero.viewmodel.ChatViewModel
import site.addzero.viewmodel.LoginViewModel
import site.addzero.viewmodel.SysRouteViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.startKoin
import org.koin.ksp.generated.defaultModule


@Composable
fun App() {
    initKoin()

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

//@Composable
private fun initKoin() {
    startKoin {
        printLogger()
        modules(
            defaultModule,
        )
    }
}

