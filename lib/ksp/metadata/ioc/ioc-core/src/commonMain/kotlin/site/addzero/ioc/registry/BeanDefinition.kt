package site.addzero.ioc.registry

/**
 * Runtime descriptor generated from `@Bean`.
 */
data class BeanDefinition(
    val simpleName: String,
    val qualifiedName: String = "",
    val beanName: String = simpleName.replaceFirstChar { it.lowercase() },
    val enabled: Boolean = true,
    val tags: List<String> = emptyList(),
    val order: Int = Int.MAX_VALUE,
    val dependsOn: List<String> = emptyList()
) {
    val identity: String
        get() = qualifiedName.ifBlank { beanName.ifBlank { simpleName } }

    val aliases: List<String>
        get() = listOf(identity, beanName, qualifiedName, simpleName)
            .filter { it.isNotBlank() }
            .distinct()

    fun hasTag(tag: String): Boolean = tag in tags
}

/**
 * Shared bean-definition query / planning helpers.
 */
object BeanDefinitions {

    fun enabled(definitions: Collection<BeanDefinition>): List<BeanDefinition> {
        return unique(definitions.filter { it.enabled })
    }

    fun unique(definitions: Collection<BeanDefinition>): List<BeanDefinition> {
        return definitions
            .distinctBy { it.identity }
            .sortedWith(beanDefinitionComparator)
    }

    fun groupByTag(definitions: Collection<BeanDefinition>): Map<String, List<BeanDefinition>> {
        val grouped = linkedMapOf<String, MutableList<BeanDefinition>>()
        unique(definitions).forEach { definition ->
            definition.tags.distinct().forEach { tag ->
                grouped.getOrPut(tag) { mutableListOf() }.add(definition)
            }
        }
        return grouped
            .mapValues { (_, beans) -> beans.sortedWith(beanDefinitionComparator) }
            .toMap()
    }

    fun find(definitions: Collection<BeanDefinition>, name: String): BeanDefinition? {
        if (name.isBlank()) return null
        val available = unique(definitions)
        val directIdentity = available.firstOrNull { it.identity == name }
        if (directIdentity != null) return directIdentity
        return resolveAlias(available, name, "bean lookup", failOnMissing = false)
    }

    fun resolve(definitions: Collection<BeanDefinition>, tag: String): List<BeanDefinition> {
        return resolve(definitions, tags = setOf(tag))
    }

    fun resolve(
        definitions: Collection<BeanDefinition>,
        tags: Set<String> = emptySet(),
        names: Set<String> = emptySet()
    ): List<BeanDefinition> {
        val available = enabled(definitions)
        if (available.isEmpty()) return emptyList()

        val requested = if (tags.isEmpty() && names.isEmpty()) {
            available
        } else {
            available.filter { definition ->
                definition.tags.any(tags::contains) || names.any { it in definition.aliases }
            }
        }
        if (requested.isEmpty()) return emptyList()

        val selected = linkedSetOf<BeanDefinition>()
        fun collect(definition: BeanDefinition) {
            if (!selected.add(definition)) return
            definition.dependsOn.forEach { dependencyName ->
                val dependency = resolveAlias(
                    available,
                    dependencyName,
                    "dependsOn of ${definition.identity}"
                ) ?: return@forEach
                collect(dependency)
            }
        }
        requested.forEach(::collect)

        val resolved = unique(selected.toList())
        return topoSortByDependsOnLocal(
            items = resolved,
            idSelector = BeanDefinition::identity,
            dependsOnSelector = { definition ->
                definition.dependsOn.mapNotNull { dependencyName ->
                    resolveAlias(
                        resolved,
                        dependencyName,
                        "dependsOn of ${definition.identity}"
                    )?.identity
                }
            }
        )
    }

    private fun resolveAlias(
        definitions: List<BeanDefinition>,
        name: String,
        context: String,
        failOnMissing: Boolean = true
    ): BeanDefinition? {
        val matches = definitions.filter { name in it.aliases }
        if (matches.isEmpty()) {
            if (failOnMissing) {
                error("Missing bean '$name' in $context")
            }
            return null
        }

        val bestRank = matches.minOf { aliasRank(it, name) }
        val narrowed = matches.filter { aliasRank(it, name) == bestRank }
        if (narrowed.size > 1) {
            error(
                "Ambiguous bean reference '$name' in $context: ${
                    narrowed.joinToString { it.identity }
                }"
            )
        }
        return narrowed.single()
    }

    private fun aliasRank(definition: BeanDefinition, name: String): Int {
        return when {
            definition.identity == name -> 0
            definition.beanName == name -> 1
            definition.qualifiedName == name -> 2
            definition.simpleName == name -> 3
            else -> Int.MAX_VALUE
        }
    }

    private val beanDefinitionComparator = compareBy<BeanDefinition>(
        BeanDefinition::order,
        BeanDefinition::beanName,
        BeanDefinition::qualifiedName,
        BeanDefinition::simpleName
    )
}

private fun <T, K> topoSortLocal(
    items: Collection<T>,
    idSelector: (T) -> K,
    dependsOnSelector: (T) -> Collection<K>,
    ignoreMissingDependency: Boolean = false
): List<List<T>> {
    if (items.isEmpty()) {
        return emptyList()
    }

    val itemById = linkedMapOf<K, T>()
    items.forEach { item ->
        val id = idSelector(item)
        require(id !in itemById) { "Duplicate id: $id" }
        itemById[id] = item
    }

    val indegree = itemById.keys.associateWith { 0 }.toMutableMap()
    val outEdges = linkedMapOf<K, LinkedHashSet<K>>()

    itemById.forEach { (itemId, item) ->
        dependsOnSelector(item).forEach { dependencyId ->
            if (dependencyId !in itemById) {
                if (ignoreMissingDependency) return@forEach
                error("Missing dependency '$dependencyId' for '$itemId'")
            }
            val dependents = outEdges.getOrPut(dependencyId) { linkedSetOf() }
            if (dependents.add(itemId)) {
                indegree[itemId] = indegree.getValue(itemId) + 1
            }
        }
    }

    val currentLayerIds = ArrayDeque(indegree.filterValues { it == 0 }.keys)
    val layers = mutableListOf<List<T>>()
    var processedCount = 0

    while (currentLayerIds.isNotEmpty()) {
        val layerIds = mutableListOf<K>()
        while (currentLayerIds.isNotEmpty()) {
            layerIds += currentLayerIds.removeFirst()
        }

        val nextLayerIds = linkedSetOf<K>()
        val layerItems = mutableListOf<T>()

        layerIds.forEach { id ->
            val item = itemById.getValue(id)
            layerItems += item
            processedCount++

            outEdges[id].orEmpty().forEach { dependentId ->
                val newDegree = indegree.getValue(dependentId) - 1
                indegree[dependentId] = newDegree
                if (newDegree == 0) {
                    nextLayerIds += dependentId
                }
            }
        }

        layers += layerItems
        nextLayerIds.forEach { currentLayerIds.addLast(it) }
    }

    if (processedCount != itemById.size) {
        val cycleNodes = indegree.filterValues { it > 0 }.keys
        error("Cycle detected in dependsOn graph: $cycleNodes")
    }

    return layers
}

private fun <T, K> topoSortByDependsOnLocal(
    items: Collection<T>,
    idSelector: (T) -> K,
    dependsOnSelector: (T) -> Collection<K>,
    ignoreMissingDependency: Boolean = false
): List<T> {
    return topoSortLocal(
        items = items,
        idSelector = idSelector,
        dependsOnSelector = dependsOnSelector,
        ignoreMissingDependency = ignoreMissingDependency
    ).flatten()
}
