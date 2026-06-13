// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A list of the categories along with the input type(s) that the score applies to.
 */
@Serializable
data class CreateModerationResponseResultCategoryAppliedInputTypes(
    /**
     * The applied input type(s) for the category 'hate'.
     */
    val hate: List<String>,
    /**
     * The applied input type(s) for the category 'hate/threatening'.
     */
    @SerialName("hate/threatening")
    val hateThreatening: List<String>,
    /**
     * The applied input type(s) for the category 'harassment'.
     */
    val harassment: List<String>,
    /**
     * The applied input type(s) for the category 'harassment/threatening'.
     */
    @SerialName("harassment/threatening")
    val harassmentThreatening: List<String>,
    /**
     * The applied input type(s) for the category 'illicit'.
     */
    val illicit: List<String>,
    /**
     * The applied input type(s) for the category 'illicit/violent'.
     */
    @SerialName("illicit/violent")
    val illicitViolent: List<String>,
    /**
     * The applied input type(s) for the category 'self-harm'.
     */
    @SerialName("self-harm")
    val selfHarm: List<String>,
    /**
     * The applied input type(s) for the category 'self-harm/intent'.
     */
    @SerialName("self-harm/intent")
    val selfHarmIntent: List<String>,
    /**
     * The applied input type(s) for the category 'self-harm/instructions'.
     */
    @SerialName("self-harm/instructions")
    val selfHarmInstructions: List<String>,
    /**
     * The applied input type(s) for the category 'sexual'.
     */
    val sexual: List<String>,
    /**
     * The applied input type(s) for the category 'sexual/minors'.
     */
    @SerialName("sexual/minors")
    val sexualMinors: List<String>,
    /**
     * The applied input type(s) for the category 'violence'.
     */
    val violence: List<String>,
    /**
     * The applied input type(s) for the category 'violence/graphic'.
     */
    @SerialName("violence/graphic")
    val violenceGraphic: List<String>
)
