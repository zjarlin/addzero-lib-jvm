// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * An Eval object with a data source config and testing criteria. An Eval represents a task to be done
 * for your LLM integration. Like: - Improve the quality of my chatbot - See how well my chatbot
 * handles customer support - Check if o4-mini is better at my usecase than gpt-4o
 */
@Serializable
data class Eval(
    /**
     * The object type.
     */
    @SerialName("object")
    val objectType: String = "eval",
    /**
     * Unique identifier for the evaluation.
     */
    val id: String,
    /**
     * The name of the evaluation.
     */
    val name: String,
    /**
     * Configuration of data sources used in runs of the evaluation.
     */
    @SerialName("data_source_config")
    val dataSourceConfig: JsonElement,
    /**
     * A list of testing criteria.
     */
    @SerialName("testing_criteria")
    val testingCriteria: List<JsonElement>,
    /**
     * The Unix timestamp (in seconds) for when the eval was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    val metadata: site.addzero.api.openai.models.Metadata?
)
