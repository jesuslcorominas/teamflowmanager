package com.jesuslcorominas.teamflowmanager.data.core.di

import com.jesuslcorominas.teamflowmanager.data.core.repository.AuthRepositoryImpl
import com.jesuslcorominas.teamflowmanager.data.core.repository.GoalRepositoryImpl
import com.jesuslcorominas.teamflowmanager.data.core.repository.MatchRepositoryImpl
import com.jesuslcorominas.teamflowmanager.data.core.repository.PlayerRepositoryImpl
import com.jesuslcorominas.teamflowmanager.data.core.repository.PlayerSubstitutionRepositoryImpl
import com.jesuslcorominas.teamflowmanager.data.core.repository.PlayerTimeHistoryRepositoryImpl
import com.jesuslcorominas.teamflowmanager.data.core.repository.PlayerTimeRepositoryImpl
import com.jesuslcorominas.teamflowmanager.data.core.repository.PreferencesRepositoryImpl
import com.jesuslcorominas.teamflowmanager.data.core.repository.TeamRepositoryImpl
import com.jesuslcorominas.teamflowmanager.usecase.repository.AuthRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.GoalRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerSubstitutionRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeHistoryRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

internal val repositoryModule =
    module {
        single {
            PlayerRepositoryImpl(
                get(named("PLAYER_FIRESTORE_DATA_SOURCE_IMPL")),
                get(named("PLAYER_LOCAL_DATA_SOURCE_IMPL"))
            )
        } bind PlayerRepository::class

        single {
            TeamRepositoryImpl(
                get(named("TEAM_FIRESTORE_DATA_SOURCE_IMPL")),
                get(named("TEAM_LOCAL_DATA_SOURCE_IMPL"))
            )
        } bind TeamRepository::class

        single {
            MatchRepositoryImpl(
                get(named("MATCH_FIRESTORE_DATA_SOURCE_IMPL")),
                get(named("MATCH_LOCAL_DATA_SOURCE_IMPL"))
            )
        } bind MatchRepository::class

        single {
            PlayerTimeRepositoryImpl(
                get(named("PLAYER_TIME_FIRESTORE_DATA_SOURCE_IMPL")),
                get(named("PLAYER_TIME_LOCAL_DATA_SOURCE_IMPL"))
            )
        } bind PlayerTimeRepository::class

        single {
            PlayerTimeHistoryRepositoryImpl(
                get(named("PLAYER_TIME_HISTORY_FIRESTORE_DATA_SOURCE_IMPL")),
                get(named("PLAYER_TIME_HISTORY_LOCAL_DATA_SOURCE_IMPL"))
            )
        } bind PlayerTimeHistoryRepository::class

        single {
            PlayerSubstitutionRepositoryImpl(
                get(named("PLAYER_SUBSTITUTION_FIRESTORE_DATA_SOURCE_IMPL")),
                get(named("PLAYER_SUBSTITUTION_LOCAL_DATA_SOURCE_IMPL"))
            )
        } bind PlayerSubstitutionRepository::class

        singleOf(::PreferencesRepositoryImpl) bind PreferencesRepository::class

        single {
            GoalRepositoryImpl(
                get(named("GOAL_FIRESTORE_DATA_SOURCE_IMPL")),
                get(named("GOAL_LOCAL_DATA_SOURCE_IMPL"))
            )
        } bind GoalRepository::class

        singleOf(::AuthRepositoryImpl) bind AuthRepository::class
    }

val dataCoreModule =
    module {
        includes(repositoryModule)
    }
