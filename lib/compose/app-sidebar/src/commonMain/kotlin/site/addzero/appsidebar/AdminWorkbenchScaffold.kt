package site.addzero.appsidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import site.addzero.appsidebar.spi.ScaffoldConfigSpi
import site.addzero.appsidebar.spi.sidebarResizeConfig

/**
 * 后台页面头部所需的标题信息。
 */
interface AdminWorkbenchPageConfig {
    val breadcrumb: List<String>
        get() = emptyList()
    val pageTitle: String
    val pageSubtitle: String?
        get() = null
}

/**
 * 后台工作台骨架配置。
 */
interface AdminWorkbenchConfigSpi : ScaffoldConfigSpi {
    val brandLabel
        get() = "Addzero Admin"
    val welcomeLabel
        get() = "欢迎进入后台工作台"
    val isDarkTheme: Boolean?
        get() = null
    val sidebarVisible
        get() = true
    val onSidebarToggle: (() -> Unit)?
        get() = null
}

/**
 * 后台工作台可定制插槽。
 */
interface AdminWorkbenchSlots {
    val brandContent: (@Composable RowScope.() -> Unit)?
        get() = null
    val pageActions: @Composable RowScope.() -> Unit
        get() = {}
    val showContentHeader
        get() = true
    val titleContent: (@Composable ColumnScope.() -> Unit)?
        get() = null
    val detail: (@Composable BoxScope.() -> Unit)?
        get() = null
    val topBarActions: (@Composable RowScope.() -> Unit)?
        get() = null
}

/**
 * 快速创建后台页面头部配置。
 */
fun adminWorkbenchPageConfig(
    pageTitle: String,
    breadcrumb: List<String> = emptyList(),
    pageSubtitle: String? = null,
): AdminWorkbenchPageConfig = DefaultAdminWorkbenchPageConfig(
    breadcrumb = breadcrumb,
    pageTitle = pageTitle,
    pageSubtitle = pageSubtitle,
)

/**
 * 快速创建后台工作台骨架配置。
 */
fun adminWorkbenchConfig(
    brandLabel: String = "Addzero Admin",
    welcomeLabel: String = "欢迎进入后台工作台",
    defaultSidebarRatio: Float = 0.22f,
    minSidebarWidth: Dp = 248.dp,
    maxSidebarWidth: Dp = 360.dp,
    detailWidth: Dp = 320.dp,
    outerPadding: PaddingValues = PaddingValues(0.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    detailPadding: PaddingValues = PaddingValues(0.dp),
    isDarkTheme: Boolean? = null,
    sidebarVisible: Boolean = true,
    onSidebarToggle: (() -> Unit)? = null,
): AdminWorkbenchConfigSpi = DefaultAdminWorkbenchConfigSpi(
    brandLabel = brandLabel,
    welcomeLabel = welcomeLabel,
    defaultSidebarRatio = defaultSidebarRatio,
    minSidebarWidth = minSidebarWidth,
    maxSidebarWidth = maxSidebarWidth,
    detailWidth = detailWidth,
    outerPadding = outerPadding,
    contentPadding = contentPadding,
    detailPadding = detailPadding,
    isDarkTheme = isDarkTheme,
    sidebarVisible = sidebarVisible,
    onSidebarToggle = onSidebarToggle,
)

/**
 * 快速创建后台工作台插槽集合。
 */
fun adminWorkbenchSlots(
    brandContent: (@Composable RowScope.() -> Unit)? = null,
    pageActions: @Composable RowScope.() -> Unit = {},
    showContentHeader: Boolean = true,
    titleContent: (@Composable ColumnScope.() -> Unit)? = null,
    detail: (@Composable BoxScope.() -> Unit)? = null,
    topBarActions: (@Composable RowScope.() -> Unit)? = null,
): AdminWorkbenchSlots = DefaultAdminWorkbenchSlots(
    brandContent = brandContent,
    pageActions = pageActions,
    showContentHeader = showContentHeader,
    titleContent = titleContent,
    detail = detail,
    topBarActions = topBarActions,
)

/**
 * 后台管理工作台骨架。
 *
 * 统一封装顶部全局工具条、页面标题区、左侧栏、主内容区和可选详情区。
 */
@Composable
fun AdminWorkbenchScaffold(
  sidebar: @Composable BoxScope.() -> Unit,
  content: @Composable BoxScope.() -> Unit,
  page: AdminWorkbenchPageConfig,
  modifier: Modifier = Modifier,
  state: WorkbenchScaffoldState? = null,
  config: AdminWorkbenchConfigSpi = adminWorkbenchConfig(),
  slots: AdminWorkbenchSlots = adminWorkbenchSlots(),
) {
    val scaffoldState = state ?: rememberWorkbenchScaffoldState(config.defaultSidebarRatio)
    val windowFrame = LocalWorkbenchWindowFrame.current
    val topBarDecorator = LocalWorkbenchTopBarDecorator.current
    val workbenchColors = rememberAdminWorkbenchColors(
        isDarkTheme = config.isDarkTheme,
    )

    CompositionLocalProvider(
        LocalAdminWorkbenchColors provides workbenchColors,
    ) {
        Column(
            modifier = modifier.fillMaxSize().background(AdminWorkbenchTokens.pageBackground),
        ) {
            topBarDecorator.Decorate(
                modifier = Modifier.fillMaxWidth(),
            ) {
                AdminWorkbenchGlobalBar(
                    config = config,
                    slots = slots,
                    topBarHeight = windowFrame.topBarHeight,
                    leadingInset = windowFrame.leadingInset,
                    trailingInset = windowFrame.trailingInset,
                    immersiveTopBar = windowFrame.immersiveTopBar,
                )
            }

            WorkbenchScaffold(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                sidebar = sidebar,
                content = content,
                state = scaffoldState,
                config = config,
                slots = workbenchScaffoldSlots(
                    contentHeader = if (slots.showContentHeader) {
                        {
                            AdminWorkbenchContentHeader(
                                page = page,
                                slots = slots,
                            )
                        }
                    } else {
                        null
                    },
                    detail = slots.detail,
                ),
                decor = workbenchScaffoldDecor(
                    sidebarContainerModifier = Modifier.fillMaxHeight()
                        .background(AdminWorkbenchTokens.sidebarBackground),
                    mainContainerModifier = Modifier.background(AdminWorkbenchTokens.pageBackground),
                    headerContainerModifier = Modifier.background(AdminWorkbenchTokens.headerBackground),
                    detailContainerModifier = Modifier.background(AdminWorkbenchTokens.detailBackground),
                    resizeConfig = sidebarResizeConfig(
                        dividerColor = AdminWorkbenchTokens.dividerColor,
                        thumbColor = AdminWorkbenchTokens.resizeThumbColor,
                        thumbBorderColor = AdminWorkbenchTokens.resizeThumbBorder,
                    ),
                ),
            )
        }
    }
}


@Composable
private fun RowScope.AdminWorkbenchContentHeader(
    page: AdminWorkbenchPageConfig,
    slots: AdminWorkbenchSlots,
) {
    val titleContent = slots.titleContent
    if (titleContent != null) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = titleContent,
        )
    } else {
        AdminWorkbenchTitleBlock(
            page = page,
            modifier = Modifier.weight(1f),
        )
    }

    Row(
        modifier = Modifier.widthIn(max = 440.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = slots.pageActions,
    )
}

@Composable
private fun AdminWorkbenchTitleBlock(
    page: AdminWorkbenchPageConfig,
    modifier: Modifier = Modifier,
) {
    val pageSubtitle = page.pageSubtitle
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (page.breadcrumb.isNotEmpty()) {
            Text(
                text = page.breadcrumb.joinToString(" / "),
                color = AdminWorkbenchTokens.headerTextMuted,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Text(
            text = page.pageTitle,
            color = AdminWorkbenchTokens.headerTextPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
        )
        if (pageSubtitle != null) {
            Text(
                text = pageSubtitle,
                color = AdminWorkbenchTokens.headerTextMuted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Immutable
@Serializable
private data class DefaultAdminWorkbenchPageConfig(
    override val breadcrumb: List<String>,
    override val pageTitle: String,
    override val pageSubtitle: String?,
) : AdminWorkbenchPageConfig

@Immutable
private data class DefaultAdminWorkbenchConfigSpi(
    override val brandLabel: String,
    override val welcomeLabel: String,
    override val defaultSidebarRatio: Float,
    override val minSidebarWidth: Dp,
    override val maxSidebarWidth: Dp,
    override val detailWidth: Dp,
    override val outerPadding: PaddingValues,
    override val contentPadding: PaddingValues,
    override val detailPadding: PaddingValues,
    override val isDarkTheme: Boolean?,
    override val sidebarVisible: Boolean,
    override val onSidebarToggle: (() -> Unit)?,
) : AdminWorkbenchConfigSpi {
    override val contentHeaderScrollable
        get() = false
}

private data class DefaultAdminWorkbenchSlots(
    override val brandContent: (@Composable RowScope.() -> Unit)?,
    override val pageActions: @Composable RowScope.() -> Unit,
    override val showContentHeader: Boolean,
    override val titleContent: (@Composable ColumnScope.() -> Unit)?,
    override val detail: (@Composable BoxScope.() -> Unit)?,
    override val topBarActions: (@Composable RowScope.() -> Unit)?,
) : AdminWorkbenchSlots

@Immutable
private data class AdminWorkbenchColors(
    val topBarBackground: Color,
    val topBarTextPrimary: Color,
    val topBarTextSecondary: Color,
    val pageBackground: Color,
    val sidebarBackground: Color,
    val headerBackground: Color,
    val detailBackground: Color,
    val dividerColor: Color,
    val resizeThumbColor: Color,
    val resizeThumbBorder: Color,
    val buttonBackground: Color,
    val buttonBorder: Color,
    val highlightedBackground: Color,
    val highlightedBorder: Color,
    val highlightedTextPrimary: Color,
    val badgeBackground: Color,
    val avatarBackground: Color,
    val avatarHalo: Color,
    val textPrimary: Color,
    val headerTextPrimary: Color,
    val headerTextMuted: Color,
    val brandPlateBackground: Color,
    val brandPrimaryDot: Color,
    val brandSecondaryDot: Color,
)

private val LocalAdminWorkbenchColors = staticCompositionLocalOf {
    AdminWorkbenchColors(
        topBarBackground = Color(0xFF2F78F6),
        topBarTextPrimary = Color.White,
        topBarTextSecondary = Color.White.copy(alpha = 0.84f),
        pageBackground = Color(0xFFF3F6FA),
        sidebarBackground = Color.White,
        headerBackground = Color.White,
        detailBackground = Color.White,
        dividerColor = Color(0xFFE4EAF2),
        resizeThumbColor = Color(0xFF7EB9F5),
        resizeThumbBorder = Color(0xFFB7D9FB),
        buttonBackground = Color.White.copy(alpha = 0.12f),
        buttonBorder = Color.White.copy(alpha = 0.18f),
        highlightedBackground = Color.White.copy(alpha = 0.18f),
        highlightedBorder = Color.White.copy(alpha = 0.26f),
        highlightedTextPrimary = Color(0xFF0B1320),
        badgeBackground = Color(0xFFFF5A5F),
        avatarBackground = Color.White.copy(alpha = 0.20f),
        avatarHalo = Color.White.copy(alpha = 0.18f),
        textPrimary = Color.White.copy(alpha = 0.96f),
        headerTextPrimary = Color(0xFF1F2A37),
        headerTextMuted = Color(0xFF6B7280),
        brandPlateBackground = Color.White.copy(alpha = 0.18f),
        brandPrimaryDot = Color.White,
        brandSecondaryDot = Color(0xFFFF6B6B),
    )
}

@Composable
private fun rememberAdminWorkbenchColors(
    isDarkTheme: Boolean?,
): AdminWorkbenchColors {
    val colorScheme = MaterialTheme.colorScheme
    val darkThemeEnabled = isDarkTheme ?: (colorScheme.background.luminance() < 0.5f)
    return remember(colorScheme, darkThemeEnabled) {
        if (darkThemeEnabled) {
            AdminWorkbenchColors(
                topBarBackground = Color(0xFF0A1724),
                topBarTextPrimary = colorScheme.onSurface,
                topBarTextSecondary = colorScheme.onSurfaceVariant.copy(alpha = 0.84f),
                pageBackground = colorScheme.background,
                sidebarBackground = Color(0xFF0B1826),
                headerBackground = Color(0xFF0E1D2C),
                detailBackground = Color(0xFF0E1B29),
                dividerColor = colorScheme.outline.copy(alpha = 0.34f),
                resizeThumbColor = colorScheme.primary.copy(alpha = 0.42f),
                resizeThumbBorder = colorScheme.primary.copy(alpha = 0.24f),
                buttonBackground = Color.White.copy(alpha = 0.045f),
                buttonBorder = Color.White.copy(alpha = 0.10f),
                highlightedBackground = colorScheme.primary.copy(alpha = 0.16f),
                highlightedBorder = colorScheme.primary.copy(alpha = 0.28f),
                highlightedTextPrimary = colorScheme.onSurface,
                badgeBackground = colorScheme.errorContainer,
                avatarBackground = colorScheme.primary.copy(alpha = 0.14f),
                avatarHalo = colorScheme.primary.copy(alpha = 0.22f),
                textPrimary = colorScheme.onSurface,
                headerTextPrimary = colorScheme.onSurface,
                headerTextMuted = colorScheme.onSurfaceVariant,
                brandPlateBackground = Color.White.copy(alpha = 0.08f),
                brandPrimaryDot = colorScheme.onPrimaryContainer,
                brandSecondaryDot = Color(0xFF7BD7C6),
            )
        } else {
            AdminWorkbenchColors(
                topBarBackground = colorScheme.surface,
                topBarTextPrimary = colorScheme.onSurface,
                topBarTextSecondary = colorScheme.onSurfaceVariant.copy(alpha = 0.84f),
                pageBackground = colorScheme.background,
                sidebarBackground = colorScheme.surface,
                headerBackground = colorScheme.surface,
                detailBackground = colorScheme.surface,
                dividerColor = colorScheme.outline.copy(alpha = 0.22f),
                resizeThumbColor = colorScheme.primary.copy(alpha = 0.42f),
                resizeThumbBorder = colorScheme.primary.copy(alpha = 0.24f),
                buttonBackground = colorScheme.surface,
                buttonBorder = colorScheme.outlineVariant.copy(alpha = 0.96f),
                highlightedBackground = Color(0xFF111827),
                highlightedBorder = Color(0xFF111827),
                highlightedTextPrimary = Color.White,
                badgeBackground = Color(0xFFFF5A5F),
                avatarBackground = colorScheme.primary.copy(alpha = 0.10f),
                avatarHalo = colorScheme.primary.copy(alpha = 0.14f),
                textPrimary = colorScheme.onSurface.copy(alpha = 0.96f),
                headerTextPrimary = colorScheme.onSurface,
                headerTextMuted = colorScheme.onSurfaceVariant,
                brandPlateBackground = colorScheme.surfaceVariant.copy(alpha = 0.86f),
                brandPrimaryDot = colorScheme.primary,
                brandSecondaryDot = Color(0xFFFF6B6B),
            )
        }
    }
}

internal object AdminWorkbenchTokens {
    val topBarBackground
        @Composable get() = LocalAdminWorkbenchColors.current.topBarBackground
    val topBarTextPrimary
        @Composable get() = LocalAdminWorkbenchColors.current.topBarTextPrimary
    val topBarTextSecondary
        @Composable get() = LocalAdminWorkbenchColors.current.topBarTextSecondary
    val pageBackground
        @Composable get() = LocalAdminWorkbenchColors.current.pageBackground
    val sidebarBackground
        @Composable get() = LocalAdminWorkbenchColors.current.sidebarBackground
    val headerBackground
        @Composable get() = LocalAdminWorkbenchColors.current.headerBackground
    val detailBackground
        @Composable get() = LocalAdminWorkbenchColors.current.detailBackground
    val dividerColor
        @Composable get() = LocalAdminWorkbenchColors.current.dividerColor
    val resizeThumbColor
        @Composable get() = LocalAdminWorkbenchColors.current.resizeThumbColor
    val resizeThumbBorder
        @Composable get() = LocalAdminWorkbenchColors.current.resizeThumbBorder
    val buttonBackground
        @Composable get() = LocalAdminWorkbenchColors.current.buttonBackground
    val buttonBorder
        @Composable get() = LocalAdminWorkbenchColors.current.buttonBorder
    val highlightedBackground
        @Composable get() = LocalAdminWorkbenchColors.current.highlightedBackground
    val highlightedBorder
        @Composable get() = LocalAdminWorkbenchColors.current.highlightedBorder
    val highlightedTextPrimary
        @Composable get() = LocalAdminWorkbenchColors.current.highlightedTextPrimary
    val badgeBackground
        @Composable get() = LocalAdminWorkbenchColors.current.badgeBackground
    val avatarBackground
        @Composable get() = LocalAdminWorkbenchColors.current.avatarBackground
    val avatarHalo
        @Composable get() = LocalAdminWorkbenchColors.current.avatarHalo
    val textPrimary
        @Composable get() = LocalAdminWorkbenchColors.current.textPrimary
    val headerTextPrimary
        @Composable get() = LocalAdminWorkbenchColors.current.headerTextPrimary
    val headerTextMuted
        @Composable get() = LocalAdminWorkbenchColors.current.headerTextMuted
    val brandPlateBackground
        @Composable get() = LocalAdminWorkbenchColors.current.brandPlateBackground
    val brandPrimaryDot
        @Composable get() = LocalAdminWorkbenchColors.current.brandPrimaryDot
    val brandSecondaryDot
        @Composable get() = LocalAdminWorkbenchColors.current.brandSecondaryDot
}
