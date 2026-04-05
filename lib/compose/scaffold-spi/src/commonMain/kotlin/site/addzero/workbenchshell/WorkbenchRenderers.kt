package site.addzero.workbenchshell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import org.koin.compose.koinInject
import site.addzero.appsidebar.WorkbenchScaffold
import site.addzero.appsidebar.rememberWorkbenchScaffoldState
import site.addzero.appsidebar.spi.ScaffoldConfigSpi
import site.addzero.appsidebar.workbenchScaffoldSlots
import site.addzero.workbenchshell.spi.content.ContentRender
import site.addzero.workbenchshell.spi.header.HeaderRender
import site.addzero.workbenchshell.spi.sidebar.SidebarRender

@Composable
fun RenderWorkbenchScaffold(
  modifier: Modifier = Modifier,
  scaffoldConfigSpi: ScaffoldConfigSpi,
  detail: (@Composable BoxScope.() -> Unit)? = null,
  sidebarRender: SidebarRender = koinInject(),
  headerRenderer: HeaderRender = koinInject(),
  contentRenderer: ContentRender = koinInject(),
) {
    val state = rememberWorkbenchScaffoldState(scaffoldConfigSpi.defaultSidebarRatio)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier.fillMaxSize().workbenchBackdrop(),
        ) {
            WorkbenchScaffold(
                modifier = Modifier.fillMaxSize(),
                state = state,
                config = scaffoldConfigSpi,
                slots = workbenchScaffoldSlots(
                    contentHeader = {
                        headerRenderer.Render(
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                    detail = detail,
                ),
                sidebar = {
                    sidebarRender.Render(
                        modifier = Modifier.fillMaxSize(),
                    )
                },
                content = {
                    contentRenderer.Render(
                        modifier = Modifier.fillMaxSize(),
                    )
                },
            )
        }
    }
}

private fun Modifier.workbenchBackdrop(): Modifier {
    return background(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF08111C),
                Color(0xFF091725),
                Color(0xFF06101A),
            ),
        ),
    ).background(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF245B8F).copy(alpha = 0.26f),
                Color.Transparent,
            ),
            radius = 520f,
        ),
    )
}
