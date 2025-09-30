package site.addzero.entity

import kotlinx.serialization.Serializable

/**
 * ProblemDetail is a standardized response format that follows the RFC 7807 specification.
 * This class is recommended as a replacement for the deprecated [Res] class.
 */

data class ProblemDetail(
    val detail: String,
    val errorCode: String,
    val instance: String,
    val message: String,
    val status: Int,
    val title: String,
    val type: String
)
