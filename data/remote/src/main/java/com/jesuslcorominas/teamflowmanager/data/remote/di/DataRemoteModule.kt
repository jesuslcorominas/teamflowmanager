package com.jesuslcorominas.teamflowmanager.data.remote.di

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

/**
 * Sample DI module demonstrating KtorFit configuration.
 * This module shows how to set up Ktor HttpClient and Ktorfit for dependency injection.
 *
 * To use this module:
 * 1. Include it in your main Koin module configuration
 * 2. Customize the HttpClient configuration as needed
 * 3. Add your API interfaces using Ktorfit.create<YourApi>()
 *
 * Example:
 * ```
 * includes(dataRemoteModule)
 * ```
 */
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

        // Configure Ktor HttpClient
        single {
            HttpClient(OkHttp) {
                // Content negotiation for JSON serialization
                install(ContentNegotiation) {
                    json(get())
                }

                // Logging for debugging
                install(Logging) {
                    logger =
                        object : Logger {
                            override fun log(message: String) {
                                println("Ktor HTTP Client: $message")
                            }
                        }
                    level = LogLevel.BODY // Log request/response bodies
                }
            }
        }

        // Configure Ktorfit
        single {
            Ktorfit
                .Builder()
                .httpClient(get()) // Use the configured HttpClient
                .baseUrl("https://api.example.com/") // Replace with your actual base URL
                .build()
        }

        // Add your API interfaces here
        // Example:
        // single<SampleApi> { get<Ktorfit>().create() }
    }
