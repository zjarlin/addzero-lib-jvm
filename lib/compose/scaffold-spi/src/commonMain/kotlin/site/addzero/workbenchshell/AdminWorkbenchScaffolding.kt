package site.addzero.workbenchshell

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import site.addzero.appsidebar.AdminWorkbenchScaffold
import site.addzero.appsidebar.adminWorkbenchConfig
import site.addzero.appsidebar.adminWorkbenchPageConfig
import site.addzero.appsidebar.adminWorkbenchSlots
import site.addzero.workbenchshell.spi.scaffolding.ScaffoldingSpi

@Composable
fun RenderAdminScaffolding(
    scaffolding: ScaffoldingSpi,
    modifier: Modifier = Modifier,
    sidebarVisible: Boolean = true,
    onSidebarToggle: (() -> Unit)? = null,
    defaultSidebarRatio: Float = 0.22f,
    minSidebarWidth: Dp = 248.dp,
    maxSidebarWidth: Dp = 360.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    detailPadding: PaddingValues = PaddingValues(0.dp),
) {
    AdminWorkbenchScaffold(
        sidebar = {
            scaffolding.RenderSidebar(
                modifier = Modifier.fillMaxSize(),
            )
        },
        content = {
            scaffolding.RenderContent(
                modifier = Modifier.fillMaxSize(),
            )
        },
        page = adminWorkbenchPageConfig(pageTitle = scaffolding.pageTitle),
        modifier = modifier.fillMaxSize(),
        config = adminWorkbenchConfig(
            brandLabel = scaffolding.brandLabel,
            welcomeLabel = scaffolding.welcomeLabel,
            defaultSidebarRatio = if (sidebarVisible) defaultSidebarRatio else 0f,
            minSidebarWidth = if (sidebarVisible) minSidebarWidth else 0.dp,
            maxSidebarWidth = if (sidebarVisible) maxSidebarWidth else 0.dp,
            contentPadding = contentPadding,
            detailPadding = detailPadding,
            sidebarVisible = sidebarVisible,
            onSidebarToggle = onSidebarToggle,
        ),
        slots = adminWorkbenchSlots(
            brandContent = {
                // [旗帜位 + 航标位] 左上角先放品牌门头，再接顶部场景切换。
                with(scaffolding) {
                    RenderBrand()
                    RenderHeader(
                        modifier = Modifier.weight(1f),
                    )
                }
            },
            showContentHeader = false,
            topBarActions = {
                // [驾驶舱右手边] 顶栏最右边的全局动作区。
                with(scaffolding) {
                    RenderTopBarActions()
                }
            },
        ),
    )
}
