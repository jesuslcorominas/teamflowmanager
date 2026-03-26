package com.jesuslcorominas.teamflowmanager.data.remote.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.jesuslcorominas.teamflowmanager.data.core.datasource.AuthDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmTokenDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmTokenProviderDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.NotificationTopicDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.ClubDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.ClubMemberDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.DynamicLinkDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.GoalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.ImageStorageDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchOperationDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerSubstitutionDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeHistoryDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.TeamDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.api.ShortLinkApi
import com.jesuslcorominas.teamflowmanager.data.remote.api.createShortLinkApi
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.ClubFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.ClubMemberFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.FcmNotificationTopicDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.FcmTokenFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.FcmTokenProviderDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.FirebaseAuthDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.FirebaseDynamicLinkDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.FirebaseStorageDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.FirestoreTimeProvider
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.GoalFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.MatchFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.MatchOperationFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.PlayerFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.PlayerSubstitutionFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.PlayerTimeFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.PlayerTimeHistoryFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.TeamFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.transaction.FirestoreTransactionRunner
import com.jesuslcorominas.teamflowmanager.domain.utils.TimeProvider
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val firebaseModule =
    module {
        single { FirebaseAuth.getInstance() }
        single { FirebaseFirestore.getInstance() }
        single { FirebaseStorage.getInstance() }
        singleOf(::FirebaseAuthDataSourceImpl) bind AuthDataSource::class
        singleOf(::FcmTokenFirestoreDataSourceImpl) bind FcmTokenDataSource::class
        singleOf(::FcmTokenProviderDataSourceImpl) bind FcmTokenProviderDataSource::class
        singleOf(::FcmNotificationTopicDataSourceImpl) bind NotificationTopicDataSource::class
        singleOf(::FirebaseStorageDataSourceImpl) bind ImageStorageDataSource::class
        single<DynamicLinkDataSource> {
            FirebaseDynamicLinkDataSourceImpl(shortLinkApi = get())
        }
        singleOf(::FirestoreTimeProvider) bind TimeProvider::class
        singleOf(::FirestoreTransactionRunner) bind TransactionRunner::class
    }

internal val firestoreDataSourceModule =
    module {
        singleOf(::PlayerFirestoreDataSourceImpl) bind PlayerDataSource::class
        singleOf(::TeamFirestoreDataSourceImpl) bind TeamDataSource::class
        singleOf(::ClubFirestoreDataSourceImpl) bind ClubDataSource::class
        singleOf(::ClubMemberFirestoreDataSourceImpl) bind ClubMemberDataSource::class
        singleOf(::MatchFirestoreDataSourceImpl) bind MatchDataSource::class
        singleOf(::MatchOperationFirestoreDataSourceImpl) bind MatchOperationDataSource::class
        singleOf(::GoalFirestoreDataSourceImpl) bind GoalDataSource::class
        singleOf(::PlayerSubstitutionFirestoreDataSourceImpl) bind PlayerSubstitutionDataSource::class
        singleOf(::PlayerTimeFirestoreDataSourceImpl) bind PlayerTimeDataSource::class
        singleOf(::PlayerTimeHistoryFirestoreDataSourceImpl) bind PlayerTimeHistoryDataSource::class
    }

internal val ktorfitModule =
    module {
        single {
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            }
        }
        single {
            HttpClient(OkHttp) {
                install(HttpTimeout) {
                    requestTimeoutMillis = 5_000
                    connectTimeoutMillis = 3_000
                    socketTimeoutMillis = 5_000
                }
                install(ContentNegotiation) {
                    json(get())
                }
                install(Logging) {
                    logger =
                        object : Logger {
                            override fun log(message: String) {
                                println("Ktor HTTP Client: $message")
                            }
                        }
                    level = LogLevel.INFO
                }
            }
        }
        single {
            Ktorfit
                .Builder()
                .httpClient(get<HttpClient>())
                .baseUrl("https://us-central1-teamflow-manager-dev.cloudfunctions.net/")
                .build()
        }
        single<ShortLinkApi> {
            get<Ktorfit>().createShortLinkApi()
        }
    }

actual val dataRemoteModule: Module =
    module {
        includes(firebaseModule, firestoreDataSourceModule, ktorfitModule)
    }
