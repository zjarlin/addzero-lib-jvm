package site.addzero.generated.isomorphic

import java.math.BigDecimal
import kotlinx.serialization.Serializable

@Serializable
data class ProjectMqttConfigIso(
    val id: Long = 0L,
    val enabled: Boolean = false,
    val breakpointResume: Boolean = false,
    val gatewayName: String? = null,
    val vendor: String? = null,
    val host: String? = null,
    val port: Int? = null,
    val topic: String? = null,
    val gatewayId: String? = null,
    val authEnabled: Boolean = false,
    val username: String? = null,
    val passwordEncrypted: String? = null,
    val tlsEnabled: Boolean = false,
    val certFileRef: String? = null,
    val clientId: String? = null,
    val keepAliveSec: Int? = null,
    val qos: Int? = null,
    val reportPeriodSec: Int? = null,
    val precision: BigDecimal? = null,
    val valueChangeRatioEnabled: Boolean = false,
    val cloudControlDisabled: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val project: ProjectIso = ProjectIso()
)