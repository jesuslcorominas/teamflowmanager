package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

interface FilterMatchesUseCase {
    operator fun invoke(
        filterText: String,
        startDate: Long? = null,
        endDate: Long? = null,
    ): Flow<List<Match>>
}

internal class FilterMatchesUseCaseImpl(
    private val matchRepository: MatchRepository,
) : FilterMatchesUseCase {
    override fun invoke(
        filterText: String,
        startDate: Long?,
        endDate: Long?,
    ): Flow<List<Match>> =
        combine(
            matchRepository.getAllMatches(),
            matchRepository.getArchivedMatches(),
        ) { activeMatches, archivedMatches ->
            val allMatches = activeMatches + archivedMatches
            
            allMatches.filter { match ->
                val matchesText =
                    if (filterText.isBlank()) {
                        true
                    } else {
                        val searchText = filterText.trim().lowercase()
                        val opponent = match.opponent?.lowercase() ?: ""
                        val location = match.location?.lowercase() ?: ""
                        opponent.contains(searchText) || location.contains(searchText)
                    }
                
                val matchesDateRange =
                    if (startDate != null && endDate != null && match.date != null) {
                        match.date in startDate..endDate
                    } else {
                        true
                    }
                
                matchesText && matchesDateRange
            }
        }
}
