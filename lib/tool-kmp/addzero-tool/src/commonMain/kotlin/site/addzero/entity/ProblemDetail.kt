package site.addzero.entity

import kotlinx.serialization.Serializable

@Serializable
data class ProblemDetail(
    val detail: String,
    val errorCode: String,
    val instance: String,
    val message: String,
    val status: Int,
    val title: String,
    val type: String
)
