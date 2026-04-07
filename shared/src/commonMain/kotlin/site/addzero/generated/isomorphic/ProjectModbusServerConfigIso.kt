package site.addzero.generated.isomorphic

import kotlinx.serialization.Serializable
import site.addzero.kcloud.plugins.hostconfig.model.enums.Parity
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType

@Serializable
data class ProjectModbusServerConfigIso(
    val id: Long = 0L,
    val transportType: TransportType = TransportType.entries.first(),
    val enabled: Boolean = false,
    val tcpPort: Int? = null,
    val portName: String? = null,
    val baudRate: Int? = null,
    val dataBits: Int? = null,
    val stopBits: Int? = null,
    val parity: Parity? = null,
    val stationNo: Int? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val project: ProjectIso = ProjectIso()
)