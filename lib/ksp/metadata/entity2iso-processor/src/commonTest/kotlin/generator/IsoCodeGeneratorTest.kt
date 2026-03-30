package generator

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityMeta
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerPropertyMeta
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerTypeKind
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerTypeRef

class IsoCodeGeneratorTest {
    @Test
    fun `generator respects package and class suffix`() {
        val code = IsoCodeGenerator.generateIsoCode(
            entity = JimmerEntityMeta(
                qualifiedName = "demo.domain.DeviceProfile",
                packageName = "demo.domain",
                simpleName = "DeviceProfile",
                properties = listOf(
                    JimmerPropertyMeta(
                        name = "deviceKey",
                        type = JimmerTypeRef(
                            qualifiedName = "kotlin.String",
                            simpleName = "String",
                            kind = JimmerTypeKind.BASIC,
                        ),
                    ),
                )
            ),
            packageName = "demo.generated.iso",
            classSuffix = "Snapshot",
        )

        assertContains(code, "package demo.generated.iso")
        assertContains(code, "data class DeviceProfileSnapshot(")
        assertContains(code, "val deviceKey: String = \"\"")
    }

    @Test
    fun `generator emits entity and property kdoc`() {
        val code = IsoCodeGenerator.generateIsoCode(
            entity = JimmerEntityMeta(
                qualifiedName = "demo.domain.DeviceProfile",
                packageName = "demo.domain",
                simpleName = "DeviceProfile",
                docComment = "设备档案",
                properties = listOf(
                    JimmerPropertyMeta(
                        name = "deviceKey",
                        docComment = "设备唯一键",
                        type = JimmerTypeRef(
                            qualifiedName = "kotlin.String",
                            simpleName = "String",
                            kind = JimmerTypeKind.BASIC,
                        ),
                    ),
                )
            ),
            packageName = "demo.generated.iso",
            classSuffix = "Iso",
        )

        assertContains(code, "/**\n * 设备档案\n */\n@Serializable\ndata class DeviceProfileIso(")
        assertContains(code, "    /**\n     * 设备唯一键\n     */\n    val deviceKey: String = \"\"")
    }

    @Test
    fun `generator maps nested entity collection enum nullable and time fields`() {
        val code = IsoCodeGenerator.generateIsoCode(
            entity = JimmerEntityMeta(
                qualifiedName = "demo.domain.Parent",
                packageName = "demo.domain",
                simpleName = "Parent",
                properties = listOf(
                    JimmerPropertyMeta(
                        name = "child",
                        type = JimmerTypeRef(
                            qualifiedName = "demo.domain.Child",
                            simpleName = "Child",
                            kind = JimmerTypeKind.ENTITY,
                        ),
                    ),
                    JimmerPropertyMeta(
                        name = "children",
                        type = JimmerTypeRef(
                            qualifiedName = "kotlin.collections.List",
                            simpleName = "List",
                            kind = JimmerTypeKind.COLLECTION,
                            typeArguments = listOf(
                                JimmerTypeRef(
                                    qualifiedName = "demo.domain.Child",
                                    simpleName = "Child",
                                    kind = JimmerTypeKind.ENTITY,
                                ),
                            ),
                        ),
                    ),
                    JimmerPropertyMeta(
                        name = "status",
                        type = JimmerTypeRef(
                            qualifiedName = "demo.domain.ParentStatus",
                            simpleName = "ParentStatus",
                            kind = JimmerTypeKind.ENUM,
                        ),
                    ),
                    JimmerPropertyMeta(
                        name = "createdAt",
                        type = JimmerTypeRef(
                            qualifiedName = "java.time.LocalDateTime",
                            simpleName = "LocalDateTime",
                            kind = JimmerTypeKind.DATE_TIME,
                        ),
                    ),
                    JimmerPropertyMeta(
                        name = "remark",
                        type = JimmerTypeRef(
                            qualifiedName = "kotlin.String",
                            simpleName = "String",
                            nullable = true,
                            kind = JimmerTypeKind.BASIC,
                        ),
                    ),
                ),
            ),
            packageName = "demo.generated.iso",
            classSuffix = "Iso",
        )

        assertContains(code, "val child: ChildIso = ChildIso()")
        assertContains(code, "val children: List<ChildIso> = emptyList()")
        assertContains(code, "val status: ParentStatus = ParentStatus.entries.first()")
        assertContains(
            code,
            "@Contextual val createdAt: LocalDateTime = kotlinx.datetime.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())",
        )
        assertContains(code, "val remark: String? = null")
        assertContains(code, "import demo.domain.ParentStatus")
        assertContains(code, "import kotlinx.datetime.LocalDateTime")
    }

    @Test
    fun `generator can disable serialization annotations`() {
        val code = IsoCodeGenerator.generateIsoCode(
            entity = JimmerEntityMeta(
                qualifiedName = "demo.domain.Device",
                packageName = "demo.domain",
                simpleName = "Device",
                properties = listOf(
                    JimmerPropertyMeta(
                        name = "lastSeenAt",
                        type = JimmerTypeRef(
                            qualifiedName = "java.time.Instant",
                            simpleName = "Instant",
                            kind = JimmerTypeKind.DATE_TIME,
                        ),
                    ),
                ),
            ),
            packageName = "demo.generated.iso",
            classSuffix = "Iso",
            serializableEnabled = false,
        )

        assertFalse(code.contains("@Serializable"))
        assertFalse(code.contains("@Contextual"))
        assertFalse(code.contains("import kotlinx.serialization.Serializable"))
        assertContains(code, "val lastSeenAt: Instant = kotlinx.datetime.Clock.System.now()")
    }

    @Test
    fun `generator normalizes malformed primitive token and instant default`() {
        val code = IsoCodeGenerator.generateIsoCode(
            entity = JimmerEntityMeta(
                qualifiedName = "demo.domain.Device",
                packageName = "demo.domain",
                simpleName = "Device",
                properties = listOf(
                    JimmerPropertyMeta(
                        name = "id",
                        type = JimmerTypeRef(
                            qualifiedName = null,
                            simpleName = "Long]",
                            kind = JimmerTypeKind.OTHER,
                        ),
                    ),
                    JimmerPropertyMeta(
                        name = "lastSeenAt",
                        type = JimmerTypeRef(
                            qualifiedName = "java.time.Instant",
                            simpleName = "Instant",
                            kind = JimmerTypeKind.DATE_TIME,
                        ),
                    ),
                ),
            ),
            packageName = "demo.generated.iso",
            classSuffix = "Iso",
        )

        assertContains(code, "val id: Long = 0L")
        assertContains(code, "@Contextual val lastSeenAt: Instant = kotlinx.datetime.Clock.System.now()")
        assertFalse(code.contains("import kotlin.time.Clock"))
    }
}
