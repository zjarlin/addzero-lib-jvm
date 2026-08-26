package site.addzero.ddlgenerator.lsi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import site.addzero.ddlgenerator.core.model.AutoDdlLogicalType
import site.addzero.ddlgenerator.lsi.support.TestAnnotation
import site.addzero.ddlgenerator.lsi.support.TestClass
import site.addzero.ddlgenerator.lsi.support.TestField
import site.addzero.ddlgenerator.lsi.support.TestType

class LsiAutoDdlSchemaAdapterTest {

    @Test
    fun `expands Jimmer embeddable into leaf columns`() {
        val cacle = TestClass(
            simpleName = "CacleMixin",
            qualifiedName = "demo.CacleMixin",
            annotations = listOf(embeddable()),
            isInterface = true,
            fields = listOf(
                "mom" to "mom",
                "momElectric" to "mom_electric",
                "momWater" to "mom_water",
                "momBalance" to "mom_balance",
                "yoy" to "yoy",
                "yoyElectric" to "yoy_electric",
                "yoyWater" to "yoy_water",
                "yoyBalance" to "yoy_balance",
            ).map { (propertyName, columnName) ->
                TestField(
                    name = propertyName,
                    type = TestType("String"),
                    typeName = "String",
                    columnName = columnName,
                    isNullable = true,
                )
            },
        )
        val thresholdConfig = TestClass(
            simpleName = "IotThresholdConfigDO",
            qualifiedName = "demo.IotThresholdConfigDO",
            annotations = listOf(entity(), table("iot_threshold_config")),
            fields = listOf(
                TestField(
                    name = "id",
                    type = TestType("Long"),
                    typeName = "Long",
                    annotations = listOf(id()),
                ),
                TestField(
                    name = "cacle",
                    type = TestType("CacleMixin", qualifiedName = "demo.CacleMixin", lsiClass = cacle),
                    typeName = "CacleMixin",
                    fieldTypeClass = cacle,
                ),
            ),
        )

        val schema = LsiAutoDdlSchemaAdapter.from(listOf(thresholdConfig))
        val columns = schema.table("iot_threshold_config")?.columns.orEmpty()

        assertEquals(
            setOf(
                "id",
                "mom",
                "mom_electric",
                "mom_water",
                "mom_balance",
                "yoy",
                "yoy_electric",
                "yoy_water",
                "yoy_balance",
            ),
            columns.map { it.name }.toSet(),
        )
        assertTrue(columns.none { it.name == "cacle" })
        assertTrue(columns.filterNot { it.name == "id" }.all { it.logicalType == AutoDdlLogicalType.STRING })
    }

    @Test
    fun `recursively expands nullable Jimmer embeddable and applies prop override`() {
        val comparisonValues = TestClass(
            simpleName = "ComparisonValues",
            qualifiedName = "demo.ComparisonValues",
            annotations = listOf(embeddable()),
            isInterface = true,
            fields = listOf(
                TestField(
                    name = "electric",
                    type = TestType("String"),
                    typeName = "String",
                    columnName = "electric_value",
                ),
                TestField(
                    name = "water",
                    type = TestType("String"),
                    typeName = "String",
                    columnName = "water_value",
                ),
            ),
        )
        val statistics = TestClass(
            simpleName = "Statistics",
            qualifiedName = "demo.Statistics",
            annotations = listOf(embeddable()),
            isInterface = true,
            fields = listOf(
                TestField(
                    name = "values",
                    type = TestType(
                        "ComparisonValues",
                        qualifiedName = "demo.ComparisonValues",
                        lsiClass = comparisonValues,
                    ),
                    typeName = "ComparisonValues",
                    fieldTypeClass = comparisonValues,
                    isNullable = true,
                ),
            ),
        )
        val report = TestClass(
            simpleName = "Report",
            qualifiedName = "demo.Report",
            annotations = listOf(entity()),
            fields = listOf(
                TestField(
                    name = "id",
                    type = TestType("Long"),
                    typeName = "Long",
                    annotations = listOf(id()),
                ),
                TestField(
                    name = "statistics",
                    type = TestType("Statistics", qualifiedName = "demo.Statistics", lsiClass = statistics),
                    typeName = "Statistics",
                    fieldTypeClass = statistics,
                    annotations = listOf(
                        propOverride("values.electric", "mom_electric"),
                    ),
                ),
            ),
        )

        val schema = LsiAutoDdlSchemaAdapter.from(listOf(report))
        val reportTable = schema.table("report")
        assertNotNull(reportTable)

        assertEquals(setOf("id", "mom_electric", "water_value"), reportTable.columns.map { it.name }.toSet())
        assertEquals(true, reportTable.column("mom_electric")?.nullable)
        assertEquals(true, reportTable.column("water_value")?.nullable)
        assertTrue(reportTable.columns.none { it.name == "statistics" || it.name == "values" })
    }

    @Test
    fun `keeps serialized embeddable child as json column`() {
        val payload = TestClass(
            simpleName = "Payload",
            qualifiedName = "demo.Payload",
            annotations = listOf(embeddable()),
            isInterface = true,
            fields = listOf(
                TestField(
                    name = "content",
                    type = TestType("String"),
                    typeName = "String",
                ),
            ),
        )
        val envelope = TestClass(
            simpleName = "Envelope",
            qualifiedName = "demo.Envelope",
            annotations = listOf(embeddable()),
            isInterface = true,
            fields = listOf(
                TestField(
                    name = "payload",
                    type = TestType("Payload", qualifiedName = "demo.Payload", lsiClass = payload),
                    typeName = "Payload",
                    fieldTypeClass = payload,
                    annotations = listOf(serialized()),
                    columnName = "payload_json",
                ),
            ),
        )
        val event = TestClass(
            simpleName = "Event",
            qualifiedName = "demo.Event",
            annotations = listOf(entity()),
            fields = listOf(
                TestField(
                    name = "envelope",
                    type = TestType("Envelope", qualifiedName = "demo.Envelope", lsiClass = envelope),
                    typeName = "Envelope",
                    fieldTypeClass = envelope,
                ),
            ),
        )

        val schema = LsiAutoDdlSchemaAdapter.from(listOf(event))
        val columns = schema.table("event")?.columns.orEmpty()

        assertEquals(listOf("payload_json"), columns.map { it.name })
        assertEquals(AutoDdlLogicalType.JSON, columns.single().logicalType)
    }

    @Test
    fun `reads bean validation size max as explicit string length`() {
        val document = TestClass(
            simpleName = "Document",
            qualifiedName = "demo.Document",
            annotations = listOf(entity()),
            fields = listOf(
                TestField(
                    name = "title",
                    type = TestType("String"),
                    typeName = "String",
                    annotations = listOf(size(max = 128)),
                )
            ),
        )

        val schema = LsiAutoDdlSchemaAdapter.from(listOf(document))
        val title = schema.table("document")?.column("title")

        assertEquals(AutoDdlLogicalType.STRING, title?.logicalType)
        assertEquals(128, title?.length)
    }

    @Test
    fun `keeps Jimmer name enum on text fallback`() {
        val enumClass = TestClass(
            simpleName = "EnumKnowledgeType",
            qualifiedName = "demo.EnumKnowledgeType",
            annotations = listOf(enumType("NAME")),
            isEnum = true,
        )
        val document = TestClass(
            simpleName = "KnowledgeDocument",
            qualifiedName = "demo.KnowledgeDocument",
            annotations = listOf(entity()),
            fields = listOf(
                TestField(
                    name = "knowledgeType",
                    type = TestType("EnumKnowledgeType", lsiClass = enumClass),
                    typeName = "EnumKnowledgeType",
                    columnName = "knowledge_type",
                    fieldTypeClass = enumClass,
                    isEnum = true,
                )
            ),
        )

        val schema = LsiAutoDdlSchemaAdapter.from(listOf(document))

        assertEquals(AutoDdlLogicalType.UNKNOWN, schema.table("knowledge_document")?.column("knowledge_type")?.logicalType)
    }

    @Test
    fun `maps Jimmer ordinal enum to integer column`() {
        val enumClass = TestClass(
            simpleName = "EnumKnowledgeType",
            qualifiedName = "demo.EnumKnowledgeType",
            annotations = listOf(enumType("ORDINAL")),
            isEnum = true,
        )
        val document = TestClass(
            simpleName = "KnowledgeDocument",
            qualifiedName = "demo.KnowledgeDocument",
            annotations = listOf(entity()),
            fields = listOf(
                TestField(
                    name = "knowledgeType",
                    type = TestType("EnumKnowledgeType", lsiClass = enumClass),
                    typeName = "EnumKnowledgeType",
                    columnName = "knowledge_type",
                    fieldTypeClass = enumClass,
                    isEnum = true,
                )
            ),
        )

        val schema = LsiAutoDdlSchemaAdapter.from(listOf(document))

        assertEquals(AutoDdlLogicalType.INT32, schema.table("knowledge_document")?.column("knowledge_type")?.logicalType)
    }

    @Test
    fun `converts scalar columns foreign keys indexes and sequences`() {
        val customer = TestClass(
            simpleName = "Customer",
            qualifiedName = "demo.Customer",
            annotations = listOf(entity()),
            fields = listOf(
                TestField(
                    name = "id",
                    type = TestType("Long"),
                    typeName = "Long",
                    annotations = listOf(id())
                ),
                TestField(
                    name = "code",
                    type = TestType("String"),
                    typeName = "String",
                    annotations = listOf(key(group = "biz")),
                    columnName = "customer_code",
                )
            )
        )
        val order = TestClass(
            simpleName = "OrderRecord",
            qualifiedName = "demo.OrderRecord",
            annotations = listOf(entity(), table(name = "order_record")),
            comment = "订单",
            fields = listOf(
                TestField(
                    name = "id",
                    type = TestType("Long"),
                    typeName = "Long",
                    annotations = listOf(id(), generatedValue(strategy = "SEQUENCE", generatorName = "order_seq"))
                ),
                TestField(
                    name = "amount",
                    type = TestType("BigDecimal"),
                    typeName = "BigDecimal",
                    annotations = listOf(column(precision = 10, scale = 2))
                ),
                TestField(
                    name = "customer",
                    type = TestType("Customer", qualifiedName = "demo.Customer", lsiClass = customer),
                    typeName = "Customer",
                    fieldTypeClass = customer,
                    annotations = listOf(manyToOne(), joinColumn("customer_id"))
                )
            )
        )

        val schema = LsiAutoDdlSchemaAdapter.from(listOf(order, customer))
        val orderTable = schema.table("order_record")
        assertNotNull(orderTable)
        assertEquals(3, orderTable.columns.size)
        assertEquals(AutoDdlLogicalType.DECIMAL, orderTable.column("amount")?.logicalType)
        assertEquals("customer_id", orderTable.column("customer_id")?.name)
        assertEquals("customer", orderTable.foreignKeys.single().referencedTableName)
        assertEquals("order_seq", schema.sequences.single().name)

        val customerTable = schema.table("customer")
        assertNotNull(customerTable)
        assertEquals("uk_customer_biz", customerTable.indexes.single().name)
    }

    @Test
    fun `skips unique index for nullable key fields`() {
        val user = TestClass(
            simpleName = "User",
            qualifiedName = "demo.User",
            annotations = listOf(entity(), table("system_users")),
            fields = listOf(
                TestField(
                    name = "id",
                    type = TestType("Long"),
                    typeName = "Long",
                    annotations = listOf(id())
                ),
                TestField(
                    name = "username",
                    type = TestType("String"),
                    typeName = "String",
                    annotations = listOf(key(group = "username"))
                ),
                TestField(
                    name = "email",
                    type = TestType("String"),
                    typeName = "String",
                    annotations = listOf(key(group = "email")),
                    isNullable = true,
                )
            )
        )

        val schema = LsiAutoDdlSchemaAdapter.from(listOf(user))
        val table = schema.table("system_users")
        assertNotNull(table)

        assertEquals(listOf("uk_system_users_username"), table.indexes.map { index -> index.name })
    }

    @Test
    fun `converts joined inheritance into root and subtype physical tables`() {
        val root = TestClass(
            simpleName = "LogRecord",
            qualifiedName = "demo.LogRecord",
            annotations = listOf(entity(), table("system_log_record"), inheritance("JOINED")),
            fields = listOf(
                TestField(
                    name = "id",
                    type = TestType("Long"),
                    typeName = "Long",
                    annotations = listOf(id(), generatedValue(strategy = "IDENTITY")),
                ),
                TestField(
                    name = "recordType",
                    type = TestType("String"),
                    typeName = "String",
                    columnName = "record_type",
                ),
                TestField(
                    name = "userIp",
                    type = TestType("String"),
                    typeName = "String",
                    columnName = "user_ip",
                    isNullable = true,
                ),
            )
        )
        val operate = TestClass(
            simpleName = "OperateLog",
            qualifiedName = "demo.OperateLog",
            annotations = listOf(entity(), table("system_operate_log")),
            interfaces = listOf(root),
            fields = listOf(
                TestField(
                    name = "type",
                    type = TestType("String"),
                    typeName = "String",
                ),
                TestField(
                    name = "action",
                    type = TestType("String"),
                    typeName = "String",
                ),
            )
        )

        val schema = LsiAutoDdlSchemaAdapter.from(listOf(root, operate))

        val rootTable = schema.table("system_log_record")
        assertNotNull(rootTable)
        assertEquals(setOf("id", "record_type", "user_ip"), rootTable.columns.map { it.name }.toSet())
        assertEquals(true, rootTable.column("id")?.autoIncrement)

        val operateTable = schema.table("system_operate_log")
        assertNotNull(operateTable)
        assertEquals(setOf("id", "type", "action"), operateTable.columns.map { it.name }.toSet())
        assertEquals(true, operateTable.column("id")?.primaryKey)
        assertEquals(false, operateTable.column("id")?.autoIncrement)
        assertEquals("system_log_record", operateTable.foreignKeys.single().referencedTableName)
    }

    @Test
    fun `keeps mapped superclass fields in joined subtype physical table`() {
        val baseNode = TestClass(
            simpleName = "BaseNode",
            qualifiedName = "demo.BaseNode",
            annotations = listOf(entity(), table("iot_base_node"), inheritance("JOINED")),
            fields = listOf(
                TestField(
                    name = "id",
                    type = TestType("Long"),
                    typeName = "Long",
                    annotations = listOf(id()),
                ),
                TestField(
                    name = "name",
                    type = TestType("String"),
                    typeName = "String",
                ),
            ),
        )
        val lifecycle = TestClass(
            simpleName = "DeviceLifecycle",
            qualifiedName = "demo.DeviceLifecycle",
            annotations = listOf(mappedSuperclass()),
            fields = listOf(
                TestField(
                    name = "onlineTime",
                    type = TestType("LocalDateTime"),
                    typeName = "LocalDateTime",
                    columnName = "online_time",
                    isNullable = true,
                ),
            ),
        )
        val device = TestClass(
            simpleName = "Device",
            qualifiedName = "demo.Device",
            annotations = listOf(entity(), table("iot_device")),
            interfaces = listOf(baseNode, lifecycle),
            fields = baseNode.fields + lifecycle.fields + TestField(
                name = "deviceKey",
                type = TestType("String"),
                typeName = "String",
                columnName = "device_key",
                isNullable = true,
            ),
        )

        val schema = LsiAutoDdlSchemaAdapter.from(listOf(baseNode, device))

        val deviceTable = schema.table("iot_device")
        assertNotNull(deviceTable)
        assertEquals(setOf("id", "online_time", "device_key"), deviceTable.columns.map { it.name }.toSet())
    }

    @Test
    fun `scans owning many to many into pure junction table`() {
        val role = TestClass(
            simpleName = "Role",
            qualifiedName = "demo.Role",
            annotations = listOf(entity()),
            fields = listOf(TestField(name = "id", type = TestType("Long"), typeName = "Long", annotations = listOf(id())))
        )
        val user = TestClass(
            simpleName = "UserAccount",
            qualifiedName = "demo.UserAccount",
            annotations = listOf(entity()),
            fields = listOf(
                TestField(name = "id", type = TestType("Long"), typeName = "Long", annotations = listOf(id())),
                TestField(
                    name = "roles",
                    type = TestType(
                        simpleName = "List",
                        qualifiedName = "kotlin.collections.List",
                        isCollectionType = true,
                        typeParameters = listOf(TestType("Role", qualifiedName = "demo.Role", lsiClass = role))
                    ),
                    typeName = "List",
                    isCollectionType = true,
                    annotations = listOf(
                        manyToMany(),
                        joinTable(name = "user_role", joinColumnName = "user_id", inverseJoinColumnName = "role_id")
                    )
                )
            )
        )

        val junctionTables = LsiAutoDdlSchemaAdapter.scanManyToManyTables(listOf(user, role))
        val junction = junctionTables.single()
        assertEquals("user_role", junction.name)
        assertEquals(listOf("user_id", "role_id"), junction.columns.map { it.name })
        assertTrue(junction.foreignKeys.any { it.referencedTableName == "user_account" })
        assertTrue(junction.foreignKeys.any { it.referencedTableName == "role" })
    }

    @Test
    fun `adds join table filter column to junction primary key`() {
        val dept = TestClass(
            simpleName = "Dept",
            qualifiedName = "demo.Dept",
            annotations = listOf(entity()),
            fields = listOf(TestField(name = "id", type = TestType("Long"), typeName = "Long", annotations = listOf(id())))
        )
        val notice = TestClass(
            simpleName = "Notice",
            qualifiedName = "demo.Notice",
            annotations = listOf(entity()),
            fields = listOf(
                TestField(name = "id", type = TestType("Long"), typeName = "Long", annotations = listOf(id())),
                TestField(
                    name = "depts",
                    type = TestType(
                        simpleName = "List",
                        qualifiedName = "kotlin.collections.List",
                        isCollectionType = true,
                        typeParameters = listOf(TestType("Dept", qualifiedName = "demo.Dept", lsiClass = dept))
                    ),
                    typeName = "List",
                    isCollectionType = true,
                    annotations = listOf(
                        manyToMany(),
                        joinTable(
                            name = "biz_mapping",
                            joinColumnName = "from_id",
                            inverseJoinColumnName = "to_id",
                            filterColumnName = "mapping_type",
                        )
                    )
                )
            )
        )

        val junction = LsiAutoDdlSchemaAdapter.scanManyToManyTables(listOf(notice, dept)).single()

        assertEquals(listOf("from_id", "to_id", "mapping_type"), junction.columns.map { it.name })
        assertEquals(AutoDdlLogicalType.STRING, junction.column("mapping_type")?.logicalType)
        assertEquals(null, junction.column("mapping_type")?.length)
        assertEquals(false, junction.column("mapping_type")?.nullable)
        assertEquals(true, junction.column("mapping_type")?.primaryKey)
    }


    @Test
    fun `default many to many table name keeps DO suffix like Jimmer runtime`() {
        val user = TestClass(
            simpleName = "User",
            qualifiedName = "site.addzero.crud.model.system.user.User",
            annotations = listOf(entity(), table("system_users")),
            fields = listOf(TestField(name = "id", type = TestType("Long"), typeName = "Long", annotations = listOf(id())))
        )
        val project = TestClass(
            simpleName = "IotProjectDO",
            qualifiedName = "cn.iocoder.yudao.module.iot.project.IotProjectDO",
            annotations = listOf(entity(), table("iot_project")),
            fields = listOf(
                TestField(name = "id", type = TestType("Long"), typeName = "Long", annotations = listOf(id())),
                TestField(
                    name = "users",
                    type = TestType(
                        simpleName = "List",
                        qualifiedName = "kotlin.collections.List",
                        isCollectionType = true,
                        typeParameters = listOf(TestType("User", qualifiedName = "site.addzero.crud.model.system.user.User", lsiClass = user))
                    ),
                    typeName = "List",
                    isCollectionType = true,
                    annotations = listOf(manyToMany())
                )
            )
        )

        val junctionTables = LsiAutoDdlSchemaAdapter.scanManyToManyTables(listOf(project, user))
        val junction = junctionTables.single()
        assertEquals("iot_project_do_user_mapping", junction.name)
        assertEquals(listOf("iot_project_do_id", "user_id"), junction.columns.map { it.name })
    }

    @Test
    fun `default many to many table name uses target entity instead of property name`() {
        val device = TestClass(
            simpleName = "Device",
            qualifiedName = "demo.Device",
            annotations = listOf(entity()),
            fields = listOf(TestField(name = "id", type = TestType("Long"), typeName = "Long", annotations = listOf(id())))
        )
        val rulePlan = TestClass(
            simpleName = "RulePlan",
            qualifiedName = "demo.RulePlan",
            annotations = listOf(entity()),
            fields = listOf(
                TestField(name = "id", type = TestType("Long"), typeName = "Long", annotations = listOf(id())),
                TestField(
                    name = "topologyDevices",
                    type = TestType(
                        simpleName = "List",
                        qualifiedName = "kotlin.collections.List",
                        isCollectionType = true,
                        typeParameters = listOf(TestType("Device", qualifiedName = "demo.Device", lsiClass = device))
                    ),
                    typeName = "List",
                    isCollectionType = true,
                    annotations = listOf(manyToMany())
                )
            )
        )

        val junction = LsiAutoDdlSchemaAdapter.scanManyToManyTables(listOf(rulePlan, device)).single()

        assertEquals("rule_plan_device_mapping", junction.name)
        assertEquals(listOf("rule_plan_id", "device_id"), junction.columns.map { it.name })
    }

    @Test
    fun `scans inherited owning many to many when target entity comes from dependency module`() {
        val user = TestClass(
            simpleName = "User",
            qualifiedName = "site.addzero.crud.model.system.user.User",
            annotations = listOf(entity(), table("system_users")),
            fields = listOf(TestField(name = "id", type = TestType("Long"), typeName = "Long", annotations = listOf(id())))
        )
        val basePersonInCharge = TestClass(
            simpleName = "BasePersonInCharge",
            qualifiedName = "cn.iocoder.yudao.module.ai.power.equipment_information_archive.entity.BasePersonInCharge",
            annotations = listOf(mappedSuperclass()),
            fields = listOf(
                TestField(
                    name = "personInCharge",
                    type = TestType(
                        simpleName = "List",
                        qualifiedName = "kotlin.collections.List",
                        isCollectionType = true,
                        typeParameters = listOf(
                            TestType(
                                simpleName = "User",
                                qualifiedName = "site.addzero.crud.model.system.user.User",
                                lsiClass = user,
                            )
                        )
                    ),
                    typeName = "List",
                    isCollectionType = true,
                    annotations = listOf(manyToMany())
                )
            )
        )
        val device = TestClass(
            simpleName = "EquipmentInformationArchive",
            qualifiedName = "cn.iocoder.yudao.module.ai.power.equipment_information_archive.entity.EquipmentInformationArchive",
            annotations = listOf(entity(), table("ai_power_device")),
            fields = listOf(TestField(name = "id", type = TestType("Long"), typeName = "Long", annotations = listOf(id()))),
            interfaces = listOf(basePersonInCharge),
        )

        val baseSchema = LsiAutoDdlSchemaAdapter.from(listOf(device))
        val junctionTables = LsiAutoDdlSchemaAdapter.scanManyToManyTables(
            owningClasses = listOf(device),
            targetClasses = listOf(user),
        )

        assertEquals(1, baseSchema.tables.size)
        assertEquals("ai_power_device", baseSchema.tables.single().name)
        val junction = junctionTables.single()
        assertEquals("equipment_information_archive_user_mapping", junction.name)
        assertEquals(listOf("equipment_information_archive_id", "user_id"), junction.columns.map { it.name })
        assertTrue(junction.foreignKeys.any { it.referencedTableName == "ai_power_device" })
        assertTrue(junction.foreignKeys.any { it.referencedTableName == "system_users" })
    }

    @Test
    fun `scans mapped by many to many using jimmer default mapping table name`() {
        val user = TestClass(
            simpleName = "User",
            qualifiedName = "site.addzero.crud.model.system.user.User",
            annotations = listOf(entity(), table("system_users")),
            fields = listOf(TestField(name = "id", type = TestType("Long"), typeName = "Long", annotations = listOf(id())))
        )
        val dept = TestClass(
            simpleName = "Dept",
            qualifiedName = "site.addzero.crud.model.system.dept.Dept",
            annotations = listOf(entity(), table("system_dept")),
            fields = listOf(
                TestField(name = "id", type = TestType("Long"), typeName = "Long", annotations = listOf(id())),
                TestField(
                    name = "users",
                    type = TestType(
                        simpleName = "List",
                        qualifiedName = "kotlin.collections.List",
                        isCollectionType = true,
                        typeParameters = listOf(
                            TestType(
                                simpleName = "User",
                                qualifiedName = "site.addzero.crud.model.system.user.User",
                                lsiClass = user,
                            )
                        )
                    ),
                    typeName = "List",
                    isCollectionType = true,
                    annotations = listOf(manyToMany(mappedBy = "depts"))
                )
            )
        )

        val junctionTables = LsiAutoDdlSchemaAdapter.scanManyToManyTables(listOf(user, dept))

        val junction = junctionTables.single()
        assertEquals("user_dept_mapping", junction.name)
        assertEquals(listOf("user_id", "dept_id"), junction.columns.map { it.name })
        assertTrue(junction.foreignKeys.any { it.referencedTableName == "system_users" })
        assertTrue(junction.foreignKeys.any { it.referencedTableName == "system_dept" })
    }

    @Test
    fun `mapped by many to many reuses explicit join table from owning property`() {
        val user = TestClass(
            simpleName = "User",
            qualifiedName = "demo.User",
            annotations = listOf(entity()),
            fields = listOf(
                TestField(name = "id", type = TestType("Long"), typeName = "Long", annotations = listOf(id())),
                TestField(
                    name = "categories",
                    type = TestType(
                        simpleName = "List",
                        qualifiedName = "kotlin.collections.List",
                        isCollectionType = true,
                        typeParameters = listOf(TestType("Category", qualifiedName = "demo.Category"))
                    ),
                    typeName = "List",
                    isCollectionType = true,
                    annotations = listOf(
                        manyToMany(),
                        joinTable(
                            name = "user_category_mapping",
                            joinColumnName = "user_id",
                            inverseJoinColumnName = "category_id",
                        ),
                    ),
                ),
            ),
        )
        val category = TestClass(
            simpleName = "Category",
            qualifiedName = "demo.Category",
            annotations = listOf(entity()),
            fields = listOf(
                TestField(name = "id", type = TestType("Long"), typeName = "Long", annotations = listOf(id())),
                TestField(
                    name = "users",
                    type = TestType(
                        simpleName = "List",
                        qualifiedName = "kotlin.collections.List",
                        isCollectionType = true,
                        typeParameters = listOf(TestType("User", qualifiedName = "demo.User", lsiClass = user))
                    ),
                    typeName = "List",
                    isCollectionType = true,
                    annotations = listOf(manyToMany(mappedBy = "categories")),
                ),
            ),
        )

        val junctionTables = LsiAutoDdlSchemaAdapter.scanManyToManyTables(listOf(user, category))

        assertEquals(listOf("user_category_mapping"), junctionTables.map { table -> table.name })
        assertEquals(listOf("user_id", "category_id"), junctionTables.single().columns.map { column -> column.name })
    }

    private fun entity(): TestAnnotation {
        return TestAnnotation("org.babyfish.jimmer.sql.Entity", "Entity")
    }

    private fun embeddable(): TestAnnotation {
        return TestAnnotation("org.babyfish.jimmer.sql.Embeddable", "Embeddable")
    }

    private fun propOverride(prop: String, columnName: String): TestAnnotation {
        return TestAnnotation(
            "org.babyfish.jimmer.sql.PropOverride",
            "PropOverride",
            mapOf(
                "prop" to prop,
                "columnName" to columnName,
            ),
        )
    }

    private fun serialized(): TestAnnotation {
        return TestAnnotation("org.babyfish.jimmer.sql.Serialized", "Serialized")
    }

    private fun enumType(strategy: String): TestAnnotation {
        return TestAnnotation("org.babyfish.jimmer.sql.EnumType", "EnumType", mapOf("value" to strategy))
    }

    private fun mappedSuperclass(): TestAnnotation {
        return TestAnnotation("org.babyfish.jimmer.sql.MappedSuperclass", "MappedSuperclass")
    }

    private fun table(name: String): TestAnnotation {
        return TestAnnotation("org.babyfish.jimmer.sql.Table", "Table", mapOf("name" to name))
    }

    private fun id(): TestAnnotation {
        return TestAnnotation("org.babyfish.jimmer.sql.Id", "Id")
    }

    private fun key(group: String? = null): TestAnnotation {
        return TestAnnotation("org.babyfish.jimmer.sql.Key", "Key", buildMap {
            if (group != null) {
                put("group", group)
            }
        })
    }

    private fun size(max: Int): TestAnnotation {
        return TestAnnotation("jakarta.validation.constraints.Size", "Size", mapOf("max" to max))
    }

    private fun column(
        precision: Int? = null,
        scale: Int? = null,
    ): TestAnnotation {
        return TestAnnotation("org.babyfish.jimmer.sql.Column", "Column", buildMap {
            if (precision != null) {
                put("precision", precision)
            }
            if (scale != null) {
                put("scale", scale)
            }
        })
    }

    private fun generatedValue(
        strategy: String,
        generatorName: String = "",
        sequenceName: String = generatorName,
        generatorType: String? = null,
        generatorRef: String? = null,
    ): TestAnnotation {
        return TestAnnotation(
            "org.babyfish.jimmer.sql.GeneratedValue",
            "GeneratedValue",
            buildMap {
                put("strategy", strategy)
                if (generatorName.isNotBlank()) {
                    put("generatorName", generatorName)
                }
                if (sequenceName.isNotBlank()) {
                    put("sequenceName", sequenceName)
                }
                if (generatorType != null) {
                    put("generatorType", generatorType)
                }
                if (generatorRef != null) {
                    put("generatorRef", generatorRef)
                }
            }
        )
    }

    private fun inheritance(strategy: String): TestAnnotation {
        return TestAnnotation("org.babyfish.jimmer.sql.Inheritance", "Inheritance", mapOf("strategy" to strategy))
    }

    private fun manyToOne(): TestAnnotation {
        return TestAnnotation("org.babyfish.jimmer.sql.ManyToOne", "ManyToOne")
    }

    private fun joinColumn(name: String): TestAnnotation {
        return TestAnnotation(
            "org.babyfish.jimmer.sql.JoinColumn",
            "JoinColumn",
            mapOf("name" to name, "referencedColumnName" to "id")
        )
    }

    private fun manyToMany(mappedBy: String? = null): TestAnnotation {
        return TestAnnotation("org.babyfish.jimmer.sql.ManyToMany", "ManyToMany", buildMap {
            if (mappedBy != null) {
                put("mappedBy", mappedBy)
            }
        })
    }

    private fun joinTable(
        name: String,
        joinColumnName: String,
        inverseJoinColumnName: String,
        filterColumnName: String? = null,
    ): TestAnnotation {
        return TestAnnotation(
            "org.babyfish.jimmer.sql.JoinTable",
            "JoinTable",
            buildMap {
                put("name", name)
                put("joinColumnName", joinColumnName)
                put("inverseJoinColumnName", inverseJoinColumnName)
                filterColumnName?.let { columnName ->
                    put(
                        "filter",
                        TestAnnotation(
                            "org.babyfish.jimmer.sql.JoinTable.JoinTableFilter",
                            "JoinTableFilter",
                            mapOf(
                                "columnName" to columnName,
                                "values" to listOf("notice_dept"),
                            ),
                        )
                    )
                }
            }
        )
    }
}
