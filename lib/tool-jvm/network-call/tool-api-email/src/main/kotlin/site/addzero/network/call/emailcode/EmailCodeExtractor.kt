package site.addzero.network.call.emailcode

import site.addzero.network.call.emailcode.model.EmailCodeRequest
import site.addzero.network.call.emailcode.model.EmailMessageDetail
import site.addzero.network.call.emailcode.model.EmailMessageSummary
import java.util.Locale

object EmailCodeExtractor {
    fun extract(
        summary: EmailMessageSummary,
        detail: EmailMessageDetail?,
        request: EmailCodeRequest,
    ): String? {
        if (!matchesSummary(summary, request)) {
            return null
        }

        val combinedBody = buildList {
            detail?.text?.takeIf { it.isNotBlank() }?.let(::add)
            detail?.html?.takeIf { it.isNotBlank() }?.let(::add)
            summary.previewText.takeIf { it.isNotBlank() }?.let(::add)
        }.joinToString("\n")

        if (!matchesBody(combinedBody, request)) {
            return null
        }

        val candidates = buildList {
            detail?.subject?.takeIf { it.isNotBlank() }?.let(::add)
            summary.subject.takeIf { it.isNotBlank() }?.let(::add)
            detail?.text?.takeIf { it.isNotBlank() }?.let(::add)
            detail?.html?.takeIf { it.isNotBlank() }?.let(::add)
            summary.previewText.takeIf { it.isNotBlank() }?.let(::add)
        }

        return candidates.firstNotNullOfOrNull { findCode(it, request) }
    }

    fun matchesSummary(summary: EmailMessageSummary, request: EmailCodeRequest): Boolean {
        val sender = listOf(summary.fromAddress, summary.fromName)
        return containsAll(sender, request.senderIncludes) &&
            containsAll(listOf(summary.subject), request.subjectIncludes)
    }

    private fun matchesBody(body: String, request: EmailCodeRequest): Boolean {
        if (request.bodyIncludes.isEmpty()) {
            return true
        }
        return containsAll(listOf(body), request.bodyIncludes)
    }

    private fun containsAll(candidates: List<String>, filters: List<String>): Boolean {
        if (filters.isEmpty()) {
            return true
        }
        val haystack = candidates.joinToString("\n").lowercase(Locale.ROOT)
        return filters.all { haystack.contains(it.lowercase(Locale.ROOT)) }
    }

    private fun findCode(text: String, request: EmailCodeRequest): String? {
        request.codePattern?.find(text)?.groupValues?.firstOrNull { it.isNotBlank() }?.let { return it }

        val strictMatch = Regex("\\b(\\d{${request.codeLength}})\\b")
            .find(text)
            ?.groupValues
            ?.getOrNull(1)
        if (!strictMatch.isNullOrBlank()) {
            return strictMatch
        }

        val spacedMatch = Regex("(?:\\d\\s*){${request.codeLength}}")
            .find(text)
            ?.value
            ?.filter(Char::isDigit)
            ?.takeIf { it.length == request.codeLength }
        if (!spacedMatch.isNullOrBlank()) {
            return spacedMatch
        }

        return null
    }
}
