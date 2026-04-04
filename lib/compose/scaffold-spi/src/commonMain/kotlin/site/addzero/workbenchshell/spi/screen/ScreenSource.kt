package site.addzero.workbenchshell.spi.screen

/**
 * 工作台菜单来源。
 *
 * `@Route`、数据库 SysMenu、远程配置等都应该实现成 source，再在壳层统一融合。
 */
interface ScreenSource {
    val priority
        get() = 0

    fun listScreens(): List<Screen>
}

fun mergeScreens(
    sources: List<ScreenSource>,
): List<Screen> {
    val merged = linkedMapOf<String, BasicScreen>()
    sources
        .sortedBy(ScreenSource::priority)
        .flatMap(ScreenSource::listScreens)
        .filter(Screen::enabled)
        .forEach { screen ->
            val mergeKey = screen.mergeKey()
            val incoming = screen.toBasicScreen()
            merged[mergeKey] = merged[mergeKey]
                ?.mergeWith(incoming)
                ?: incoming
        }
    return merged.values.toList()
}

private fun Screen.mergeKey(): String {
    return routePath.ifBlank {
        qualifiedName.ifBlank {
            "${sceneName}::${title}"
        }
    }
}

private fun BasicScreen.mergeWith(
    incoming: BasicScreen,
): BasicScreen {
    return copy(
        value = incoming.value.ifBlank { value },
        title = incoming.title.ifBlank { title },
        routePath = incoming.routePath.ifBlank { routePath },
        icon = incoming.icon.ifBlank { icon },
        order = incoming.order,
        enabled = incoming.enabled,
        sceneName = incoming.sceneName.ifBlank { sceneName },
        sceneIcon = incoming.sceneIcon.ifBlank { sceneIcon },
        sceneOrder = incoming.sceneOrder,
        defaultInScene = incoming.defaultInScene,
        qualifiedName = incoming.qualifiedName.ifBlank { qualifiedName },
        simpleName = incoming.simpleName.ifBlank { simpleName },
    )
}
