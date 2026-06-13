// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The format of the output, in one of these options: `json`, `text`, `srt`, `verbose_json`, `vtt`, or
 * `diarized_json`. For `gpt-4o-transcribe` and `gpt-4o-mini-transcribe`, the only supported format is
 * `json`. For `gpt-4o-transcribe-diarize`, the supported formats are `json`, `text`, and
 * `diarized_json`, with `diarized_json` required to receive speaker annotations.
 */
typealias AudioResponseFormat = String
