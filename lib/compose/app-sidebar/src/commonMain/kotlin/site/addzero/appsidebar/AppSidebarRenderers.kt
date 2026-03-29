package site.addzero.appsidebar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.serialization.Serializable
import org.koin.compose.getKoin
import org.koin.compose.koinInject

interface AppSidebarRenderer {
    @Composable
    fun Render(
        modifier: Modifier = Modifier,
    )
}

@Composable
fun RenderAppSidebar(
    modifier: Modifier = Modifier,
    renderer: AppSidebarRenderer = koinInject(),
) {
    renderer.Render(modifier)
}

@Serializable
enum class AppSidebarScaffoldShell {
    Workbench,
    AdminWorkbench,
}

/**
 * @author zjarlin
 * @date 2026/03/28
 * @constructor 创建[AppSidebarScaffoldRenderer]
 */
interface AppSidebarScaffoldRenderer {
    val shell: AppSidebarScaffoldShell

    @Composable
    fun Render(
        modifier: Modifier = Modifier,
    )
}

@Composable
fun RenderAppSidebarScaffold(
    shell: AppSidebarScaffoldShell,
    modifier: Modifier = Modifier,
) {
    val koin = getKoin()
    val rendererByShell = remember(koin) {
        koin.getAll<AppSidebarScaffoldRenderer>().associateBy { renderer ->
            renderer.shell
        }
    }
    val renderer = rendererByShell[shell]
        ?: error("未找到 $shell 对应的 AppSidebarScaffoldRenderer。")
    renderer.Render(modifier)
}

interface WorkbenchScaffoldRenderer {
    @Composable
    fun Render(
        modifier: Modifier = Modifier,
    )
}

@Composable
fun RenderWorkbenchScaffold(
    modifier: Modifier = Modifier,
    renderer: WorkbenchScaffoldRenderer = koinInject(),
) {
    renderer.Render(modifier)
}

interface AdminWorkbenchScaffoldRenderer {
    @Composable
    fun Render(
        modifier: Modifier = Modifier,
    )
}

@Composable
fun RenderAdminWorkbenchScaffold(
    modifier: Modifier = Modifier,
    renderer: AdminWorkbenchScaffoldRenderer = koinInject(),
) {
    renderer.Render(modifier)
}
