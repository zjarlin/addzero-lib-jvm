package site.addzero.appsidebar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.serialization.Serializable
import org.koin.compose.getKoin
import org.koin.compose.koinInject

/**
 * 侧栏渲染器 SPI。
 */
interface AppSidebarRenderer {
    @Composable
    fun Render(
        modifier: Modifier = Modifier,
    )
}

/**
 * 从 Koin 解析并渲染当前侧栏实现。
 */
@Composable
fun RenderAppSidebar(
    modifier: Modifier = Modifier,
    renderer: AppSidebarRenderer = koinInject(),
) {
    renderer.Render(modifier)
}

/**
 * 工作台骨架渲染器的壳层枚举。
 */
@Serializable
enum class AppSidebarScaffoldShell {
    Workbench,
    AdminWorkbench,
}

/**
 * 侧栏骨架渲染器 SPI。
 */
interface AppSidebarScaffoldRenderer {
    val shell: AppSidebarScaffoldShell

    @Composable
    fun Render(
        modifier: Modifier = Modifier,
    )
}

/**
 * 按壳层类型选择并渲染对应的工作台骨架。
 */
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

/**
 * 通用工作台骨架渲染器 SPI。
 */
interface WorkbenchScaffoldRenderer {
    @Composable
    fun Render(
        modifier: Modifier = Modifier,
    )
}

/**
 * 从 Koin 解析并渲染通用工作台骨架。
 */
@Composable
fun RenderWorkbenchScaffold(
    modifier: Modifier = Modifier,
    renderer: WorkbenchScaffoldRenderer = koinInject(),
) {
    renderer.Render(modifier)
}

/**
 * 后台工作台骨架渲染器 SPI。
 */
interface AdminWorkbenchScaffoldRenderer {
    @Composable
    fun Render(
        modifier: Modifier = Modifier,
    )
}

/**
 * 从 Koin 解析并渲染后台工作台骨架。
 */
@Composable
fun RenderAdminWorkbenchScaffold(
    modifier: Modifier = Modifier,
    renderer: AdminWorkbenchScaffoldRenderer = koinInject(),
) {
    renderer.Render(modifier)
}
