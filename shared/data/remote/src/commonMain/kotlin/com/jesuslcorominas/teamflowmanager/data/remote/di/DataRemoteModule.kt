package com.jesuslcorominas.teamflowmanager.data.remote.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

/**
 * Sample DI module demonstrating Ktor configuration for Kotlin Multiplatform.
 * This module shows how to set up Ktor HttpClient for dependency injection.
 *
 * The HttpClient engine is provided by platform-specific implementations.
 *
 * To use this module:
 * 1. Include it in your main Koin module configuration
 * 2. Customize the HttpClient configuration as needed
 * 3. Add your API implementations
 *
 * Example:
 * ```
 * includes(dataRemoteModule)
 * ```
 */

/**
 * Expected function to create the platform-specific HttpClient.
 * Each platform (Android, iOS) provides its own implementation.
 */
expect fun createHttpClient(json: Json): HttpClient

val dataRemoteModule =
    module {
        // Configure JSON serialization
        single {
            Json {
                ignoreUnknownKeys = true // Ignore unknown properties in JSON responses
                isLenient = true // Be lenient with malformed JSON
                prettyPrint = true // Pretty print JSON for debugging
            }
        }

        // Configure Ktor HttpClient (platform-specific engine)
        single {
            createHttpClient(get())
        }

        // Add your API implementations here
        // Example:
        // single<SampleApi> { SampleApiImpl(get()) }
    }
