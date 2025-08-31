@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.addzero.generated.isomorphic

import kotlinx.serialization.Serializable


@Serializable
data class JdbcColumnMetadataAttachIso(
    val id: Long? = null,
    val showInListFlag: Boolean = false,
    val showInFormFlag: Boolean = false,
    val showInSearchFlag: Boolean = false,
    val jdbcColumnMetadata: JdbcColumnMetadataIso? = null
)