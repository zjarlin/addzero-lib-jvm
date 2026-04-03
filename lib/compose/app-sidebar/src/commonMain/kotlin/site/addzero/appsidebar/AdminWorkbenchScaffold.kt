package site.addzero.appsidebar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Icon
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
import site.addzero.appsidebar.spi.ScaffoldConfig
import site.addzero.appsidebar.spi.scaffoldConfig
import site.addzero.appsidebar.spi.sidebarResizeConfig

interface AdminWorkbenchPageConfig {
    val breadcrumb: List<String>
        get() = emptyList()
    val pageTitle: String
    val pageSubtitle: String?
        get() = null
}

interface AdminWorkbenchConfig : ScaffoldConfig {
    val brandLabel: String
        get() = "Addzero Admin"
    val welcomeLabel: String
        get() = "欢迎进入后台工作台"
    val githubLabel: String?
        get() = null
    val languageLabel: String?
        get() = null
    val isDarkTheme: Boolean?
        get() = null
    val notificationCount: Int?
        get() = null
    val userLabel: String?
        get() = null
}

interface AdminWorkbenchActions {
    val onGlobalSearchClick: (() -> Unit)?
        get() = null
    val onGithubClick: (() -> Unit)?
        get() = null
    val onLanguageClick: (() -> Unit)?
        get() = null
    val onThemeToggle: (() -> Unit)?
        get() = null
    val onNotificationsClick: (() -> Unit)?
        get() = null
    val onUserClick: (() -> Unit)?
        get() = null
}

interface AdminWorkbenchSlots {
    val brandContent: (@Composable RowScope.() -> Unit)?
        get() = null
    val pageActions: @Composable RowScope.() -> Unit
        get() = {}
    val showContentHeader: Boolean
        get() = true
    val titleContent: (@Composable ColumnScope.() -> Unit)?
        get() = null
    val detail: (@Composable BoxScope.() -> Unit)?
        get() = null
    val userContent: (@Composable RowScope.() -> Unit)?
        get() = null
}

fun adminWorkbenchPageConfig(
    pageTitle: String,
    breadcrumb: List<String> = emptyList(),
    pageSubtitle: String? = null,
): AdminWorkbenchPageConfig = DefaultAdminWorkbenchPageConfig(
    breadcrumb = breadcrumb,
    pageTitle = pageTitle,
    pageSubtitle = pageSubtitle,
)

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
    githubLabel: String? = null,
    languageLabel: String? = null,
    isDarkTheme: Boolean? = null,
    notificationCount: Int? = null,
    userLabel: String? = null,
): AdminWorkbenchConfig = DefaultAdminWorkbenchConfig(
    brandLabel = brandLabel,
    welcomeLabel = welcomeLabel,
    defaultSidebarRatio = defaultSidebarRatio,
    minSidebarWidth = minSidebarWidth,
    maxSidebarWidth = maxSidebarWidth,
    detailWidth = detailWidth,
    outerPadding = outerPadding,
    contentPadding = contentPadding,
    detailPadding = detailPadding,
    githubLabel = githubLabel,
    languageLabel = languageLabel,
    isDarkTheme = isDarkTheme,
    notificationCount = notificationCount,
    userLabel = userLabel,
)

fun adminWorkbenchActions(
    onGlobalSearchClick: (() -> Unit)? = null,
    onGithubClick: (() -> Unit)? = null,
    onLanguageClick: (() -> Unit)? = null,
    onThemeToggle: (() -> Unit)? = null,
    onNotificationsClick: (() -> Unit)? = null,
    onUserClick: (() -> Unit)? = null,
): AdminWorkbenchActions = DefaultAdminWorkbenchActions(
    onGlobalSearchClick = onGlobalSearchClick,
    onGithubClick = onGithubClick,
    onLanguageClick = onLanguageClick,
    onThemeToggle = onThemeToggle,
    onNotificationsClick = onNotificationsClick,
    onUserClick = onUserClick,
)

fun adminWorkbenchSlots(
    brandContent: (@Composable RowScope.() -> Unit)? = null,
    pageActions: @Composable RowScope.() -> Unit = {},
    showContentHeader: Boolean = true,
    titleContent: (@Composable ColumnScope.() -> Unit)? = null,
    detail: (@Composable BoxScope.() -> Unit)? = null,
    userContent: (@Composable RowScope.() -> Unit)? = null,
): AdminWorkbenchSlots = DefaultAdminWorkbenchSlots(
    brandContent = brandContent,
    pageActions = pageActions,
    showContentHeader = showContentHeader,
    titleContent = titleContent,
    detail = detail,
    userContent = userContent,
)

@Composable
fun AdminWorkbenchScaffold(
    sidebar: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
    page: AdminWorkbenchPageConfig,
    modifier: Modifier = Modifier,
    state: WorkbenchScaffoldState? = null,
    config: AdminWorkbenchConfig = adminWorkbenchConfig(),
    actions: AdminWorkbenchActions = adminWorkbenchActions(),
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
                    actions = actions,
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
fun WorkbenchSearchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "搜索",
) {
    WorkbenchUtilityButton(
        label = label,
        modifier = modifier,
        onClick = onClick,
    )
}

@Composable
fun WorkbenchLanguageButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WorkbenchUtilityButton(
        label = label,
        modifier = modifier,
        onClick = onClick,
    )
}

@Composable
fun WorkbenchGitHubButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WorkbenchUtilityButton(
        label = label,
        modifier = modifier,
        onClick = onClick,
    )
}

@Composable
fun WorkbenchThemeToggleButton(
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    darkLabel: String = "深色",
    lightLabel: String = "浅色",
) {
    WorkbenchUtilityButton(
        label = if (isDarkTheme) lightLabel else darkLabel,
        modifier = modifier,
        onClick = onClick,
        highlighted = true,
        leading = {
            Icon(
                imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                contentDescription = null,
                tint = AdminWorkbenchTokens.highlightedTextPrimary,
            )
        },
    )
}

@Composable
fun WorkbenchNotificationButton(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "通知",
) {
    WorkbenchUtilityButton(
        label = label,
        modifier = modifier,
        onClick = onClick,
        badge = count.toNotificationBadge(),
    )
}

@Composable
fun WorkbenchUserButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    avatarInitials: String = label.toAvatarInitials(),
) {
    WorkbenchUtilityButton(
        label = label,
        modifier = modifier,
        onClick = onClick,
        leading = {
            WorkbenchUserAvatar(
                initials = avatarInitials,
            )
        },
    )
}

@Composable
private fun AdminWorkbenchGlobalBar(
    config: AdminWorkbenchConfig,
    actions: AdminWorkbenchActions,
    slots: AdminWorkbenchSlots,
    topBarHeight: Dp,
    leadingInset: Dp,
    trailingInset: Dp,
    immersiveTopBar: Boolean,
) {
    val compactTopBar = topBarHeight <= 48.dp
    Row(
        modifier = Modifier.fillMaxWidth()
            .height(topBarHeight)
            .background(
                AdminWorkbenchTokens.topBarBackground.copy(
                    alpha = if (immersiveTopBar) 0.96f else 1f,
                ),
            )
            .padding(
                start = (if (compactTopBar) 14.dp else 18.dp) + leadingInset,
                end = (if (compactTopBar) 14.dp else 18.dp) + trailingInset,
            ),
        horizontalArrangement = Arrangement.spacedBy(if (compactTopBar) 14.dp else 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(if (compactTopBar) 10.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val brandContent = slots.brandContent
            if (brandContent != null) {
                brandContent()
            } else {
                AdminWorkbenchBrand(
                    label = config.brandLabel,
                    compact = compactTopBar,
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = config.brandLabel,
                        color = AdminWorkbenchTokens.topBarTextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                    )
                    if (config.welcomeLabel.isNotBlank()) {
                        Text(
                            text = config.welcomeLabel,
                            color = AdminWorkbenchTokens.topBarTextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }

        Row(
        modifier = Modifier.widthIn(max = if (compactTopBar) 640.dp else 720.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
            val onGlobalSearchClick = actions.onGlobalSearchClick
            val isDarkTheme = config.isDarkTheme
            val onThemeToggle = actions.onThemeToggle
            val githubLabel = config.githubLabel
            val onGithubClick = actions.onGithubClick
            val languageLabel = config.languageLabel
            val onLanguageClick = actions.onLanguageClick
            val notificationCount = config.notificationCount
            val onNotificationsClick = actions.onNotificationsClick
            val userContent = slots.userContent
            val userLabel = config.userLabel
            val onUserClick = actions.onUserClick

            if (onGlobalSearchClick != null) {
                WorkbenchSearchButton(
                    onClick = onGlobalSearchClick,
                )
            }
            if (isDarkTheme != null && onThemeToggle != null) {
                WorkbenchThemeToggleButton(
                    isDarkTheme = isDarkTheme,
                    onClick = onThemeToggle,
                )
            }
            if (!githubLabel.isNullOrBlank() && onGithubClick != null) {
                WorkbenchGitHubButton(
                    label = githubLabel,
                    onClick = onGithubClick,
                )
            }
            if (!languageLabel.isNullOrBlank() && onLanguageClick != null) {
                WorkbenchLanguageButton(
                    label = languageLabel,
                    onClick = onLanguageClick,
                )
            }
            if (notificationCount != null && onNotificationsClick != null) {
                WorkbenchNotificationButton(
                    count = notificationCount,
                    onClick = onNotificationsClick,
                )
            }
            if (userContent != null) {
                userContent.invoke(this)
            } else if (!userLabel.isNullOrBlank() && onUserClick != null) {
                WorkbenchUserButton(
                    label = userLabel,
                    onClick = onUserClick,
                )
            }
        }
    }
}

@Composable
private fun AdminWorkbenchBrand(
    label: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val brandPrimaryDot = AdminWorkbenchTokens.brandPrimaryDot
    val brandSecondaryDot = AdminWorkbenchTokens.brandSecondaryDot

    Box(
        modifier = modifier.size(if (compact) 24.dp else 28.dp)
            .background(
                color = AdminWorkbenchTokens.brandPlateBackground,
                shape = RoundedCornerShape(if (compact) 8.dp else 9.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier.size(if (compact) 15.dp else 18.dp),
        ) {
            drawCircle(
                color = brandPrimaryDot,
                radius = size.minDimension * 0.20f,
                center = center.copy(x = size.width * 0.36f),
            )
            drawCircle(
                color = brandSecondaryDot,
                radius = size.minDimension * 0.20f,
                center = center.copy(x = size.width * 0.64f),
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

@Composable
private fun WorkbenchUtilityButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    highlighted: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
) {
    val contentColor = if (highlighted) {
        AdminWorkbenchTokens.highlightedTextPrimary
    } else {
        AdminWorkbenchTokens.textPrimary
    }
    Row(
        modifier = modifier.utilityButtonFrame(
            highlighted = highlighted,
        ).clickable(
            onClick = onClick,
        ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.invoke()
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
        if (badge != null) {
            Box(
                modifier = Modifier.utilityBadgeFrame(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = badge,
                    color = contentColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun WorkbenchUserAvatar(
    initials: String,
    modifier: Modifier = Modifier,
) {
    val avatarHalo = AdminWorkbenchTokens.avatarHalo
    val compactTopBar = LocalWorkbenchWindowFrame.current.topBarHeight <= 48.dp

    Box(
        modifier = modifier.size(if (compactTopBar) 20.dp else 24.dp).background(
            color = AdminWorkbenchTokens.avatarBackground,
            shape = CircleShape,
        ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier.size(if (compactTopBar) 20.dp else 24.dp),
        ) {
            drawCircle(
                color = avatarHalo,
                radius = size.minDimension / 2f,
            )
        }
        Text(
            text = initials,
            color = AdminWorkbenchTokens.textPrimary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** 工具按钮底板：默认是后台工具条里的紧凑胶囊按钮，不抢主操作的风头。 */
@Composable
private fun Modifier.utilityButtonFrame(
    highlighted: Boolean,
): Modifier {
    val compactTopBar = LocalWorkbenchWindowFrame.current.topBarHeight <= 48.dp
    val background = if (highlighted) {
        AdminWorkbenchTokens.highlightedBackground
    } else {
        AdminWorkbenchTokens.buttonBackground
    }
    val border = if (highlighted) {
        AdminWorkbenchTokens.highlightedBorder
    } else {
        AdminWorkbenchTokens.buttonBorder
    }
    return background(
        color = background,
        shape = RoundedCornerShape(999.dp),
    ).border(
        width = 1.dp,
        color = border,
        shape = RoundedCornerShape(999.dp),
    ).padding(
        horizontal = if (compactTopBar) 10.dp else 12.dp,
        vertical = if (compactTopBar) 6.dp else 8.dp,
    )
}

/** 角标胶囊：用于通知数这类轻量全局状态，不把按钮撑成大块。 */
@Composable
private fun Modifier.utilityBadgeFrame(): Modifier {
    return size(width = 24.dp, height = 18.dp)
        .background(
            color = AdminWorkbenchTokens.badgeBackground,
            shape = CircleShape,
        ).padding(horizontal = 4.dp, vertical = 2.dp)
}

private fun Int.toNotificationBadge(): String? {
    return when {
        this <= 0 -> null
        this > 99 -> "99+"
        else -> toString()
    }
}

private fun String.toAvatarInitials(): String {
    val source = substringBefore("@")
        .split('.', '-', '_', ' ')
        .filter(String::isNotBlank)
    if (source.isEmpty()) {
        return "U"
    }
    val initials = source
        .take(2)
        .joinToString(separator = "") { token ->
            token.first().uppercase()
    }
    return initials.ifBlank { "U" }
}

@Immutable
@Serializable
private data class DefaultAdminWorkbenchPageConfig(
    override val breadcrumb: List<String>,
    override val pageTitle: String,
    override val pageSubtitle: String?,
) : AdminWorkbenchPageConfig

@Immutable
private data class DefaultAdminWorkbenchConfig(
    override val brandLabel: String,
    override val welcomeLabel: String,
    override val defaultSidebarRatio: Float,
    override val minSidebarWidth: Dp,
    override val maxSidebarWidth: Dp,
    override val detailWidth: Dp,
    override val outerPadding: PaddingValues,
    override val contentPadding: PaddingValues,
    override val detailPadding: PaddingValues,
    override val githubLabel: String?,
    override val languageLabel: String?,
    override val isDarkTheme: Boolean?,
    override val notificationCount: Int?,
    override val userLabel: String?,
) : AdminWorkbenchConfig {
    override val contentHeaderScrollable: Boolean
        get() = false
}

private data class DefaultAdminWorkbenchActions(
    override val onGlobalSearchClick: (() -> Unit)?,
    override val onGithubClick: (() -> Unit)?,
    override val onLanguageClick: (() -> Unit)?,
    override val onThemeToggle: (() -> Unit)?,
    override val onNotificationsClick: (() -> Unit)?,
    override val onUserClick: (() -> Unit)?,
) : AdminWorkbenchActions

private data class DefaultAdminWorkbenchSlots(
    override val brandContent: (@Composable RowScope.() -> Unit)?,
    override val pageActions: @Composable RowScope.() -> Unit,
    override val showContentHeader: Boolean,
    override val titleContent: (@Composable ColumnScope.() -> Unit)?,
    override val detail: (@Composable BoxScope.() -> Unit)?,
    override val userContent: (@Composable RowScope.() -> Unit)?,
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

private object AdminWorkbenchTokens {
    val topBarBackground: Color
        @Composable get() = LocalAdminWorkbenchColors.current.topBarBackground
    val topBarTextPrimary: Color
        @Composable get() = LocalAdminWorkbenchColors.current.topBarTextPrimary
    val topBarTextSecondary: Color
        @Composable get() = LocalAdminWorkbenchColors.current.topBarTextSecondary
    val pageBackground: Color
        @Composable get() = LocalAdminWorkbenchColors.current.pageBackground
    val sidebarBackground: Color
        @Composable get() = LocalAdminWorkbenchColors.current.sidebarBackground
    val headerBackground: Color
        @Composable get() = LocalAdminWorkbenchColors.current.headerBackground
    val detailBackground: Color
        @Composable get() = LocalAdminWorkbenchColors.current.detailBackground
    val dividerColor: Color
        @Composable get() = LocalAdminWorkbenchColors.current.dividerColor
    val resizeThumbColor: Color
        @Composable get() = LocalAdminWorkbenchColors.current.resizeThumbColor
    val resizeThumbBorder: Color
        @Composable get() = LocalAdminWorkbenchColors.current.resizeThumbBorder
    val buttonBackground: Color
        @Composable get() = LocalAdminWorkbenchColors.current.buttonBackground
    val buttonBorder: Color
        @Composable get() = LocalAdminWorkbenchColors.current.buttonBorder
    val highlightedBackground: Color
        @Composable get() = LocalAdminWorkbenchColors.current.highlightedBackground
    val highlightedBorder: Color
        @Composable get() = LocalAdminWorkbenchColors.current.highlightedBorder
    val highlightedTextPrimary: Color
        @Composable get() = LocalAdminWorkbenchColors.current.highlightedTextPrimary
    val badgeBackground: Color
        @Composable get() = LocalAdminWorkbenchColors.current.badgeBackground
    val avatarBackground: Color
        @Composable get() = LocalAdminWorkbenchColors.current.avatarBackground
    val avatarHalo: Color
        @Composable get() = LocalAdminWorkbenchColors.current.avatarHalo
    val textPrimary: Color
        @Composable get() = LocalAdminWorkbenchColors.current.textPrimary
    val headerTextPrimary: Color
        @Composable get() = LocalAdminWorkbenchColors.current.headerTextPrimary
    val headerTextMuted: Color
        @Composable get() = LocalAdminWorkbenchColors.current.headerTextMuted
    val brandPlateBackground: Color
        @Composable get() = LocalAdminWorkbenchColors.current.brandPlateBackground
    val brandPrimaryDot: Color
        @Composable get() = LocalAdminWorkbenchColors.current.brandPrimaryDot
    val brandSecondaryDot: Color
        @Composable get() = LocalAdminWorkbenchColors.current.brandSecondaryDot
}
