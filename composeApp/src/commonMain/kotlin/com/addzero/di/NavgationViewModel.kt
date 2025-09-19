package site.addzero.di

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import site.addzero.generated.RouteTable
import site.addzero.settings.SettingContext4Compose

object NavgationViewModel {
    private var _navController: NavHostController? = null

    @Composable
    fun Initialize(controller: NavHostController) {
        val navController = getNavController()
        NavHost(
            navController = navController,
            startDestination = SettingContext4Compose.HOME_SCREEN,
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            // 动态生成导航目标
            RouteTable.allRoutes.forEach { (route, content) ->
                composable(route) {
                    content()
                }
            }
        }
        _navController = controller
    }

    fun navigate(key: String) {
        _navController?.navigate(key)
    }

    fun goBack() {
        _navController?.popBackStack()
    }

    @Composable
    fun getNavController(): NavHostController {
        return _navController ?: rememberNavController()
    }
}
