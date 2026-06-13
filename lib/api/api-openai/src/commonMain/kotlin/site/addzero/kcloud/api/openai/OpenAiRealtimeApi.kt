// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Realtime REST endpoints. */
interface OpenAiRealtimeApi {

    /**
     * Create a new Realtime API call over WebRTC and receive the SDP answer needed to complete the peer connection.
     *
     * REST: POST /realtime/calls
     */
    @POST(OpenAiApiPaths.REALTIME_BY_CALLS)
    suspend fun createRealtimeCall(
        @Body body: OpenAiRequestBody
    ): OpenAiTextBody

    /**
     * Accept an incoming SIP call and configure the realtime session that will handle it.
     *
     * REST: POST /realtime/calls/{call_id}/accept
     */
    @POST(OpenAiApiPaths.REALTIME_BY_CALLS_BY_CALL_ID_BY_ACCEPT)
    suspend fun acceptRealtimeCall(
        @Path("call_id") callId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * End an active Realtime API call, whether it was initiated over SIP or WebRTC.
     *
     * REST: POST /realtime/calls/{call_id}/hangup
     */
    @POST(OpenAiApiPaths.REALTIME_BY_CALLS_BY_CALL_ID_BY_HANGUP)
    suspend fun hangupRealtimeCall(
        @Path("call_id") callId: String
    ): OpenAiResponseBody

    /**
     * Transfer an active SIP call to a new destination using the SIP REFER verb.
     *
     * REST: POST /realtime/calls/{call_id}/refer
     */
    @POST(OpenAiApiPaths.REALTIME_BY_CALLS_BY_CALL_ID_BY_REFER)
    suspend fun referRealtimeCall(
        @Path("call_id") callId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Decline an incoming SIP call by returning a SIP status code to the caller.
     *
     * REST: POST /realtime/calls/{call_id}/reject
     */
    @POST(OpenAiApiPaths.REALTIME_BY_CALLS_BY_CALL_ID_BY_REJECT)
    suspend fun rejectRealtimeCall(
        @Path("call_id") callId: String,
        @Body body: OpenAiRequestBody? = null
    ): OpenAiResponseBody

    /**
     * Create a Realtime client secret with an associated session configuration. Client secrets are short-lived tokens that can be passed to a client app, such as a web frontend or mobile client, which grants access to the Realtime API without leaking your main API key. You can configure a custom TTL for each client secret. You can also attach session configuration options to the client secret, which will be applied to any sessions created using that client secret, but these can also be overridden by the client connection. [Learn more about authentication with client secrets over WebRTC](/docs/guides/realtime-webrtc). Returns the created client secret and the effective session object. The client secret is a string that looks like `ek_1234`.
     *
     * REST: POST /realtime/client_secrets
     */
    @POST(OpenAiApiPaths.REALTIME_BY_CLIENT_SECRETS)
    suspend fun createRealtimeClientSecret(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Create an ephemeral API token for use in client-side applications with the Realtime API. Can be configured with the same session parameters as the `session.update` client event. It responds with a session object, plus a `client_secret` key which contains a usable ephemeral API token that can be used to authenticate browser clients for the Realtime API. Returns the created Realtime session object, plus an ephemeral key.
     *
     * REST: POST /realtime/sessions
     */
    @POST(OpenAiApiPaths.REALTIME_BY_SESSIONS)
    suspend fun createRealtimeSession(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Create an ephemeral API token for use in client-side applications with the Realtime API specifically for realtime transcriptions. Can be configured with the same session parameters as the `transcription_session.update` client event. It responds with a session object, plus a `client_secret` key which contains a usable ephemeral API token that can be used to authenticate browser clients for the Realtime API. Returns the created Realtime transcription session object, plus an ephemeral key.
     *
     * REST: POST /realtime/transcription_sessions
     */
    @POST(OpenAiApiPaths.REALTIME_BY_TRANSCRIPTION_SESSIONS)
    suspend fun createRealtimeTranscriptionSession(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Create a Realtime translation client secret with an associated translation session configuration. Client secrets are short-lived tokens that can be passed to a client app, such as a web frontend or mobile client, which grants access to the Realtime Translation API without leaking your main API key. You can configure a custom TTL for each client secret. Returns the created client secret and the effective translation session object. The client secret is a string that looks like `ek_1234`.
     *
     * REST: POST /realtime/translations/client_secrets
     */
    @POST(OpenAiApiPaths.REALTIME_BY_TRANSLATIONS_BY_CLIENT_SECRETS)
    suspend fun createRealtimeTranslationClientSecret(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody
}
