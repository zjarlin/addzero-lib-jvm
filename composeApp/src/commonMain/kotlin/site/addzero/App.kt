package site.addzero

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.startKoin
import org.koin.ksp.generated.defaultModule
import site.addzero.autoinit.annotation.AutoInit
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

@AutoInit
fun Hello(): Unit {
    println("hello")

}

@AutoInit
fun hello1(): Unit {
    println("hello1")

}

@AutoInit
suspend fun hello2() = withContext(Dispatchers.Main) {
    println("hello2")
}

@AutoInit
class Hello6 {

}

@AutoInit
object Hello5 {

}

fun main() {
    val listOf = listOf(

    Hello5
    ,Hello6()
    )
    listOf.forEach {
       it
    }
}


@AutoInit
suspend fun hello3() = {
    println("hello3")
}


@AutoInit
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

