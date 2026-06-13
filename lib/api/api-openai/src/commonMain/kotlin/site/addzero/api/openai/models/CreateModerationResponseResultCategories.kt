// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A list of the categories, and whether they are flagged or not.
 */
@Serializable
data class CreateModerationResponseResultCategories(
    /**
     * Content that expresses, incites, or promotes hate based on race, gender, ethnicity, religion,
     * nationality, sexual orientation, disability status, or caste. Hateful content aimed at non-protected
     * groups (e.g., chess players) is harassment.
     */
    val hate: Boolean,
    /**
     * Hateful content that also includes violence or serious harm towards the targeted group based on
     * race, gender, ethnicity, religion, nationality, sexual orientation, disability status, or caste.
     */
    @SerialName("hate/threatening")
    val hateThreatening: Boolean,
    /**
     * Content that expresses, incites, or promotes harassing language towards any target.
     */
    val harassment: Boolean,
    /**
     * Harassment content that also includes violence or serious harm towards any target.
     */
    @SerialName("harassment/threatening")
    val harassmentThreatening: Boolean,
    val illicit: Boolean?,
    @SerialName("illicit/violent")
    val illicitViolent: Boolean?,
    /**
     * Content that promotes, encourages, or depicts acts of self-harm, such as suicide, cutting, and
     * eating disorders.
     */
    @SerialName("self-harm")
    val selfHarm: Boolean,
    /**
     * Content where the speaker expresses that they are engaging or intend to engage in acts of self-harm,
     * such as suicide, cutting, and eating disorders.
     */
    @SerialName("self-harm/intent")
    val selfHarmIntent: Boolean,
    /**
     * Content that encourages performing acts of self-harm, such as suicide, cutting, and eating
     * disorders, or that gives instructions or advice on how to commit such acts.
     */
    @SerialName("self-harm/instructions")
    val selfHarmInstructions: Boolean,
    /**
     * Content meant to arouse sexual excitement, such as the description of sexual activity, or that
     * promotes sexual services (excluding sex education and wellness).
     */
    val sexual: Boolean,
    /**
     * Sexual content that includes an individual who is under 18 years old.
     */
    @SerialName("sexual/minors")
    val sexualMinors: Boolean,
    /**
     * Content that depicts death, violence, or physical injury.
     */
    val violence: Boolean,
    /**
     * Content that depicts death, violence, or physical injury in graphic detail.
     */
    @SerialName("violence/graphic")
    val violenceGraphic: Boolean
)
