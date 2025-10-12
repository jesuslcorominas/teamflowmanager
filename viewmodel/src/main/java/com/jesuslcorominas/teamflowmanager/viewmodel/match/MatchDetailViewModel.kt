package com.jesuslcorominas.teamflowmanager.viewmodel.match

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.usecase.match.GetMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.match.PauseMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.match.ResumeMatchUseCase
import kotlinx.coroutines.launch

class MatchDetailViewModel(
    private val getMatchUseCase: GetMatchUseCase,
    private val pauseMatchUseCase: PauseMatchUseCase,
    private val resumeMatchUseCase: ResumeMatchUseCase
) : ViewModel() {

    private val _match = MutableLiveData<Match?>()
    val match: LiveData<Match?> = _match

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadMatch(matchId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = getMatchUseCase(matchId)
                _match.value = result
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun pauseMatch(matchId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = pauseMatchUseCase(matchId)
                result.onSuccess { pausedMatch ->
                    _match.value = pausedMatch
                    _error.value = null
                }.onFailure { exception ->
                    _error.value = exception.message
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resumeMatch(matchId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = resumeMatchUseCase(matchId)
                result.onSuccess { resumedMatch ->
                    _match.value = resumedMatch
                    _error.value = null
                }.onFailure { exception ->
                    _error.value = exception.message
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
