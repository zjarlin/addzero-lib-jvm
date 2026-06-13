// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateModerationResponseResult(
    /**
     * Whether any of the below categories are flagged.
     */
    val flagged: Boolean,
    /**
     * A list of the categories, and whether they are flagged or not.
     */
    val categories: site.addzero.api.openai.models.CreateModerationResponseResultCategories,
    /**
     * A list of the categories along with their scores as predicted by model.
     */
    @SerialName("category_scores")
    val categoryScores: site.addzero.api.openai.models.CreateModerationResponseResultCategoryScores,
    /**
     * A list of the categories along with the input type(s) that the score applies to.
     */
    @SerialName("category_applied_input_types")
    val categoryAppliedInputTypes: site.addzero.api.openai.models.CreateModerationResponseResultCategoryAppliedInputTypes
)
