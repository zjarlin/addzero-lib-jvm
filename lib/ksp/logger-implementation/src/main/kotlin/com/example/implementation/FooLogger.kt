package com.example.implementation

import com.example.api.Logger
import com.google.auto.service.AutoService

@AutoService(Logger::class)
class FooLogger : Logger {
    override val name: String = "FooLogger"

    override fun log(message: String) {
      println("foo")
        // At runtime, this would print to the console.
        println("[$name] $message")
    }
}
