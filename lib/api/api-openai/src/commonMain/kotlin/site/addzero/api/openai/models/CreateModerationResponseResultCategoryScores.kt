// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A list of the categories along with their scores as predicted by model.
 */
@Serializable
data class CreateModerationResponseResultCategoryScores(
    /**
     * The score for the category 'hate'.
     */
    val hate: Double,
    /**
     * The score for the category 'hate/threatening'.
     */
    @SerialName("hate/threatening")
    val hateThreatening: Double,
    /**
     * The score for the category 'harassment'.
     */
    val harassment: Double,
    /**
     * The score for the category 'harassment/threatening'.
     */
    @SerialName("harassment/threatening")
    val harassmentThreatening: Double,
    /**
     * The score for the category 'illicit'.
     */
    val illicit: Double,
    /**
     * The score for the category 'illicit/violent'.
     */
    @SerialName("illicit/violent")
    val illicitViolent: Double,
    /**
     * The score for the category 'self-harm'.
     */
    @SerialName("self-harm")
    val selfHarm: Double,
    /**
     * The score for the category 'self-harm/intent'.
     */
    @SerialName("self-harm/intent")
    val selfHarmIntent: Double,
    /**
     * The score for the category 'self-harm/instructions'.
     */
    @SerialName("self-harm/instructions")
    val selfHarmInstructions: Double,
    /**
     * The score for the category 'sexual'.
     */
    val sexual: Double,
    /**
     * The score for the category 'sexual/minors'.
     */
    @SerialName("sexual/minors")
    val sexualMinors: Double,
    /**
     * The score for the category 'violence'.
     */
    val violence: Double,
    /**
     * The score for the category 'violence/graphic'.
     */
    @SerialName("violence/graphic")
    val violenceGraphic: Double
)
