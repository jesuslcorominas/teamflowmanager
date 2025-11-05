package com.jesuslcorominas.teamflowmanager.data.remote.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path

/**
 * Sample API interface demonstrating KtorFit usage.
 * This is a placeholder to show how to define API endpoints with KtorFit.
 *
 * To use this interface:
 * 1. Create your API endpoints using KtorFit annotations (@GET, @POST, etc.)
 * 2. Define your data models with kotlinx.serialization.Serializable annotation
 * 3. Create a Ktorfit instance in your DI module
 * 4. Generate the implementation using Ktorfit.create<YourApi>()
 *
 * Example:
 * ```
 * val ktorfit = Ktorfit.Builder()
 *     .baseUrl("https://api.example.com/")
 *     .build()
 * val api = ktorfit.create<SampleApi>()
 * ```
 */
interface SampleApi {
    /**
     * Example GET endpoint
     * @param id The resource ID
     * @return The requested resource
     */
    @GET("resource/{id}")
    suspend fun getResource(@Path("id") id: String): String // Replace String with your actual response type
}
