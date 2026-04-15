package site.addzero.device.protocol.modbus.ksp.core

import com.google.devtools.ksp.processing.KSPLogger
import java.sql.DriverManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModbusDatabaseMetadataProviderTest {
    private val provider = ModbusDatabaseMetadataProvider()

    @Test
    fun decodeServicesSupportsEnvelopePayload() {
        val services =
            provider.decodeServices(
                """
                {
                  "services": [
                    {
                      "interfacePackage": "site.addzero.device.contract",
                      "interfaceSimpleName": "DeviceApi",
                      "transport": "rtu",
                      "operations": []
                    }
                  ]
                }
                """.trimIndent(),
            )

        assertEquals(1, services.size)
        assertEquals("DeviceApi", services.single().interfaceSimpleName)
    }

    @Test
    fun collectFromDatabaseBuildsNormalizedServiceModels() {
        val jdbcUrl = "jdbc:sqlite:file:modbus-ksp-core-test?mode=memory&cache=shared"
        DriverManager.getConnection(jdbcUrl).use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("create table metadata_store (payload text not null)")
            }
            connection.prepareStatement("insert into metadata_store(payload) values (?)").use { statement ->
                statement.setString(
                    1,
                    """
                    {
                      "interfacePackage": "site.addzero.device.contract",
                      "interfaceSimpleName": "DeviceApi",
                      "transport": "rtu",
                      "doc": {
                        "summary": "设备接口。"
                      },
                      "operations": [
                        {
                          "methodName": "getDeviceRuntimeInfo",
                          "address": 100,
                          "parameters": [],
                          "returnType": {
                            "qualifiedName": "site.addzero.device.contract.DeviceRuntimeInfo",
                            "simpleName": "DeviceRuntimeInfo",
                            "kind": "DTO",
                            "properties": [
                              {
                                "name": "protocolVersion",
                                "qualifiedType": "kotlin.Int",
                                "valueKind": "INT",
                                "field": {
                                  "codecName": "U16",
                                  "registerOffset": 0,
                                  "length": 1
                                }
                              },
                              {
                                "name": "deviceName",
                                "qualifiedType": "kotlin.String",
                                "valueKind": "STRING",
                                "field": {
                                  "codecName": "STRING_UTF8",
                                  "registerOffset": 4,
                                  "length": 16
                                }
                              }
                            ]
                          }
                        }
                      ]
                    }
                    """.trimIndent(),
                )
                statement.executeUpdate()
            }

            val services =
                provider.collectFromDatabase(
                    options =
                        ModbusDatabaseMetadataOptions(
                            driverClass = "org.sqlite.JDBC",
                            jdbcUrl = jdbcUrl,
                            username = null,
                            password = null,
                            query = "select payload from metadata_store",
                            jsonColumn = null,
                        ),
                    transport = ModbusTransportKind.RTU,
                    contractPackages = listOf("site.addzero.device.contract"),
                    logger = testLogger(),
                )

            assertEquals(1, services.size)
            val service = services.single().model
            assertEquals("site.addzero.device.contract.DeviceApi", service.interfaceQualifiedName)
            assertEquals("device", service.serviceId)
            val operation = service.operations.single()
            assertEquals("READ_INPUT_REGISTERS", operation.functionCodeName)
            assertEquals("get-device-runtime-info", operation.operationId)
            assertEquals(20, operation.quantity)
            assertTrue(operation.requestQualifiedName.endsWith(".generated.DeviceApiRtuGetDeviceRuntimeInfoRequest"))
            assertEquals(16, operation.returnType.properties.last().field?.registerWidth)
            assertTrue(services.single().providesSourceContract.not())
        }
    }

    @Test
    fun collectFromDatabaseSupportsBytesValueKind() {
        val jdbcUrl = "jdbc:sqlite:file:modbus-ksp-core-bytes-test?mode=memory&cache=shared"
        DriverManager.getConnection(jdbcUrl).use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("create table metadata_store (payload text not null)")
            }
            connection.prepareStatement("insert into metadata_store(payload) values (?)").use { statement ->
                statement.setString(
                    1,
                    """
                    {
                      "services": [
                        {
                          "interfacePackage": "site.addzero.device.contract",
                          "interfaceSimpleName": "DeviceApi",
                          "transport": "rtu",
                          "operations": [
                            {
                              "methodName": "getFlashConfig",
                              "functionCodeName": "READ_HOLDING_REGISTERS",
                              "address": 200,
                              "returnType": {
                                "qualifiedName": "site.addzero.device.contract.FlashConfigRegisters",
                                "simpleName": "FlashConfigRegisters",
                                "kind": "DTO",
                                "properties": [
                                  {
                                    "name": "magicWord",
                                    "qualifiedType": "kotlin.Int",
                                    "valueKind": "INT",
                                    "field": {
                                      "codecName": "U32_BE",
                                      "registerOffset": 0,
                                      "length": 1,
                                      "registerWidth": 2
                                    }
                                  },
                                  {
                                    "name": "portConfig",
                                    "qualifiedType": "kotlin.ByteArray",
                                    "valueKind": "BYTES",
                                    "field": {
                                      "codecName": "BYTE_ARRAY",
                                      "registerOffset": 2,
                                      "length": 24,
                                      "registerWidth": 12
                                    }
                                  }
                                ]
                              }
                            }
                          ]
                        }
                      ]
                    }
                    """.trimIndent(),
                )
                statement.executeUpdate()
            }

            val services =
                provider.collectFromDatabase(
                    options =
                        ModbusDatabaseMetadataOptions(
                            driverClass = "org.sqlite.JDBC",
                            jdbcUrl = jdbcUrl,
                            username = null,
                            password = null,
                            query = "select payload from metadata_store",
                            jsonColumn = null,
                        ),
                    transport = ModbusTransportKind.RTU,
                    contractPackages = listOf("site.addzero.device.contract"),
                    logger = testLogger(),
                )

            assertEquals(1, services.size)
            val bytesProperty = services.single().model.operations.single().returnType.properties.last()
            assertEquals(ModbusValueKind.BYTES, bytesProperty.valueKind)
            assertEquals("BYTE_ARRAY", bytesProperty.field?.codecName)
            assertEquals(12, bytesProperty.field?.registerWidth)
        }
    }
}

private fun testLogger(): KSPLogger =
    object : KSPLogger {
        override fun logging(
            message: String,
            symbol: com.google.devtools.ksp.symbol.KSNode?,
        ) = Unit

        override fun info(
            message: String,
            symbol: com.google.devtools.ksp.symbol.KSNode?,
        ) = Unit

        override fun warn(
            message: String,
            symbol: com.google.devtools.ksp.symbol.KSNode?,
        ) = Unit

        override fun error(
            message: String,
            symbol: com.google.devtools.ksp.symbol.KSNode?,
        ) = Unit

        override fun exception(
            e: Throwable,
        ) = Unit
    }
