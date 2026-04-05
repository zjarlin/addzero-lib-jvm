package site.addzero.appsidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import site.addzero.appsidebar.spi.ScaffoldConfigSpi
import site.addzero.appsidebar.spi.SidebarResizeConfig
import site.addzero.appsidebar.spi.scaffoldConfig
import site.addzero.appsidebar.spi.sidebarResizeConfig
import kotlin.math.roundToInt

/**
 * 可拖拽工作台侧栏的状态。
 */
@Stable
class AppSidebarScaffoldState internal constructor(
    defaultSidebarRatio: Float,
) {
    var sidebarRatio by mutableFloatStateOf(defaultSidebarRatio)
        private set

    private var sidebarWidthPx by mutableFloatStateOf(Float.NaN)

    /**
     * 容器尺寸更新后，重新同步侧栏宽度和比例。
     */
    fun onContainerMeasured(
        containerWidthPx: Float,
        minSidebarWidthPx: Float,
        maxSidebarWidthPx: Float,
    ) {
        if (containerWidthPx <= 0f) {
            return
        }
        val measuredSidebarWidthPx = if (sidebarWidthPx.isNaN()) {
            containerWidthPx * sidebarRatio
        } else {
            sidebarWidthPx
        }.coerceIn(minSidebarWidthPx, maxSidebarWidthPx)

        sidebarWidthPx = measuredSidebarWidthPx
        sidebarRatio = (measuredSidebarWidthPx / containerWidthPx).coerceIn(0f, 1f)
    }

    /**
     * 根据拖拽增量更新侧栏宽度。
     */
    fun dragSidebarBy(
        deltaPx: Float,
        containerWidthPx: Float,
        minSidebarWidthPx: Float,
        maxSidebarWidthPx: Float,
    ) {
        if (containerWidthPx <= 0f) {
            return
        }
        val nextWidth = (currentSidebarWidthPx(containerWidthPx) + deltaPx)
            .coerceIn(minSidebarWidthPx, maxSidebarWidthPx)
        sidebarWidthPx = nextWidth
        sidebarRatio = (nextWidth / containerWidthPx).coerceIn(0f, 1f)
    }

    /**
     * 按当前状态计算侧栏宽度。
     */
    fun currentSidebarWidthPx(
        containerWidthPx: Float,
    ): Float {
        return if (sidebarWidthPx.isNaN()) {
            containerWidthPx * sidebarRatio
        } else {
            sidebarWidthPx
        }
    }

    internal companion object {
        val Saver: Saver<AppSidebarScaffoldState, Float> = Saver(
            save = { state -> state.sidebarRatio },
            restore = { savedRatio -> AppSidebarScaffoldState(savedRatio) },
        )
    }
}

/**
 * 记住侧栏骨架状态。
 */
@Composable
fun rememberAppSidebarScaffoldState(
    defaultSidebarRatio: Float = 0.22f,
): AppSidebarScaffoldState = rememberSaveable(
    inputs = arrayOf(defaultSidebarRatio),
    saver = AppSidebarScaffoldState.Saver,
) {
    AppSidebarScaffoldState(
        defaultSidebarRatio = defaultSidebarRatio,
    )
}

/**
 * 兼容旧命名的工作台骨架状态。
 */
typealias WorkbenchScaffoldState = AppSidebarScaffoldState

/**
 * 记住工作台骨架状态。
 */
@Composable
fun rememberWorkbenchScaffoldState(
    defaultSidebarRatio: Float = 0.22f,
): WorkbenchScaffoldState = rememberAppSidebarScaffoldState(
    defaultSidebarRatio = defaultSidebarRatio,
)

/**
 * 工作台骨架的可插拔区域。
 */
interface WorkbenchScaffoldSlots {
    val contentHeader: (@Composable RowScope.() -> Unit)?
        get() = null
    val detail: (@Composable BoxScope.() -> Unit)?
        get() = null
}

/**
 * 工作台骨架的装饰项。
 */
interface WorkbenchScaffoldDecor {
    val sidebarContainerModifier: Modifier
        get() = Modifier
    val mainContainerModifier: Modifier
        get() = Modifier
    val headerContainerModifier: Modifier
        get() = Modifier
    val detailContainerModifier: Modifier
        get() = Modifier
    val resizeConfig: SidebarResizeConfig
}

/**
 * 创建工作台插槽配置。
 */
fun workbenchScaffoldSlots(
    contentHeader: (@Composable RowScope.() -> Unit)? = null,
    detail: (@Composable BoxScope.() -> Unit)? = null,
): WorkbenchScaffoldSlots = DefaultWorkbenchScaffoldSlots(
    contentHeader = contentHeader,
    detail = detail,
)

/**
 * 创建工作台装饰配置。
 */
fun workbenchScaffoldDecor(
    sidebarContainerModifier: Modifier = Modifier,
    mainContainerModifier: Modifier = Modifier,
    headerContainerModifier: Modifier = Modifier,
    detailContainerModifier: Modifier = Modifier,
    resizeConfig: SidebarResizeConfig = sidebarResizeConfig(
        dividerColor = WorkbenchTokens.dividerColor,
        thumbColor = WorkbenchTokens.thumbColor,
        thumbBorderColor = WorkbenchTokens.thumbBorder,
    ),
): WorkbenchScaffoldDecor = DefaultWorkbenchScaffoldDecor(
    sidebarContainerModifier = sidebarContainerModifier,
    mainContainerModifier = mainContainerModifier,
    headerContainerModifier = headerContainerModifier,
    detailContainerModifier = detailContainerModifier,
    resizeConfig = resizeConfig,
)

/**
 * 兼容旧命名的侧栏工作台骨架。
 */
@Composable
fun AppSidebarScaffold(
    sidebar: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    state: AppSidebarScaffoldState? = null,
    config: ScaffoldConfigSpi = scaffoldConfig(),
) {
    WorkbenchScaffold(
        sidebar = sidebar,
        content = content,
        modifier = modifier,
        state = state,
        config = config,
    )
}

/**
 * 通用工作台骨架。
 *
 * 提供左侧栏、主内容区、内容头部和可选详情区的基础布局能力。
 */
@Composable
fun WorkbenchScaffold(
    sidebar: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    state: AppSidebarScaffoldState? = null,
    config: ScaffoldConfigSpi = scaffoldConfig(),
    slots: WorkbenchScaffoldSlots = workbenchScaffoldSlots(),
    decor: WorkbenchScaffoldDecor = workbenchScaffoldDecor(),
) {
    val scaffoldState = state ?: rememberAppSidebarScaffoldState(config.defaultSidebarRatio)

    ResizableSidebarShell(
        modifier = modifier,
        sidebar = sidebar,
        state = scaffoldState,
        minSidebarWidth = config.minSidebarWidth,
        maxSidebarWidth = config.maxSidebarWidth,
        outerPadding = config.outerPadding,
        sidebarContainerModifier = decor.sidebarContainerModifier,
        mainContainerModifier = decor.mainContainerModifier,
        sidebarResizeConfig = decor.resizeConfig,
        body = { layoutClass ->
            MainWorkbenchPanel(
                modifier = Modifier.weight(1f),
                contentHeader = slots.contentHeader,
                contentHeaderScrollable = config.contentHeaderScrollable,
                contentPadding = config.contentPadding,
                headerContainerModifier = decor.headerContainerModifier,
                dividerColor = decor.resizeConfig.dividerColor,
                content = content,
            )

            if (slots.detail != null && layoutClass == WorkbenchLayoutClass.Expanded) {
                val detail = slots.detail
                Box(
                    modifier = Modifier.workbenchDivider(
                        color = decor.resizeConfig.dividerColor,
                    ),
                )
                if (detail != null) {
                    Box(
                        modifier = Modifier.width(config.detailWidth)
                            .fillMaxHeight()
                            .then(decor.detailContainerModifier)
                            .padding(config.detailPadding),
                    ) {
                        CompositionLocalProvider(
                            LocalContentColor provides MaterialTheme.colorScheme.onSurface,
                        ) {
                            detail.invoke(this)
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun ResizableSidebarShell(
    modifier: Modifier,
    sidebar: @Composable BoxScope.() -> Unit,
    body: @Composable RowScope.(WorkbenchLayoutClass) -> Unit,
    state: AppSidebarScaffoldState,
    minSidebarWidth: Dp,
    maxSidebarWidth: Dp,
    outerPadding: androidx.compose.foundation.layout.PaddingValues,
    sidebarContainerModifier: Modifier,
    mainContainerModifier: Modifier,
    sidebarResizeConfig: SidebarResizeConfig,
) {
    var containerWidthPx by remember { mutableFloatStateOf(0f) }
    val density = androidx.compose.ui.platform.LocalDensity.current
    val minSidebarWidthPx = with(density) { minSidebarWidth.toPx() }
    val maxSidebarWidthPx = with(density) { maxSidebarWidth.toPx() }
    val handleHotZoneWidth = WorkbenchTokens.handleHotZoneWidth
    val handleHotZoneWidthPx = with(density) { handleHotZoneWidth.toPx() }
    val sidebarWidthPx = remember(containerWidthPx, state.sidebarRatio, minSidebarWidthPx, maxSidebarWidthPx) {
        if (containerWidthPx <= 0f) {
            0f
        } else {
            state.currentSidebarWidthPx(containerWidthPx)
                .coerceIn(minSidebarWidthPx, maxSidebarWidthPx)
        }
    }
    val sidebarWidth = with(density) {
        sidebarWidthPx.roundToInt().toDp()
    }
    val layoutClass = resolveWorkbenchLayoutClass(
        widthDp = with(density) { containerWidthPx.toDp() },
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(outerPadding)
            .onSizeChanged { size ->
                containerWidthPx = size.width.toFloat()
                state.onContainerMeasured(
                    containerWidthPx = containerWidthPx,
                    minSidebarWidthPx = minSidebarWidthPx,
                    maxSidebarWidthPx = maxSidebarWidthPx,
                )
            },
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier.width(sidebarWidth)
                    .fillMaxHeight()
                    .then(sidebarContainerModifier),
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurface,
                ) {
                    sidebar()
                }
            }
            Row(
                modifier = Modifier.weight(1f)
                    .fillMaxHeight()
                    .then(mainContainerModifier),
                content = {
                    body(layoutClass)
                },
            )
        }

        if (containerWidthPx > 0f) {
            SidebarResizeHandle(
                sidebarResizeConfig = sidebarResizeConfig,
                modifier = Modifier.sidebarHandleOverlay(
                    sidebarWidthPx = sidebarWidthPx,
                    handleHotZoneWidthPx = handleHotZoneWidthPx,
                    handleHotZoneWidth = handleHotZoneWidth,
                ),
                onDrag = { deltaPx ->
                    state.dragSidebarBy(
                        deltaPx = deltaPx,
                        containerWidthPx = containerWidthPx,
                        minSidebarWidthPx = minSidebarWidthPx,
                        maxSidebarWidthPx = maxSidebarWidthPx,
                    )
                },
            )
        }
    }
}

@Composable
private fun MainWorkbenchPanel(
    modifier: Modifier,
    contentHeader: (@Composable RowScope.() -> Unit)?,
    contentHeaderScrollable: Boolean,
    contentPadding: androidx.compose.foundation.layout.PaddingValues,
    headerContainerModifier: Modifier,
    dividerColor: Color,
    content: @Composable BoxScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxHeight(),
    ) {
        if (contentHeader != null) {
            val headerOverflowModifier = if (contentHeaderScrollable) {
                Modifier.horizontalScroll(rememberScrollState())
            } else {
                Modifier
            }
            Row(
                modifier = Modifier.fillMaxWidth()
                    .then(headerOverflowModifier)
                    .then(headerContainerModifier)
                    .workbenchHeaderFrame(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurface,
                ) {
                    contentHeader()
                }
            }
            Box(
                modifier = Modifier.workbenchHorizontalDivider(
                    color = dividerColor,
                ),
            )
        }

        Box(
            modifier = Modifier.weight(1f)
                .fillMaxWidth()
                .padding(contentPadding),
        ) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurface,
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SidebarResizeHandle(
    sidebarResizeConfig: SidebarResizeConfig,
    modifier: Modifier,
    onDrag: (Float) -> Unit,
) {
    Box(
        modifier = modifier.dragHandlePointer(onDrag = onDrag),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.dragHandleRail(
                color = sidebarResizeConfig.dividerColor,
            ),
        ) {
            Box(
                modifier = Modifier.align(Alignment.Center).dragHandleThumb(
                    color = sidebarResizeConfig.thumbColor,
                    borderColor = sidebarResizeConfig.thumbBorderColor,
                ),
            )
        }
    }
}

private fun Modifier.dragHandlePointer(
    onDrag: (Float) -> Unit,
): Modifier {
    return pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
            change.consume()
            onDrag(dragAmount.x)
        }
    }
}

private fun Modifier.sidebarHandleOverlay(
    sidebarWidthPx: Float,
    handleHotZoneWidthPx: Float,
    handleHotZoneWidth: Dp,
): Modifier {
    return offset {
        IntOffset(
            x = (sidebarWidthPx - handleHotZoneWidthPx / 2f).roundToInt(),
            y = 0,
        )
    }.width(handleHotZoneWidth)
        .fillMaxHeight()
        .zIndex(1f)
}

private fun Modifier.dragHandleRail(
    color: Color,
): Modifier {
    return width(1.dp)
        .fillMaxHeight()
        .padding(vertical = 10.dp)
        .background(color, CircleShape)
}

private fun Modifier.dragHandleThumb(
    color: Color,
    borderColor: Color,
): Modifier {
    return width(6.dp)
        .fillMaxHeight(0.08f)
        .background(color, CircleShape)
        .border(1.dp, borderColor, CircleShape)
}

private fun Modifier.workbenchHeaderFrame(): Modifier {
    return padding(horizontal = 16.dp, vertical = 12.dp)
}

private fun Modifier.workbenchDivider(
    color: Color,
): Modifier {
    return width(1.dp)
        .fillMaxHeight()
        .background(color)
}

private fun Modifier.workbenchHorizontalDivider(
    color: Color,
): Modifier {
    return fillMaxWidth()
        .height(1.dp)
        .background(color)
}

private class DefaultWorkbenchScaffoldSlots(
    override val contentHeader: (@Composable RowScope.() -> Unit)?,
    override val detail: (@Composable BoxScope.() -> Unit)?,
) : WorkbenchScaffoldSlots

private class DefaultWorkbenchScaffoldDecor(
    override val sidebarContainerModifier: Modifier,
    override val mainContainerModifier: Modifier,
    override val headerContainerModifier: Modifier,
    override val detailContainerModifier: Modifier,
    override val resizeConfig: SidebarResizeConfig,
) : WorkbenchScaffoldDecor

private object WorkbenchTokens {
    val handleHotZoneWidth = 14.dp
    val dividerColor = Color.White.copy(alpha = 0.08f)
    val thumbColor = Color.White.copy(alpha = 0.18f)
    val thumbBorder = Color.White.copy(alpha = 0.10f)
    val expandedBreakpoint = 1200.dp
    val mediumBreakpoint = 760.dp
}

private enum class WorkbenchLayoutClass {
    Compact,
    Medium,
    Expanded,
}

private fun resolveWorkbenchLayoutClass(
    widthDp: Dp,
): WorkbenchLayoutClass {
    return when {
        widthDp.value >= WorkbenchTokens.expandedBreakpoint.value -> WorkbenchLayoutClass.Expanded
        widthDp.value >= WorkbenchTokens.mediumBreakpoint.value -> WorkbenchLayoutClass.Medium
        else -> WorkbenchLayoutClass.Compact
    }
}
