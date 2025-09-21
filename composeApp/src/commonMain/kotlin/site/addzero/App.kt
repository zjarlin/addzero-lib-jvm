package site.addzero

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.dsl.koinApplication
import org.koin.ksp.generated.defaultModule
import site.addzero.di.NavgationViewModel
import site.addzero.ioc.generated.IocContainer
import site.addzero.ui.auth.LoginScreen
import site.addzero.ui.infra.MainLayout
import site.addzero.ui.infra.model.favorite.FavoriteTabsViewModel
import site.addzero.ui.infra.model.navigation.RecentTabsManagerViewModel
import site.addzero.ui.infra.theme.AppThemes
import site.addzero.ui.infra.theme.ThemeViewModel
import site.addzero.viewmodel.ChatViewModel
import site.addzero.viewmodel.LoginViewModel
import site.addzero.viewmodel.SysRouteViewModel
import kotlin.getValue


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
    IocContainer.iocAllStart()
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

