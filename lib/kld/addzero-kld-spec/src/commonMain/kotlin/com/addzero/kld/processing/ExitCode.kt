package com.addzero.kld.processing

enum class ExitCode(
    @Suppress("UNUSED_PARAMETER")
    code: Int
) {
    OK(0),

    // Whenever there are some error messages.
    PROCESSING_ERROR(1),

    // Let exceptions pop through to the caller. Don't catch and convert them to, e.g., INTERNAL_ERROR.
}
