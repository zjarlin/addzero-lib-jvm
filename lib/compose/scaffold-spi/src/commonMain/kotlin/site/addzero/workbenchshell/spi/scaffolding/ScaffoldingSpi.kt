package site.addzero.workbenchshell.spi.scaffolding

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import site.addzero.workbenchshell.spi.content.ContentRender
import site.addzero.workbenchshell.spi.header.HeaderRender
import site.addzero.workbenchshell.spi.sidebar.SidebarRender

interface ScaffoldingSpi {
    val pageTitle
        get() = "Workbench"

    val brandLabel
        get() = "Addzero Workbench"

    val welcomeLabel
        get() = ""

    // [左翼] 页面左侧整条导航带，像建筑的左翼走廊。
    @Composable
    fun RenderSidebar(
        modifier: Modifier,
    ) {
        koinInject<SidebarRender>().Render(modifier)
    }

    // [航标位] 顶栏左上到中间这一带，像驾驶舱前方的航标区。
    @Composable
    fun RenderHeader(
        modifier: Modifier,
    ) {
        koinInject<HeaderRender>().Render(modifier)
    }

    // [中庭] 页面中间最大的一块内容区，像建筑中庭，业务页面主要在这里展开。
    @Composable
    fun RenderContent(
        modifier: Modifier,
    ) {
        koinInject<ContentRender>().Render(modifier)
    }

    // [旗帜位] 顶栏最左边的一小块，像门头招牌，通常放品牌名或产品身份。
    @Composable
    fun RowScope.RenderBrand() {}

    // [驾驶舱右手边] 顶栏最右侧的操作带，像驾驶位右手边按钮区，通常放全局顶栏动作。
    @Composable
    fun RowScope.RenderTopBarActions() {}

    // [浮层位] 悬在主页面上方的一层，像舞台上方吊着的幕布，通常放弹窗、助手面板、全局覆盖层。
    @Composable
    fun RenderOverlay() {}
}
