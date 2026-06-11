package site.addzero.configcenter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import site.addzero.core.network.json.json

class ConfigCenterEncodingTest {
  @Serializable
  data class RedisConfig(
    val host: String,
    val port: Int,
  )

  @Test
  fun configItemUsesServerFieldNames() {
    val item = Json.decodeFromString<ConfigItem>(
      """
      {
        "namespace": "cmp-aio.dev",
        "config_key": "redis",
        "config_value": "{\"host\":\"127.0.0.1\",\"port\":6379}",
        "value_type": "json",
        "enabled": true,
        "version": 1
      }
      """.trimIndent(),
    )

    assertEquals("redis", item.key)
  }

  @Test
  fun upsertRequestUsesServerFieldNames() {
    val body = json.encodeToString(
      UpsertRequest.serializer(),
      UpsertRequest(
        namespace = "cmp-aio.dev",
        key = "redis",
        value = """{"host":"127.0.0.1","port":6379}""",
        valueType = "json",
        description = "Redis 配置",
        enabled = true,
        updatedBy = "zjarlin",
      ),
    )

    assertTrue(body.contains(""""value_type":"json""""))
    assertTrue(body.contains(""""updated_by":"zjarlin""""))
    assertFalse(body.contains("valueType"))
    assertFalse(body.contains("updatedBy"))
  }
}
