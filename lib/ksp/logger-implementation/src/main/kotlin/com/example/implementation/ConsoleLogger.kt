package com.example.implementation

import com.example.api.Logger
import com.google.auto.service.AutoService

@AutoService(Logger::class)
class ConsoleLogger : Logger {
    override val name: String = "ConsoleLogger"

    override fun log(message: String) {
      println("consul")
        // At runtime, this would print to the console.
        println("[$name] $message")
    }
}
