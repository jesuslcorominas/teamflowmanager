# Data Remote Module - KtorFit Setup

This module is configured to use **KtorFit** for remote data access, which is KMM-ready and provides a modern, coroutine-based approach to API calls.

## Architecture

KtorFit is built on top of:
- **Ktor Client**: A multiplatform HTTP client
- **kotlinx.serialization**: For JSON serialization/deserialization
- **KSP (Kotlin Symbol Processing)**: For compile-time API implementation generation

## How to Use

### 1. Define Data Models

Create data classes with `@Serializable` annotation:

```kotlin
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String
)
```

### 2. Define API Interface

Create an interface with KtorFit annotations:

```kotlin
import de.jensklingenberg.ktorfit.http.*

interface UserApi {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): User
    
    @POST("users")
    suspend fun createUser(@Body user: User): User
    
    @GET("users")
    suspend fun getUsers(@Query("page") page: Int): List<User>
}
```

### 3. Configure in DI Module

Add your API to the Koin module:

```kotlin
// In DataRemoteModule.kt
single<UserApi> { get<Ktorfit>().create() }
```

### 4. Use in Repository

Inject and use the API in your repository:

```kotlin
class UserRepository(private val userApi: UserApi) {
    suspend fun getUser(id: String): Result<User> {
        return try {
            val user = userApi.getUser(id)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## Available HTTP Methods

KtorFit supports all standard HTTP methods:
- `@GET` - GET requests
- `@POST` - POST requests
- `@PUT` - PUT requests
- `@DELETE` - DELETE requests
- `@PATCH` - PATCH requests
- `@HEAD` - HEAD requests

## Available Annotations

- `@Path("name")` - Path parameter substitution
- `@Query("name")` - Query parameter
- `@QueryMap` - Multiple query parameters
- `@Body` - Request body
- `@Header("name")` - Custom header
- `@Headers` - Multiple headers
- `@FormUrlEncoded` - Form-encoded data
- `@Multipart` - Multipart data

## Configuration Options

The Ktor HttpClient can be configured in `DataRemoteModule.kt`:

### Adding Authentication
```kotlin
install(Auth) {
    bearer {
        loadTokens {
            // Load tokens
        }
    }
}
```

### Custom Timeouts
```kotlin
install(HttpTimeout) {
    requestTimeoutMillis = 15000
    connectTimeoutMillis = 15000
}
```

### Retry Logic
```kotlin
install(HttpRequestRetry) {
    retryOnServerErrors(maxRetries = 3)
    exponentialDelay()
}
```

## Benefits of KtorFit over Retrofit

1. **KMM Ready**: Works on all Kotlin Multiplatform targets
2. **Coroutine First**: Built with Kotlin coroutines from the ground up
3. **Modern**: Uses latest Kotlin features
4. **Lightweight**: Smaller dependency footprint
5. **Type Safe**: Compile-time code generation with KSP

## References

- [KtorFit Documentation](https://foso.github.io/Ktorfit/)
- [Ktor Client Documentation](https://ktor.io/docs/client.html)
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)
