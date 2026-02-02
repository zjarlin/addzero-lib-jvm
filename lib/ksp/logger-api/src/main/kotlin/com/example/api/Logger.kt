package com.example.api

/**
 * The logger interface we want to abstract.
 */
interface Logger {
    /**
     * The name of the implementation.
     */
    val name: String

    /**
     * Logs a message.
     */
    fun log(message: String)
}
