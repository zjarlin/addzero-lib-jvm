package site.addzero.tool.kotlinscript

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KotlinScriptTemplateUtilTest {

    @Test
    fun `fillTemplate replaces metadata placeholders in kts template`() {
        val template = """
            plugins {
                kotlin(\"jvm\") version \"{{kotlinVersion}}\"
            }
            group = \"{{groupId}}\"
            version = \"{{version}}\"
            repositories {
                mavenCentral()
            }
            dependencies {
                testImplementation(\"org.junit.jupiter:junit-jupiter-api:{{junitVersion}}\")
            }
        """.trimIndent()

        val ctx = KotlinScriptTemplateCtx(
            mapOf(
                "kotlinVersion" to "2.1.0",
                "groupId" to "site.addzero",
                "version" to "1.2.3",
                "junitVersion" to "5.10.2"
            )
        )

        val result = KotlinScriptTemplateUtil.fillTemplate(template, ctx)

        assertTrue("{{" !in result)
        assertEquals(
            """
                plugins {
                    kotlin(\"jvm\") version \"2.1.0\"
                }
                group = \"site.addzero\"
                version = \"1.2.3\"
                repositories {
                    mavenCentral()
                }
                dependencies {
                    testImplementation(\"org.junit.jupiter:junit-jupiter-api:5.10.2\")
                }
            """.trimIndent(),
            result
        )
    }

    @Test
    fun `fillTemplate supports nested metadata and missing keys`() {
        val template = """
            rootProject.name = \"{{project.name}}\"
            description = \"{{project.description}}\"
            missing = \"{{project.missing}}\"
        """.trimIndent()

        val ctx = KotlinScriptTemplateCtx(
            mapOf(
                "project" to mapOf(
                    "name" to "kts-demo",
                    "description" to "Kotlin script template"
                )
            )
        )

        val result = KotlinScriptTemplateUtil.fillTemplate(template, ctx, keepUnknown = true)

        assertEquals(
            """
                rootProject.name = \"kts-demo\"
                description = \"Kotlin script template\"
                missing = \"{{project.missing}}\"
            """.trimIndent(),
            result
        )
    }

    @Test
    fun `fillTemplate can drop missing keys`() {
        val template = """
            project.version = \"{{version}}\"
            project.group = \"{{group}}\"
        """.trimIndent()

        val result = KotlinScriptTemplateUtil.fillTemplate(
            template,
            mapOf("version" to null),
            keepUnknown = false
        )

        assertEquals(
            """
                project.version = \"\"
                project.group = \"\"
            """.trimIndent(),
            result
        )
    }

    @Test
    fun `evalTemplate executes kotlin string template`() {
        val template = "Hello, \${ctx[\"name\"]}!"

        val result = KotlinScriptTemplateUtil.evalTemplate(
            template,
            mapOf("name" to "World")
        )

        assertEquals("Hello, World!", result)
    }

    @Test
    fun `evalTemplate supports dot path access in ctx`() {
        val template = "Hello, \${ctx[\"user.name\"]}!"

        val result = KotlinScriptTemplateUtil.evalTemplate(
            template,
            mapOf("user" to mapOf("name" to "Ada"))
        )

        assertEquals("Hello, Ada!", result)
    }

    @Test
    fun `evalScriptText supports split trim distinct logic`() {
        val script = """
            val raw = ctx["value"] as? String
            val items = raw
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?: emptyList()
            val unique = items.distinct()
            unique.joinToString("|")
        """.trimIndent()

        val result = KotlinScriptTemplateUtil.evalScriptText(
            script,
            mapOf("value" to " a, b,, a ,  ,c ")
        )

        assertEquals("a|b|c", result)
    }
}
