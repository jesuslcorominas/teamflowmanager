package com.jesuslcorominas.teamflowmanager.data.remote.api

/**
 * Sample API interface demonstrating Ktor usage.
 * This is a placeholder to show how to define API endpoints with Ktor.
 *
 * To use this interface:
 * 1. Create your API endpoints using suspend functions
 * 2. Define your data models with kotlinx.serialization.Serializable annotation
 * 3. Create a Ktor HttpClient in your DI module
 * 4. Implement the API using the HttpClient
 *
 * Example:
 * ```
 * class SampleApiImpl(private val client: HttpClient) : SampleApi {
 *     override suspend fun getResource(id: String): String {
 *         return client.get("resource/$id").body()
 *     }
 * }
 * ```
 */
interface SampleApi {
    /**
     * Example GET endpoint
     * @param id The resource ID
     * @return The requested resource
     */
    suspend fun getResource(id: String): String // Replace String with your actual response type
}
