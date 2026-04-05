package site.addzero.appsidebar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import site.addzero.workbench.design.button.WorkbenchButtonSize
import site.addzero.workbench.design.button.WorkbenchButtonVariant
import site.addzero.workbench.design.button.WorkbenchPillButton

/**
 * 后台工作台的全局搜索按钮。
 */
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

/**
 * 后台工作台的语言切换按钮。
 */
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

/**
 * 后台工作台的 GitHub 入口按钮。
 */
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

/**
 * 后台工作台的主题切换按钮。
 */
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
            )
        },
    )
}

/**
 * 后台工作台的通知入口按钮。
 */
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

/**
 * 后台工作台的用户入口按钮。
 */
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

/**
 * 后台工作台的侧栏显隐按钮。
 */
@Composable
fun WorkbenchSidebarToggleButton(
    sidebarVisible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    visibleLabel: String = "隐藏菜单",
    hiddenLabel: String = "显示菜单",
) {
    WorkbenchUtilityButton(
        label = if (sidebarVisible) visibleLabel else hiddenLabel,
        modifier = modifier,
        onClick = onClick,
        leading = {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = null,
            )
        },
    )
}

/**
 * 后台工作台的全局顶部工具条。
 */
@Composable
internal fun AdminWorkbenchGlobalBar(
    config: AdminWorkbenchConfigSpi,
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
            config.onSidebarToggle?.let { onSidebarToggle ->
                WorkbenchSidebarToggleButton(
                    sidebarVisible = config.sidebarVisible,
                    onClick = onSidebarToggle,
                )
            }
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
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
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
            val topBarActions = slots.topBarActions
            if (topBarActions != null) {
                topBarActions.invoke(this)
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
private fun WorkbenchUtilityButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    highlighted: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
) {
    val compactTopBar = LocalWorkbenchWindowFrame.current.topBarHeight <= 48.dp
    WorkbenchPillButton(
        onClick = onClick,
        modifier = modifier,
        variant = if (highlighted) {
            WorkbenchButtonVariant.Secondary
        } else {
            WorkbenchButtonVariant.Outline
        },
        size = if (compactTopBar) {
            WorkbenchButtonSize.Sm
        } else {
            WorkbenchButtonSize.Default
        },
        content = {
            leading?.invoke()
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            )
            if (badge != null) {
                Box(
                    modifier = Modifier.utilityBadgeFrame(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = badge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    )
                }
            }
        },
    )
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
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        )
    }
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
