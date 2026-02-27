package com.jesuslcorominas.teamflowmanager.data.remote.di

import com.jesuslcorominas.teamflowmanager.data.core.datasource.AuthDataSource
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
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.ClubFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.ClubMemberFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.FirebaseAuthDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.GoalFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.MatchFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.MatchOperationFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.NoOpDynamicLinkDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.NoOpImageStorageDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.PlayerFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.PlayerSubstitutionFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.PlayerTimeFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.PlayerTimeHistoryFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.TeamFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.transaction.FirestoreTransactionRunner
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val dataRemoteModule: Module = module {
    // Firebase instances
    single { Firebase.auth }
    single { Firebase.firestore }

    // Infrastructure
    singleOf(::FirestoreTransactionRunner) bind TransactionRunner::class

    // Auth
    singleOf(::FirebaseAuthDataSourceImpl) bind AuthDataSource::class

    // Real Firestore implementations
    singleOf(::TeamFirestoreDataSourceImpl) bind TeamDataSource::class
    singleOf(::MatchFirestoreDataSourceImpl) bind MatchDataSource::class
    singleOf(::ClubMemberFirestoreDataSourceImpl) bind ClubMemberDataSource::class

    // Phase 2 stubs — read operations return empty/null, writes throw NotImplementedError
    single<ClubDataSource> { ClubFirestoreDataSourceImpl() }
    single<PlayerDataSource> { PlayerFirestoreDataSourceImpl() }
    single<GoalDataSource> { GoalFirestoreDataSourceImpl() }
    single<PlayerSubstitutionDataSource> { PlayerSubstitutionFirestoreDataSourceImpl() }
    single<PlayerTimeDataSource> { PlayerTimeFirestoreDataSourceImpl() }
    single<PlayerTimeHistoryDataSource> { PlayerTimeHistoryFirestoreDataSourceImpl() }
    single<MatchOperationDataSource> { MatchOperationFirestoreDataSourceImpl() }
    single<ImageStorageDataSource> { NoOpImageStorageDataSource() }
    single<DynamicLinkDataSource> { NoOpDynamicLinkDataSource() }
}
