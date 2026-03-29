package site.addzero.ksp.jdbc2enum.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property
import site.addzero.gradle.kspconsumer.AbstractPublishedKspConsumerPlugin
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact

abstract class Jdbc2EnumExtension {
    abstract val enumOutputPackage: Property<String>
    abstract val sharedSourceDir: Property<String>
    abstract val jdbcDriver: Property<String>
    abstract val jdbcUrl: Property<String>
    abstract val jdbcUsername: Property<String>
    abstract val jdbcPassword: Property<String>
    abstract val dictTableName: Property<String>
    abstract val dictIdColumn: Property<String>
    abstract val dictCodeColumn: Property<String>
    abstract val dictNameColumn: Property<String>
    abstract val dictItemTableName: Property<String>
    abstract val dictItemForeignKeyColumn: Property<String>
    abstract val dictItemCodeColumn: Property<String>
    abstract val dictItemNameColumn: Property<String>

    init {
        enumOutputPackage.convention("site.addzero.generated.enums")
        sharedSourceDir.convention("")
        jdbcDriver.convention("org.postgresql.Driver")
        jdbcUrl.convention("")
        jdbcUsername.convention("")
        jdbcPassword.convention("")
        dictTableName.convention("sys_dict")
        dictIdColumn.convention("id")
        dictCodeColumn.convention("dict_code")
        dictNameColumn.convention("dict_name")
        dictItemTableName.convention("sys_dict_item")
        dictItemForeignKeyColumn.convention("dict_id")
        dictItemCodeColumn.convention("item_value")
        dictItemNameColumn.convention("item_text")
    }
}

class Jdbc2EnumGradlePlugin : AbstractPublishedKspConsumerPlugin() {
    override val pluginId: String = PLUGIN_ID
    override val coordinatesResourcePath: String = COORDINATES_RESOURCE_PATH
    override val processorArtifact: PublishedProcessorArtifact =
        PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.KMP,
            localProjectPath = ":lib:ksp:jdbc2metadata:jdbc2enum-processor",
            artifactId = PROCESSOR_ARTIFACT_ID,
        )

    override fun createExtension(project: Project): Any {
        return createTypedExtension(project, EXTENSION_NAME, Jdbc2EnumExtension::class.java)
    }

    override fun collectKspArgs(project: Project, extension: Any?): Map<String, String> {
        val jdbc2Enum = extension as Jdbc2EnumExtension
        return linkedMapOf<String, String>().apply {
            put("enumOutputPackage", jdbc2Enum.enumOutputPackage.get())
            put(
                "sharedSourceDir",
                jdbc2Enum.sharedSourceDir.orNull?.takeIf(String::isNotBlank) ?: defaultSourceDirectory(project),
            )
            put("jdbcDriver", jdbc2Enum.jdbcDriver.get())
            jdbc2Enum.jdbcUrl.orNull?.takeIf(String::isNotBlank)?.let { put("jdbcUrl", it) }
            jdbc2Enum.jdbcUsername.orNull?.takeIf(String::isNotBlank)?.let { put("jdbcUsername", it) }
            jdbc2Enum.jdbcPassword.orNull?.takeIf(String::isNotBlank)?.let { put("jdbcPassword", it) }
            put("dictTableName", jdbc2Enum.dictTableName.get())
            put("dictIdColumn", jdbc2Enum.dictIdColumn.get())
            put("dictCodeColumn", jdbc2Enum.dictCodeColumn.get())
            put("dictNameColumn", jdbc2Enum.dictNameColumn.get())
            put("dictItemTableName", jdbc2Enum.dictItemTableName.get())
            put("dictItemForeignKeyColumn", jdbc2Enum.dictItemForeignKeyColumn.get())
            put("dictItemCodeColumn", jdbc2Enum.dictItemCodeColumn.get())
            put("dictItemNameColumn", jdbc2Enum.dictItemNameColumn.get())
        }
    }

    companion object {
        const val PLUGIN_ID: String = "site.addzero.ksp.jdbc2enum"
        const val EXTENSION_NAME: String = "jdbc2enum"
        const val PROCESSOR_ARTIFACT_ID: String = "jdbc2enum-processor"
        const val COORDINATES_RESOURCE_PATH: String =
            "site/addzero/ksp/jdbc2enum/gradle-plugin.properties"
    }
}
