package com.jesuslcorominas.teamflowmanager.data.remote.di

import com.jesuslcorominas.teamflowmanager.data.core.datasource.AuthDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.FirebaseAuthDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.MatchFirestoreDataSourceImpl
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
    single { Firebase.auth }
    single { Firebase.firestore }
    singleOf(::FirebaseAuthDataSourceImpl) bind AuthDataSource::class
    singleOf(::MatchFirestoreDataSourceImpl) bind MatchDataSource::class
    singleOf(::FirestoreTransactionRunner) bind TransactionRunner::class
}
