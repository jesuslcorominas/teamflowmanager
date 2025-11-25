package com.jesuslcorominas.teamflowmanager.data.remote.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Android implementation of HttpClient using OkHttp engine.
 */
actual fun createHttpClient(json: Json): HttpClient {
    return HttpClient(OkHttp) {
        // Content negotiation for JSON serialization
        install(ContentNegotiation) {
            json(json)
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
