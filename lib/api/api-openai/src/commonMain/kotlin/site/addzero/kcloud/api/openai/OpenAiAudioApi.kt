// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Audio REST endpoints. */
interface OpenAiAudioApi {

    /**
     * Generates audio from the input text. Returns the audio file content, or a stream of audio events.
     *
     * REST: POST /audio/speech
     */
    @POST(OpenAiApiPaths.AUDIO_BY_SPEECH)
    suspend fun createSpeech(
        @Body body: OpenAiRequestBody
    ): OpenAiBinaryBody

    /**
     * Transcribes audio into the input language. Returns a transcription object in `json`, `diarized_json`, or `verbose_json` format, or a stream of transcript events.
     *
     * REST: POST /audio/transcriptions
     */
    @POST(OpenAiApiPaths.AUDIO_BY_TRANSCRIPTIONS)
    suspend fun createTranscription(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Translates audio into English.
     *
     * REST: POST /audio/translations
     */
    @POST(OpenAiApiPaths.AUDIO_BY_TRANSLATIONS)
    suspend fun createTranslation(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Returns a list of voice consent recordings.
     *
     * REST: GET /audio/voice_consents
     */
    @GET(OpenAiApiPaths.AUDIO_BY_VOICE_CONSENTS)
    suspend fun listVoiceConsents(
        @Query("after") after: String? = null,
        @Query("limit") limit: Int? = null
    ): OpenAiResponseBody

    /**
     * Upload a voice consent recording.
     *
     * REST: POST /audio/voice_consents
     */
    @POST(OpenAiApiPaths.AUDIO_BY_VOICE_CONSENTS)
    suspend fun createVoiceConsent(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Deletes a voice consent recording.
     *
     * REST: DELETE /audio/voice_consents/{consent_id}
     */
    @DELETE(OpenAiApiPaths.AUDIO_BY_VOICE_CONSENTS_BY_CONSENT_ID)
    suspend fun deleteVoiceConsent(
        @Path("consent_id") consentId: String
    ): OpenAiResponseBody

    /**
     * Retrieves a voice consent recording.
     *
     * REST: GET /audio/voice_consents/{consent_id}
     */
    @GET(OpenAiApiPaths.AUDIO_BY_VOICE_CONSENTS_BY_CONSENT_ID)
    suspend fun getVoiceConsent(
        @Path("consent_id") consentId: String
    ): OpenAiResponseBody

    /**
     * Updates a voice consent recording (metadata only).
     *
     * REST: POST /audio/voice_consents/{consent_id}
     */
    @POST(OpenAiApiPaths.AUDIO_BY_VOICE_CONSENTS_BY_CONSENT_ID)
    suspend fun updateVoiceConsent(
        @Path("consent_id") consentId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Creates a custom voice.
     *
     * REST: POST /audio/voices
     */
    @POST(OpenAiApiPaths.AUDIO_BY_VOICES)
    suspend fun createVoice(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody
}
