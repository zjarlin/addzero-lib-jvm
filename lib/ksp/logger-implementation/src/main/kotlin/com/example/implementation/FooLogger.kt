package com.example.implementation

import com.example.api.Logger
import com.fueledbycaffeine.autoservice.AutoService

@AutoService
class FooLogger : Logger {
    override val name: String = "FooLogger"

    override fun log(message: String) {
      println("foo")
        // At runtime, this would print to the console.
        println("[$name] $message")
    }
}
