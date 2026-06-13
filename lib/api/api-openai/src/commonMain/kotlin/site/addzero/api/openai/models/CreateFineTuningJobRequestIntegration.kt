// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateFineTuningJobRequestIntegration(
    /**
     * The type of integration to enable. Currently, only "wandb" (Weights and Biases) is supported.
     */
    val type: String,
    /**
     * The settings for your integration with Weights and Biases. This payload specifies the project that
     * metrics will be sent to. Optionally, you can set an explicit display name for your run, add tags to
     * your run, and set a default entity (team, username, etc) to be associated with your run.
     */
    val wandb: site.addzero.api.openai.models.CreateFineTuningJobRequestIntegrationWandb
)
