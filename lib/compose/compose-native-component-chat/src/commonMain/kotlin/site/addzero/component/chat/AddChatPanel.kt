package site.addzero.component.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import site.addzero.component.button.AddButton
import site.addzero.component.button.AddIconButton
import site.addzero.component.text.BodyMedium
import site.addzero.component.text.Caption
import site.addzero.component.text.H1
import site.addzero.component.text.H3

/**
 * 通用聊天面板。
 */
@Composable
fun AddChatPanel(
    state: AddChatPanelState,
    actions: AddChatPanelActions,
    modifier: Modifier = Modifier,
    slots: AddChatPanelSlots = AddChatPanelSlots(),
) {
    val listState = rememberLazyListState()
    val visibleMessageCount = state.messages.size + if (state.isSending) 1 else 0

    LaunchedEffect(visibleMessageCount) {
        if (visibleMessageCount > 0) {
            listState.animateScrollToItem(visibleMessageCount - 1)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(chatPanelBackground())
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ChatSessionSidebar(
                state = state,
                actions = actions,
                slots = slots,
            )
            ChatMainPanel(
                state = state,
                actions = actions,
                slots = slots,
                listState = listState,
            )
        }
    }
}

@Composable
private fun ChatSessionSidebar(
    state: AddChatPanelState,
    actions: AddChatPanelActions,
    slots: AddChatPanelSlots,
) {
    Surface(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                H1(text = state.title)
                Caption(
                    text = state.subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AddButton(
                    displayName = "新建",
                    icon = Icons.Default.Add,
                    onClick = actions.onCreateSession,
                    modifier = Modifier.weight(1f),
                )
                AddButton(
                    displayName = "删除",
                    icon = Icons.Default.DeleteOutline,
                    onClick = { actions.onDeleteSession(state.selectedSessionId) },
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))

            if (state.sessions.isEmpty()) {
                if (slots.sessionEmpty != null) {
                    slots.sessionEmpty.invoke()
                } else {
                    DefaultSessionEmpty()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                ) {
                    items(
                        items = state.sessions,
                        key = { it.id },
                    ) { session ->
                        SessionListItem(
                            session = session,
                            selected = session.id == state.selectedSessionId,
                            onClick = { actions.onSelectSession(session.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMainPanel(
    state: AddChatPanelState,
    actions: AddChatPanelActions,
    slots: AddChatPanelSlots,
    listState: androidx.compose.foundation.lazy.LazyListState,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ChatHeader(
                state = state,
                actions = actions,
                slots = slots,
            )

            if (state.isLoadingMessages) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            AnimatedVisibility(visible = state.showConnectionEditor) {
                ChatConnectionEditor(
                    connection = state.connection,
                    onConnectionChange = actions.onConnectionChange,
                )
            }

            ChatMessageViewport(
                state = state,
                actions = actions,
                slots = slots,
                listState = listState,
                modifier = Modifier.weight(1f),
            )

            ChatComposer(
                state = state,
                actions = actions,
            )
        }
    }
}

@Composable
private fun ChatHeader(
    state: AddChatPanelState,
    actions: AddChatPanelActions,
    slots: AddChatPanelSlots,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                H1(text = "AI 对话")
                BodyMedium(
                    text = if (state.selectedSessionId == null) {
                        "先创建或选择会话，再开始提问。"
                    } else {
                        "当前连接到 ${state.connection.vendor.displayName} · ${state.connection.transport.displayName}"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AddIconButton(
                    text = if (state.showConnectionEditor) "收起连接配置" else "展开连接配置",
                    imageVector = Icons.Default.SettingsSuggest,
                    onClick = { actions.onToggleConnectionEditor(!state.showConnectionEditor) },
                )
            }
        }

        if (state.statusText.isNotBlank()) {
            AssistChip(
                onClick = {},
                label = { Text(state.statusText) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            )
        }

        if (slots.header != null) {
            slots.header.invoke()
        }
    }
}

@Composable
private fun ChatConnectionEditor(
    connection: AddChatConnectionConfig,
    onConnectionChange: (AddChatConnectionConfig) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            H3(text = "连接配置")

            OutlinedTextField(
                value = connection.backendUrl,
                onValueChange = { onConnectionChange(connection.copy(backendUrl = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("KCloud 后端 URL") },
                singleLine = true,
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Caption(text = "传输协议")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AddChatTransport.entries.forEach { transport ->
                        FilterChip(
                            selected = connection.transport == transport,
                            onClick = { onConnectionChange(connection.copy(transport = transport)) },
                            label = { Text(transport.displayName) },
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Caption(text = "模型厂商")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AddChatVendor.entries.forEach { vendor ->
                        FilterChip(
                            selected = connection.vendor == vendor,
                            onClick = { onConnectionChange(connection.copy(vendor = vendor)) },
                            label = { Text(vendor.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                }
            }

            OutlinedTextField(
                value = connection.providerBaseUrl,
                onValueChange = { onConnectionChange(connection.copy(providerBaseUrl = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("模型 Base URL") },
                placeholder = { Text("留空则按厂商默认地址") },
                singleLine = true,
            )

            OutlinedTextField(
                value = connection.apiKey,
                onValueChange = { onConnectionChange(connection.copy(apiKey = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("API Key") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    keyboardType = KeyboardType.Password,
                ),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = connection.model,
                    onValueChange = { onConnectionChange(connection.copy(model = it)) },
                    modifier = Modifier.weight(1f),
                    label = { Text("模型名") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = connection.vendor.wireValue,
                    onValueChange = {},
                    modifier = Modifier.weight(1f),
                    label = { Text("厂商标识") },
                    singleLine = true,
                    readOnly = true,
                )
            }

            OutlinedTextField(
                value = connection.systemPrompt,
                onValueChange = { onConnectionChange(connection.copy(systemPrompt = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("系统提示词") },
                minLines = 2,
                maxLines = 5,
            )
        }
    }
}

@Composable
private fun ChatMessageViewport(
    state: AddChatPanelState,
    actions: AddChatPanelActions,
    slots: AddChatPanelSlots,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.86f),
        tonalElevation = 1.dp,
    ) {
        if (state.messages.isEmpty()) {
            ChatEmptyState(
                state = state,
                actions = actions,
                slots = slots,
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 8.dp),
            ) {
                items(
                    items = state.messages,
                    key = { it.id },
                ) { message ->
                    MessageBubble(
                        message = message,
                        onRetry = { actions.onRetryMessage(message) },
                        content = slots.messageContent,
                    )
                }
                if (state.isSending) {
                    item(key = "sending-indicator") {
                        ThinkingBubble()
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatEmptyState(
    state: AddChatPanelState,
    actions: AddChatPanelActions,
    slots: AddChatPanelSlots,
) {
    if (slots.empty != null) {
        slots.empty.invoke()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            H3(text = state.emptyTitle)
            Caption(text = state.emptyDescription)
        }

        if (state.quickPrompts.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Caption(text = "快捷提示")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.quickPrompts.forEach { prompt ->
                        AssistChip(
                            onClick = { actions.onUsePrompt(prompt) },
                            label = { Text(prompt.title) },
                        )
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Caption(text = "当前连接")
                BodyMedium(text = "后端：${state.connection.backendUrl.ifBlank { "未填写" }}")
                BodyMedium(text = "模型厂商：${state.connection.vendor.displayName}")
                BodyMedium(text = "传输协议：${state.connection.transport.displayName}")
            }
        }
    }
}

@Composable
private fun ChatComposer(
    state: AddChatPanelState,
    actions: AddChatPanelActions,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            OutlinedTextField(
                value = state.input,
                onValueChange = actions.onInputChange,
                modifier = Modifier
                    .weight(1f)
                    .onPreviewKeyEvent { event ->
                        if (event.type != KeyEventType.KeyDown || event.key != Key.Enter) {
                            return@onPreviewKeyEvent false
                        }
                        if (event.isShiftPressed || event.isCtrlPressed || event.isAltPressed) {
                            actions.onInputChange(state.input + "\n")
                        } else if (state.input.isNotBlank() && !state.isSending) {
                            actions.onSend()
                        }
                        true
                    },
                label = { Text("输入消息") },
                placeholder = { Text("Enter 发送，Shift/Ctrl/Alt + Enter 换行") },
                minLines = 3,
                maxLines = 8,
            )

            AddButton(
                displayName = if (state.isSending) "发送中" else "发送",
                icon = Icons.AutoMirrored.Filled.Send,
                onClick = actions.onSend,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun SessionListItem(
    session: AddChatSessionItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val containerColor = when {
        selected -> MaterialTheme.colorScheme.primaryContainer
        hovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.66f)
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.44f)
        hovered -> MaterialTheme.colorScheme.outlineVariant
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, RoundedCornerShape(18.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = session.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            if (session.subtitle.isNotBlank()) {
                Caption(text = session.subtitle)
            }
            if (session.badgeText.isNotBlank()) {
                AssistChip(
                    onClick = {},
                    label = { Text(session.badgeText) },
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: AddChatMessageItem,
    onRetry: () -> Unit,
    content: (@Composable (AddChatMessageItem) -> Unit)?,
) {
    val isUser = message.role == AddChatMessageRole.User
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(if (isUser) 0.88f else 0.94f),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top,
        ) {
            if (!isUser) {
                MessageAvatar(role = message.role)
                Spacer(modifier = Modifier.width(10.dp))
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isUser) 18.dp else 6.dp,
                    bottomEnd = if (isUser) 6.dp else 18.dp,
                ),
                color = messageBubbleColor(message.role),
                tonalElevation = 1.dp,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = message.role.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (message.timestampLabel.isNotBlank()) {
                            Caption(
                                text = message.timestampLabel,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    if (content != null) {
                        content.invoke(message)
                    } else {
                        DefaultMessageContent(message)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (message.statusLabel.isNotBlank()) {
                            Caption(text = message.statusLabel)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        if (message.canRetry) {
                            TextButton(onClick = onRetry) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "重试",
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("重试")
                            }
                        }
                    }
                }
            }

            if (isUser) {
                Spacer(modifier = Modifier.width(10.dp))
                MessageAvatar(role = message.role)
            }
        }
    }
}

@Composable
private fun DefaultMessageContent(
    message: AddChatMessageItem,
) {
    SelectionContainer {
        Text(
            text = message.content,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = if (message.role == AddChatMessageRole.Tool) {
                FontFamily.Monospace
            } else {
                FontFamily.Default
            },
        )
    }
}

@Composable
private fun MessageAvatar(
    role: AddChatMessageRole,
) {
    val backgroundColor = when (role) {
        AddChatMessageRole.User -> MaterialTheme.colorScheme.primary
        AddChatMessageRole.Assistant -> MaterialTheme.colorScheme.tertiary
        AddChatMessageRole.System -> MaterialTheme.colorScheme.secondary
        AddChatMessageRole.Tool -> MaterialTheme.colorScheme.secondaryContainer
    }
    val label = when (role) {
        AddChatMessageRole.User -> "U"
        AddChatMessageRole.Assistant -> "A"
        AddChatMessageRole.System -> "S"
        AddChatMessageRole.Tool -> "T"
    }

    Box(
        modifier = Modifier
            .size(28.dp)
            .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ThinkingBubble() {
    val transition = rememberInfiniteTransition(label = "chat-thinking")
    val alphaA by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "thinking-a",
    )
    val alphaB by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "thinking-b",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MessageAvatar(role = AddChatMessageRole.Assistant)
        Spacer(modifier = Modifier.width(10.dp))
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Caption(text = "助手思考中")
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .alpha(if (index % 2 == 0) alphaA else alphaB)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                    )
                }
            }
        }
    }
}

@Composable
private fun DefaultSessionEmpty() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        H3(text = "暂无会话")
        Caption(text = "点击“新建”后开始第一轮对话。")
    }
}

@Composable
private fun messageBubbleColor(
    role: AddChatMessageRole,
): Color {
    return when (role) {
        AddChatMessageRole.User -> MaterialTheme.colorScheme.primaryContainer
        AddChatMessageRole.Assistant -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
        AddChatMessageRole.System -> MaterialTheme.colorScheme.secondaryContainer
        AddChatMessageRole.Tool -> MaterialTheme.colorScheme.tertiaryContainer
    }
}

@Composable
private fun chatPanelBackground(): Brush {
    return Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
            MaterialTheme.colorScheme.surface,
        ),
    )
}
