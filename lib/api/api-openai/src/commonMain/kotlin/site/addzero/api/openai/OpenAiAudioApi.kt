// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.CreateSpeechRequest
import site.addzero.api.openai.models.CreateTranscriptionRequest
import site.addzero.api.openai.models.CreateTranscriptionResponse
import site.addzero.api.openai.models.CreateTranslationRequest
import site.addzero.api.openai.models.CreateTranslationResponse
import site.addzero.api.openai.models.CreateVoiceConsentRequest
import site.addzero.api.openai.models.CreateVoiceRequest
import site.addzero.api.openai.models.UpdateVoiceConsentRequest
import site.addzero.api.openai.models.VoiceConsentDeletedResource
import site.addzero.api.openai.models.VoiceConsentListResource
import site.addzero.api.openai.models.VoiceConsentResource
import site.addzero.api.openai.models.VoiceResource

interface OpenAiAudioApi {

    /**
     * Generates audio from the input text. Returns the audio file content, or a stream of audio events.
     * REST: POST /audio/speech
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.AUDIO_BY_SPEECH)
    suspend fun createSpeech(
        @Body body: site.addzero.api.openai.models.CreateSpeechRequest
    ): site.addzero.api.openai.OpenAiBinaryBody

    /**
     * Transcribes audio into the input language. Returns a transcription object in `json`,
     * `diarized_json`, or `verbose_json` format, or a stream of transcript events. REST: POST
     * /audio/transcriptions
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.AUDIO_BY_TRANSCRIPTIONS)
    suspend fun createTranscription(
        @Body body: site.addzero.api.openai.models.CreateTranscriptionRequest
    ): site.addzero.api.openai.models.CreateTranscriptionResponse

    /**
     * Translates audio into English. REST: POST /audio/translations
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.AUDIO_BY_TRANSLATIONS)
    suspend fun createTranslation(
        @Body body: site.addzero.api.openai.models.CreateTranslationRequest
    ): site.addzero.api.openai.models.CreateTranslationResponse

    /**
     * Returns a list of voice consent recordings. REST: GET /audio/voice_consents
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.AUDIO_BY_VOICE_CONSENTS)
    suspend fun listVoiceConsents(
        @Query("after") after: String? = null,
        @Query("limit") limit: Int? = null
    ): site.addzero.api.openai.models.VoiceConsentListResource

    /**
     * Upload a voice consent recording. REST: POST /audio/voice_consents
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.AUDIO_BY_VOICE_CONSENTS)
    suspend fun createVoiceConsent(
        @Body body: site.addzero.api.openai.models.CreateVoiceConsentRequest
    ): site.addzero.api.openai.models.VoiceConsentResource

    /**
     * Retrieves a voice consent recording. REST: GET /audio/voice_consents/{consent_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.AUDIO_BY_VOICE_CONSENTS_BY_CONSENT_ID)
    suspend fun getVoiceConsent(
        @Path("consent_id") consentId: String
    ): site.addzero.api.openai.models.VoiceConsentResource

    /**
     * Updates a voice consent recording (metadata only). REST: POST /audio/voice_consents/{consent_id}
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.AUDIO_BY_VOICE_CONSENTS_BY_CONSENT_ID)
    suspend fun updateVoiceConsent(
        @Path("consent_id") consentId: String,
        @Body body: site.addzero.api.openai.models.UpdateVoiceConsentRequest
    ): site.addzero.api.openai.models.VoiceConsentResource

    /**
     * Deletes a voice consent recording. REST: DELETE /audio/voice_consents/{consent_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.AUDIO_BY_VOICE_CONSENTS_BY_CONSENT_ID)
    suspend fun deleteVoiceConsent(
        @Path("consent_id") consentId: String
    ): site.addzero.api.openai.models.VoiceConsentDeletedResource

    /**
     * Creates a custom voice. REST: POST /audio/voices
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.AUDIO_BY_VOICES)
    suspend fun createVoice(
        @Body body: site.addzero.api.openai.models.CreateVoiceRequest
    ): site.addzero.api.openai.models.VoiceResource

}
