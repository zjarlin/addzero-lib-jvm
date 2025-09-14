@file:OptIn(ExperimentalTime::class)

package site.addzero.generated.isomorphic

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime


@Serializable
data class BizMappingIso(
    val id: Long? = null,
    val fromId: Long = 0,
    val toId: Long = 0,
    val mappingType: String = ""
)
