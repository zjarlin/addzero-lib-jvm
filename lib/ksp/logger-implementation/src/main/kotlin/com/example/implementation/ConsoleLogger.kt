package com.example.implementation

import com.example.api.Logger

class ConsoleLogger : Logger {
    override val name: String = "ConsoleLogger"

    override fun log(message: String) {
        // At runtime, this would print to the console.
        println("[$name] $message")
    }
}
