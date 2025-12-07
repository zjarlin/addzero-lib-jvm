package site.addzero.tool.template.jte

import gg.jte.ContentType
import gg.jte.TemplateEngine
import gg.jte.output.StringOutput
import gg.jte.resolve.DirectoryCodeResolver
import gg.jte.resolve.ResourceCodeResolver
import java.nio.file.Path

class JteTemplateEngine private constructor(
    private val engine: TemplateEngine,
    val contentType: ContentType
) {
    fun <T> render(templateName: String, model: T): String =
        StringOutput().also { engine.render(templateName, model, it) }.toString()

    fun renderWithMap(templateName: String, params: Map<String, Any?>): String =
        StringOutput().also { engine.render(templateName, params, it) }.toString()

    fun <T> renderOrNull(templateName: String, model: T): String? = runCatching {
        render(templateName, model)
    }.getOrNull()

    fun <T> renderOrElse(templateName: String, model: T, default: () -> String): String =
        renderOrNull(templateName, model) ?: default()

    fun hasTemplate(templateName: String): Boolean = runCatching {
        engine.prepareForRendering(templateName)
        true
    }.getOrDefault(false)

    companion object {
        fun fromDirectory(
            templateDir: Path,
            contentType: ContentType = ContentType.Plain
        ): JteTemplateEngine = JteTemplateEngine(
            TemplateEngine.create(DirectoryCodeResolver(templateDir), contentType),
            contentType
        )

        fun fromResource(
            resourcePath: String = "",
            contentType: ContentType = ContentType.Plain
        ): JteTemplateEngine = JteTemplateEngine(
            TemplateEngine.create(ResourceCodeResolver(resourcePath), contentType),
            contentType
        )

        fun precompiled(contentType: ContentType = ContentType.Plain): JteTemplateEngine =
            JteTemplateEngine(TemplateEngine.createPrecompiled(contentType), contentType)

        inline fun <reified T> render(
            templateDir: Path,
            templateName: String,
            model: T,
            contentType: ContentType = ContentType.Plain
        ): String = fromDirectory(templateDir, contentType).render(templateName, model)
    }
}
