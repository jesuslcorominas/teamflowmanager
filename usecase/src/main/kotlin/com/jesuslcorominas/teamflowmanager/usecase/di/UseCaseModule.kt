package com.jesuslcorominas.teamflowmanager.usecase.di

import com.jesuslcorominas.teamflowmanager.domain.usecase.AddPlayerUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ArchiveMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.CreateClubUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.CreateMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.CreateTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.JoinClubByCodeUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.DeleteMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.DeletePlayerUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.EndTimeoutUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ExportMatchReportToPdfUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ExportToPdfUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.FinishMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetActiveMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllMatchesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetArchivedMatchesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCaptainPlayerUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetDefaultCaptainUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetExportDataUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchReportDataUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchSubstitutionsUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchSummaryUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchTimelineUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayerByIdUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayerGoalStatsUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayerTimeStatsUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPreviousCaptainsUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetScheduledMatchesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.HasNotificationPermissionBeenRequestedUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.HasScheduledMatchesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.PauseMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.PausePlayerTimerForMatchPauseUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RegisterGoalUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RegisterPlayerSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RemovePlayerAsCaptainUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SaveDefaultCaptainUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SetNotificationPermissionRequestedUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SetPlayerAsCaptainUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SetShouldShowInvalidSubstitutionAlertUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ShouldShowInvalidSubstitutionAlertUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SignInWithGoogleUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SignOutUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.StartMatchTimerUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.StartPlayerTimersBatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.StartTimeoutUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SynchronizeTimeUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.UnarchiveMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdateMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdatePlayerUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdateScheduledMatchesCaptainUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdateTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.AddPlayerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.ArchiveMatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.CreateClubUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.CreateMatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.CreateTeamUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.JoinClubByCodeUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.DeleteMatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.DeletePlayerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.EndTimeoutUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.ExportMatchReportToPdfUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.ExportToPdfUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.FinishMatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetActiveMatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetAllMatchesUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetAllPlayerTimesUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetArchivedMatchesUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetCaptainPlayerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetCurrentUserUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetDefaultCaptainUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetExportDataUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchByIdUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchReportDataUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchSubstitutionsUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchSummaryUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchTimelineUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayerByIdUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayerGoalStatsUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayerTimeStatsUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetPreviousCaptainsUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetScheduledMatchesUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetTeamUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetUserClubMembershipUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.HasNotificationPermissionBeenRequestedUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.HasScheduledMatchesUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.PauseMatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.PausePlayerTimerForMatchPauseUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.RegisterGoalUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.RegisterPlayerSubstitutionUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.RemovePlayerAsCaptainUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.ResumeMatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.SaveDefaultCaptainUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.SetNotificationPermissionRequestedUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.SetPlayerAsCaptainUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.SetShouldShowInvalidSubstitutionAlertUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.ShouldShowInvalidSubstitutionAlertUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.SignInWithGoogleUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.SignOutUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.StartMatchTimerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.StartPlayerTimersBatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.StartTimeoutUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.SynchronizeTimeUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.UnarchiveMatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.UpdateMatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.UpdatePlayerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.UpdateScheduledMatchesCaptainUseCaseImpl
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
        singleOf(::GetUserClubMembershipUseCaseImpl) bind GetUserClubMembershipUseCase::class
        singleOf(::CreateClubUseCaseImpl) bind CreateClubUseCase::class
        singleOf(::JoinClubByCodeUseCaseImpl) bind JoinClubByCodeUseCase::class

        singleOf(::GetMatchByIdUseCaseImpl) bind GetMatchByIdUseCase::class
        singleOf(::GetAllMatchesUseCaseImpl) bind GetAllMatchesUseCase::class
        singleOf(::GetArchivedMatchesUseCaseImpl) bind GetArchivedMatchesUseCase::class
        singleOf(::HasScheduledMatchesUseCaseImpl) bind HasScheduledMatchesUseCase::class
        singleOf(::GetScheduledMatchesUseCaseImpl) bind GetScheduledMatchesUseCase::class
        singleOf(::CreateMatchUseCaseImpl) bind CreateMatchUseCase::class
        singleOf(::UpdateMatchUseCaseImpl) bind UpdateMatchUseCase::class
        singleOf(::DeleteMatchUseCaseImpl) bind DeleteMatchUseCase::class
        singleOf(::ArchiveMatchUseCaseImpl) bind ArchiveMatchUseCase::class
        singleOf(::UnarchiveMatchUseCaseImpl) bind UnarchiveMatchUseCase::class
        singleOf(::StartMatchTimerUseCaseImpl) bind StartMatchTimerUseCase::class
        singleOf(::PauseMatchUseCaseImpl) bind PauseMatchUseCase::class
        singleOf(::ResumeMatchUseCaseImpl) bind ResumeMatchUseCase::class
        singleOf(::StartTimeoutUseCaseImpl) bind StartTimeoutUseCase::class
        singleOf(::EndTimeoutUseCaseImpl) bind EndTimeoutUseCase::class
        singleOf(::FinishMatchUseCaseImpl) bind FinishMatchUseCase::class
        singleOf(::GetMatchSummaryUseCaseImpl) bind GetMatchSummaryUseCase::class
        singleOf(::GetMatchTimelineUseCaseImpl) bind GetMatchTimelineUseCase::class

        singleOf(::GetAllPlayerTimesUseCaseImpl) bind GetAllPlayerTimesUseCase::class
        singleOf(::GetPlayerTimeStatsUseCaseImpl) bind GetPlayerTimeStatsUseCase::class
        singleOf(::GetPlayerGoalStatsUseCaseImpl) bind GetPlayerGoalStatsUseCase::class
        singleOf(::GetExportDataUseCaseImpl) bind GetExportDataUseCase::class
        singleOf(::PausePlayerTimerForMatchPauseUseCaseImpl) bind PausePlayerTimerForMatchPauseUseCase::class
        singleOf(::StartPlayerTimersBatchUseCaseImpl) bind StartPlayerTimersBatchUseCase::class

        singleOf(::RegisterPlayerSubstitutionUseCaseImpl) bind RegisterPlayerSubstitutionUseCase::class
        singleOf(::GetMatchSubstitutionsUseCaseImpl) bind GetMatchSubstitutionsUseCase::class

        singleOf(::GetPreviousCaptainsUseCaseImpl) bind GetPreviousCaptainsUseCase::class
        singleOf(::GetDefaultCaptainUseCaseImpl) bind GetDefaultCaptainUseCase::class
        singleOf(::SaveDefaultCaptainUseCaseImpl) bind SaveDefaultCaptainUseCase::class
        singleOf(::GetCaptainPlayerUseCaseImpl) bind GetCaptainPlayerUseCase::class
        singleOf(::UpdateScheduledMatchesCaptainUseCaseImpl) bind UpdateScheduledMatchesCaptainUseCase::class
        singleOf(::SetPlayerAsCaptainUseCaseImpl) bind SetPlayerAsCaptainUseCase::class
        singleOf(::RemovePlayerAsCaptainUseCaseImpl) bind RemovePlayerAsCaptainUseCase::class

        singleOf(::HasNotificationPermissionBeenRequestedUseCaseImpl) bind HasNotificationPermissionBeenRequestedUseCase::class
        singleOf(::SetNotificationPermissionRequestedUseCaseImpl) bind SetNotificationPermissionRequestedUseCase::class
        singleOf(::ShouldShowInvalidSubstitutionAlertUseCaseImpl) bind ShouldShowInvalidSubstitutionAlertUseCase::class
        singleOf(::SetShouldShowInvalidSubstitutionAlertUseCaseImpl) bind SetShouldShowInvalidSubstitutionAlertUseCase::class

        singleOf(::RegisterGoalUseCaseImpl) bind RegisterGoalUseCase::class

        singleOf(::ExportToPdfUseCaseImpl) bind ExportToPdfUseCase::class
        singleOf(::GetMatchReportDataUseCaseImpl) bind GetMatchReportDataUseCase::class
        singleOf(::ExportMatchReportToPdfUseCaseImpl) bind ExportMatchReportToPdfUseCase::class

        // Auth use cases
        singleOf(::GetCurrentUserUseCaseImpl) bind GetCurrentUserUseCase::class
        singleOf(::SignInWithGoogleUseCaseImpl) bind SignInWithGoogleUseCase::class
        singleOf(::SignOutUseCaseImpl) bind SignOutUseCase::class

        // Time synchronization
        singleOf(::SynchronizeTimeUseCaseImpl) bind SynchronizeTimeUseCase::class

        singleOf(::GetActiveMatchUseCaseImpl) bind GetActiveMatchUseCase::class
    }

val useCaseModule =
    module {
        includes(useCaseInternalModule)
    }
