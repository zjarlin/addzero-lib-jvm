package com.example.processor

import com.example.api.Logger
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import java.util.*

class LoggerProcessor(
    private val logger: KSPLogger
) : SymbolProcessor {

    private var processed = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // Process only once to avoid repeated logs
        if (processed) {
            return emptyList()
        }

        logger.warn("--- KSP LoggerProcessor: Starting to look for Logger implementations ---")

        // Use ServiceLoader to load all implementations of the Logger interface
        val loader = ServiceLoader.load(Logger::class.java, Logger::class.java.classLoader)

        val implementations = loader.iterator().asSequence().toList()

        if (implementations.isEmpty()) {
            logger.warn("No Logger implementations found. Check SPI configuration and dependencies.")
        } else {
            implementations.forEach { loggerImpl ->
                // Log the found implementation details using KSP's logger
                logger.warn("Found Logger implementation: ${loggerImpl::class.java.canonicalName}, Name: '${loggerImpl.name}'")
            }
        }
        
        logger.warn("--- KSP LoggerProcessor: Search finished ---")

        processed = true
        return emptyList()
    }
}

class LoggerProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return LoggerProcessor(environment.logger)
    }
}
