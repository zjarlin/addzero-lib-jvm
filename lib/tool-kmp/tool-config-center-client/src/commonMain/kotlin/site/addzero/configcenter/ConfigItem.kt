package site.addzero.configcenter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConfigItem(
  val namespace: String,
  @SerialName("config_key")
  val key: String,
  @SerialName("config_value")
  val configValue: String,
  @SerialName("value_type")
  val valueType: String,
  val enabled: Boolean,
  val version: Int,
)
