package site.addzero.appsidebar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
                tint = AdminWorkbenchTokens.highlightedTextPrimary,
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
 * 后台工作台的全局顶部工具条。
 */
@Composable
internal fun AdminWorkbenchGlobalBar(
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
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
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
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
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
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
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
