package site.addzero

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.startKoin
import org.koin.ksp.generated.defaultModule
import site.addzero.di.NavgationViewModel
import site.addzero.ioc.annotation.Bean
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
    val scope = rememberCoroutineScope()

//    val listOf = listOf(
//        @Composable { EventBusConsumer() },
//        @Composable { AddToastListener() },
//    )
//
//    listOf.forEach { it() }


    scope.launch {
        IocContainer.iocAllStart()
    }

}

@Bean
fun Hello(): Unit {
    println("hello")

}

@Bean
fun hello1(): Unit {
    println("hello1")

}

@Composable
@Bean
fun TestText(themeViewModel: ThemeViewModel = koinInject()): Unit {
    Text("TestText")
}


@Bean
suspend fun hello2() = withContext(Dispatchers.Main) {
    println("hello2")
}

@Bean
suspend fun hello3() = {
    println("hello3")
}


@Bean
class Hello6 {

}

@Bean
object Hello5 {

}


@Bean
@Composable
fun Hello4(menuViewModel: ChatViewModel = koinInject<ChatViewModel>()) {
    println("hello3")
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

