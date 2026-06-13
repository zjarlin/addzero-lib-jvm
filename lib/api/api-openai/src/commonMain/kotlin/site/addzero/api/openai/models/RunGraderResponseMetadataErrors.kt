// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RunGraderResponseMetadataErrors(
    @SerialName("formula_parse_error")
    val formulaParseError: Boolean,
    @SerialName("sample_parse_error")
    val sampleParseError: Boolean,
    @SerialName("truncated_observation_error")
    val truncatedObservationError: Boolean,
    @SerialName("unresponsive_reward_error")
    val unresponsiveRewardError: Boolean,
    @SerialName("invalid_variable_error")
    val invalidVariableError: Boolean,
    @SerialName("other_error")
    val otherError: Boolean,
    @SerialName("python_grader_server_error")
    val pythonGraderServerError: Boolean,
    @SerialName("python_grader_server_error_type")
    val pythonGraderServerErrorType: String?,
    @SerialName("python_grader_runtime_error")
    val pythonGraderRuntimeError: Boolean,
    @SerialName("python_grader_runtime_error_details")
    val pythonGraderRuntimeErrorDetails: String?,
    @SerialName("model_grader_server_error")
    val modelGraderServerError: Boolean,
    @SerialName("model_grader_refusal_error")
    val modelGraderRefusalError: Boolean,
    @SerialName("model_grader_parse_error")
    val modelGraderParseError: Boolean,
    @SerialName("model_grader_server_error_details")
    val modelGraderServerErrorDetails: String?
)
