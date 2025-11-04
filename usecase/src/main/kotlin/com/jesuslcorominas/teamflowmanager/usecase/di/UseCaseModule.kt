package com.jesuslcorominas.teamflowmanager.usecase.di

import com.jesuslcorominas.teamflowmanager.usecase.AddPlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.AddPlayerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.ArchiveMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.ArchiveMatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.CreateMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.CreateMatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.CreateTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.CreateTeamUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.DeleteMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.DeleteMatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.DeletePlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.DeletePlayerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.EndTimeoutTimerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.EndTimeoutTimerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.EndTimeoutUseCase
import com.jesuslcorominas.teamflowmanager.usecase.EndTimeoutUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.ExportMatchReportToPdfUseCase
import com.jesuslcorominas.teamflowmanager.usecase.ExportMatchReportToPdfUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.ExportToPdfUseCase
import com.jesuslcorominas.teamflowmanager.usecase.ExportToPdfUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.FinishMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.FinishMatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetExportDataUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetExportDataUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchReportDataUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchReportDataUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetAllMatchesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetAllMatchesUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetAllPlayerTimesUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetArchivedMatchesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetArchivedMatchesUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetCaptainPlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetCaptainPlayerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetDefaultCaptainUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetDefaultCaptainUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchByIdUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchSubstitutionsUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchSubstitutionsUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchSummaryUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchSummaryUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayerByIdUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayerByIdUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayerGoalStatsUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayerTimeStatsUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayerTimeUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayerTimeUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetPreviousCaptainsUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPreviousCaptainsUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetTeamUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.PauseMatchTimerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.PauseMatchTimerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.PauseMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.PauseMatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.PausePlayerTimerForMatchPauseUseCase
import com.jesuslcorominas.teamflowmanager.usecase.PausePlayerTimerForMatchPauseUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.PausePlayerTimerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.PausePlayerTimerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.RegisterGoalUseCase
import com.jesuslcorominas.teamflowmanager.usecase.RegisterGoalUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.RegisterPlayerSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.usecase.RegisterPlayerSubstitutionUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.ResumeMatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.SaveDefaultCaptainUseCase
import com.jesuslcorominas.teamflowmanager.usecase.SaveDefaultCaptainUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.StartMatchTimerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.StartMatchTimerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.StartPlayerTimerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.StartPlayerTimerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.StartTimeoutTimerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.StartTimeoutTimerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.StartTimeoutUseCase
import com.jesuslcorominas.teamflowmanager.usecase.StartTimeoutUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.UnarchiveMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UnarchiveMatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.UpdateMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UpdateMatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.UpdatePlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UpdatePlayerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.UpdateScheduledMatchesCaptainUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UpdateScheduledMatchesCaptainUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.UpdateTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UpdateTeamUseCaseImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val useCaseInternalModule =
    module {
        singleOf(::GetPlayersUseCaseImpl) bind GetPlayersUseCase::class
        singleOf(::GetPlayerByIdUseCaseImpl) bind GetPlayerByIdUseCase::class
        singleOf(::AddPlayerUseCaseImpl) bind AddPlayerUseCase::class
        singleOf(::DeletePlayerUseCaseImpl) bind DeletePlayerUseCase::class
        singleOf(::UpdatePlayerUseCaseImpl) bind UpdatePlayerUseCase::class

        singleOf(::GetTeamUseCaseImpl) bind GetTeamUseCase::class
        singleOf(::CreateTeamUseCaseImpl) bind CreateTeamUseCase::class
        singleOf(::UpdateTeamUseCaseImpl) bind UpdateTeamUseCase::class

        singleOf(::GetMatchByIdUseCaseImpl) bind GetMatchByIdUseCase::class
        singleOf(::GetAllMatchesUseCaseImpl) bind GetAllMatchesUseCase::class
        singleOf(::GetArchivedMatchesUseCaseImpl) bind GetArchivedMatchesUseCase::class
        singleOf(::CreateMatchUseCaseImpl) bind CreateMatchUseCase::class
        singleOf(::UpdateMatchUseCaseImpl) bind UpdateMatchUseCase::class
        singleOf(::DeleteMatchUseCaseImpl) bind DeleteMatchUseCase::class
        singleOf(::ArchiveMatchUseCaseImpl) bind ArchiveMatchUseCase::class
        singleOf(::UnarchiveMatchUseCaseImpl) bind UnarchiveMatchUseCase::class
        singleOf(::StartMatchTimerUseCaseImpl) bind StartMatchTimerUseCase::class
        singleOf(::PauseMatchTimerUseCaseImpl) bind PauseMatchTimerUseCase::class
        singleOf(::PauseMatchUseCaseImpl) bind PauseMatchUseCase::class
        singleOf(::ResumeMatchUseCaseImpl) bind ResumeMatchUseCase::class
        singleOf(::StartTimeoutTimerUseCaseImpl) bind StartTimeoutTimerUseCase::class
        singleOf(::EndTimeoutTimerUseCaseImpl) bind EndTimeoutTimerUseCase::class
        singleOf(::StartTimeoutUseCaseImpl) bind StartTimeoutUseCase::class
        singleOf(::EndTimeoutUseCaseImpl) bind EndTimeoutUseCase::class
        singleOf(::FinishMatchUseCaseImpl) bind FinishMatchUseCase::class
        singleOf(::GetMatchSummaryUseCaseImpl) bind GetMatchSummaryUseCase::class

        singleOf(::GetPlayerTimeUseCaseImpl) bind GetPlayerTimeUseCase::class
        singleOf(::GetAllPlayerTimesUseCaseImpl) bind GetAllPlayerTimesUseCase::class
        singleOf(::GetPlayerTimeStatsUseCase)
        singleOf(::GetPlayerGoalStatsUseCase)
        singleOf(::GetExportDataUseCaseImpl) bind GetExportDataUseCase::class
        singleOf(::StartPlayerTimerUseCaseImpl) bind StartPlayerTimerUseCase::class
        singleOf(::PausePlayerTimerUseCaseImpl) bind PausePlayerTimerUseCase::class
        singleOf(::PausePlayerTimerForMatchPauseUseCaseImpl) bind PausePlayerTimerForMatchPauseUseCase::class

        singleOf(::RegisterPlayerSubstitutionUseCaseImpl) bind RegisterPlayerSubstitutionUseCase::class
        singleOf(::GetMatchSubstitutionsUseCaseImpl) bind GetMatchSubstitutionsUseCase::class

        singleOf(::GetPreviousCaptainsUseCaseImpl) bind GetPreviousCaptainsUseCase::class
        singleOf(::GetDefaultCaptainUseCaseImpl) bind GetDefaultCaptainUseCase::class
        singleOf(::SaveDefaultCaptainUseCaseImpl) bind SaveDefaultCaptainUseCase::class
        singleOf(::GetCaptainPlayerUseCaseImpl) bind GetCaptainPlayerUseCase::class
        singleOf(::UpdateScheduledMatchesCaptainUseCaseImpl) bind UpdateScheduledMatchesCaptainUseCase::class

        singleOf(::RegisterGoalUseCaseImpl) bind RegisterGoalUseCase::class

        singleOf(::ExportToPdfUseCaseImpl) bind ExportToPdfUseCase::class
        singleOf(::GetMatchReportDataUseCaseImpl) bind GetMatchReportDataUseCase::class
        singleOf(::ExportMatchReportToPdfUseCaseImpl) bind ExportMatchReportToPdfUseCase::class
    }

val useCaseModule =
    module {
        includes(useCaseInternalModule)
    }
