// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CreateTranscriptionRequest(
    /**
     * The audio file object (not file name) to transcribe, in one of these formats: flac, mp3, mp4, mpeg,
     * mpga, m4a, ogg, wav, or webm.
     */
    val file: ByteArray,
    /**
     * ID of the model to use. The options are `gpt-4o-transcribe`, `gpt-4o-mini-transcribe`, `gpt-4o-mini-
     * transcribe-2025-12-15`, `whisper-1` (which is powered by our open source Whisper V2 model), and
     * `gpt-4o-transcribe-diarize`.
     */
    val model: String,
    /**
     * The language of the input audio. Supplying the input language in
     * [ISO-639-1](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes) (e.g. `en`) format will improve
     * accuracy and latency.
     */
    val language: String? = null,
    /**
     * An optional text to guide the model's style or continue a previous audio segment. The
     * [prompt](/docs/guides/speech-to-text#prompting) should match the audio language. This field is not
     * supported when using `gpt-4o-transcribe-diarize`.
     */
    val prompt: String? = null,
    @SerialName("response_format")
    val responseFormat: site.addzero.api.openai.models.AudioResponseFormat? = null,
    /**
     * The sampling temperature, between 0 and 1. Higher values like 0.8 will make the output more random,
     * while lower values like 0.2 will make it more focused and deterministic. If set to 0, the model will
     * use [log probability](https://en.wikipedia.org/wiki/Log_probability) to automatically increase the
     * temperature until certain thresholds are hit.
     */
    val temperature: Double? = 0.0,
    /**
     * Additional information to include in the transcription response. `logprobs` will return the log
     * probabilities of the tokens in the response to understand the model's confidence in the
     * transcription. `logprobs` only works with response_format set to `json` and only with the models
     * `gpt-4o-transcribe`, `gpt-4o-mini-transcribe`, and `gpt-4o-mini-transcribe-2025-12-15`. This field
     * is not supported when using `gpt-4o-transcribe-diarize`.
     */
    val include: List<site.addzero.api.openai.models.TranscriptionInclude>? = null,
    /**
     * The timestamp granularities to populate for this transcription. `response_format` must be set
     * `verbose_json` to use timestamp granularities. Either or both of these options are supported:
     * `word`, or `segment`. Note: There is no additional latency for segment timestamps, but generating
     * word timestamps incurs additional latency. This option is not available for `gpt-4o-transcribe-
     * diarize`.
     */
    @SerialName("timestamp_granularities")
    val timestampGranularities: List<String>? = null,
    val stream: Boolean? = null,
    @SerialName("chunking_strategy")
    val chunkingStrategy: JsonElement? = null,
    /**
     * Optional list of speaker names that correspond to the audio samples provided in
     * `known_speaker_references[]`. Each entry should be a short identifier (for example `customer` or
     * `agent`). Up to 4 speakers are supported.
     */
    @SerialName("known_speaker_names")
    val knownSpeakerNames: List<String>? = null,
    /**
     * Optional list of audio samples (as [data URLs](https://developer.mozilla.org/en-
     * US/docs/Web/HTTP/Basics_of_HTTP/Data_URLs)) that contain known speaker references matching
     * `known_speaker_names[]`. Each sample must be between 2 and 10 seconds, and can use any of the same
     * input audio formats supported by `file`.
     */
    @SerialName("known_speaker_references")
    val knownSpeakerReferences: List<String>? = null
)
