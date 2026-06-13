// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EvalRunPerTestingCriteriaResult(
    /**
     * A description of the testing criteria.
     */
    @SerialName("testing_criteria")
    val testingCriteria: String,
    /**
     * Number of tests passed for this criteria.
     */
    val passed: Int,
    /**
     * Number of tests failed for this criteria.
     */
    val failed: Int
)
