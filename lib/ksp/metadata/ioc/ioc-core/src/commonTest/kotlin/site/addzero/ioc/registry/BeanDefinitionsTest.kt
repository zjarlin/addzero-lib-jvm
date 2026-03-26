package site.addzero.ioc.registry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private interface OrderedPlugin

private class FirstPlugin : OrderedPlugin

private class SecondPlugin : OrderedPlugin

class BeanDefinitionsTest {

    @Test
    fun `resolve tag also includes dependsOn in topo order`() {
        val definitions = listOf(
            BeanDefinition(
                simpleName = "CoreInit",
                qualifiedName = "demo.CoreInit",
                beanName = "coreInit",
                tags = listOf("infra"),
                order = 99
            ),
            BeanDefinition(
                simpleName = "FeatureInit",
                qualifiedName = "demo.FeatureInit",
                beanName = "featureInit",
                tags = listOf("feature"),
                order = 1,
                dependsOn = listOf("coreInit")
            ),
            BeanDefinition(
                simpleName = "FeatureTail",
                qualifiedName = "demo.FeatureTail",
                beanName = "featureTail",
                tags = listOf("feature"),
                order = 0,
                dependsOn = listOf("featureInit")
            )
        )

        val resolved = BeanDefinitions.resolve(definitions, tag = "feature")

        assertEquals(
            listOf("coreInit", "featureInit", "featureTail"),
            resolved.map { it.beanName }
        )
    }

    @Test
    fun `registry uses bean definitions for tag filtering and order`() {
        val registry = KmpBeanRegistry()

        registry.registerProvider(FirstPlugin::class) { FirstPlugin() }
        registry.registerProvider(SecondPlugin::class) { SecondPlugin() }
        registry.registerDefinition(
            FirstPlugin::class,
            BeanDefinition(
                simpleName = "FirstPlugin",
                qualifiedName = "demo.FirstPlugin",
                beanName = "firstPlugin",
                tags = listOf("plugin"),
                order = 2
            )
        )
        registry.registerDefinition(
            SecondPlugin::class,
            BeanDefinition(
                simpleName = "SecondPlugin",
                qualifiedName = "demo.SecondPlugin",
                beanName = "secondPlugin",
                tags = listOf("plugin"),
                order = 1
            )
        )
        registry.registerImplementation(OrderedPlugin::class, FirstPlugin::class)
        registry.registerImplementation(OrderedPlugin::class, SecondPlugin::class)

        val plugins = registry.injectList(OrderedPlugin::class, "plugin")

        assertEquals(
            listOf(SecondPlugin::class, FirstPlugin::class),
            plugins.map { it::class }
        )
        assertTrue(registry.beanDefinition("firstPlugin") != null)
        assertEquals(2, registry.beanDefinitions("plugin").size)
    }

    @Test
    fun `registry exposes getBean and injectList aliases`() {
        val registry = KmpBeanRegistry()

        registry.registerProvider(FirstPlugin::class) { FirstPlugin() }
        registry.registerDefinition(
            FirstPlugin::class,
            BeanDefinition(
                simpleName = "FirstPlugin",
                qualifiedName = "demo.FirstPlugin",
                beanName = "firstPlugin",
                tags = listOf("plugin"),
                order = 1
            )
        )
        registry.registerImplementation(OrderedPlugin::class, FirstPlugin::class)

        val bean = registry.getBean<FirstPlugin>()
        val beans = registry.injectList<OrderedPlugin>()
        val taggedBeans = registry.injectList<OrderedPlugin>("plugin")

        assertNotNull(bean)
        assertEquals(listOf(FirstPlugin::class), beans.map { it::class })
        assertEquals(listOf(FirstPlugin::class), taggedBeans.map { it::class })
    }
}
