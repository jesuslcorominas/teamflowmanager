package com.jesuslcorominas.teamflowmanager.data.remote.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.jesuslcorominas.teamflowmanager.data.core.datasource.AuthDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.ClubMemberDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.GoalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.ImageStorageDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerSubstitutionDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeHistoryDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.TeamDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.ClubMemberFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.FirebaseAuthDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.FirebaseStorageDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.FirestoreTimeProvider
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.GoalFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.MatchFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.PlayerFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.PlayerSubstitutionFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.PlayerTimeFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.PlayerTimeHistoryFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.TeamFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.transaction.FirestoreTransactionRunner
import com.jesuslcorominas.teamflowmanager.domain.utils.TimeProvider
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * DI module for remote data sources including Firebase and KtorFit configuration.
 *
 * This module provides:
 * - Firebase Auth, Firestore, and Storage instances
 * - Firebase-based data source implementations
 * - Ktor HttpClient and Ktorfit for REST API calls
 */

internal val firebaseModule =
    module {
        single { FirebaseAuth.getInstance() }
        single { FirebaseFirestore.getInstance() }
        single { FirebaseStorage.getInstance() }
        singleOf(::FirebaseAuthDataSourceImpl) bind AuthDataSource::class
        singleOf(::FirebaseStorageDataSourceImpl) bind ImageStorageDataSource::class
        singleOf(::FirestoreTimeProvider) bind TimeProvider::class
        singleOf(::FirestoreTransactionRunner) bind TransactionRunner::class
    }

internal val firestoreDataSourceModule =
    module {
        singleOf(::PlayerFirestoreDataSourceImpl) bind PlayerDataSource::class

        singleOf(::TeamFirestoreDataSourceImpl) bind TeamDataSource::class

        singleOf(::ClubMemberFirestoreDataSourceImpl) bind ClubMemberDataSource::class

        singleOf(::MatchFirestoreDataSourceImpl) bind MatchDataSource::class

        singleOf(::GoalFirestoreDataSourceImpl) bind GoalDataSource::class

        singleOf(::PlayerSubstitutionFirestoreDataSourceImpl) bind PlayerSubstitutionDataSource::class

        singleOf(::PlayerTimeFirestoreDataSourceImpl) bind PlayerTimeDataSource::class

        singleOf(::PlayerTimeHistoryFirestoreDataSourceImpl) bind PlayerTimeHistoryDataSource::class
    }

internal val ktorfitModule =
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
                .httpClient(get<HttpClient>()) // Use the configured HttpClient
                .baseUrl("https://api.example.com/") // Replace with your actual base URL
                .build()
        }

        // Add your API interfaces here
        // Example:
        // single<SampleApi> { get<Ktorfit>().create() }
    }

val dataRemoteModule =
    module {
        includes(firebaseModule, firestoreDataSourceModule, ktorfitModule)
    }
