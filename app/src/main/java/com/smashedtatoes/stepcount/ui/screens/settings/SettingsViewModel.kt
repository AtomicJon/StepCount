package com.smashedtatoes.stepcount.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smashedtatoes.stepcount.data.repository.StepRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val dailyGoal: Int = 10000,
    val useMiles: Boolean = false,
    val sampleDataMessage: String? = null
)

class SettingsViewModel(
    private val repository: StepRepository
) : ViewModel() {

    private val _sampleDataMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.dailyGoalFlow,
        repository.useMilesFlow,
        _sampleDataMessage
    ) { goal, useMiles, message ->
        SettingsUiState(
            dailyGoal = goal,
            useMiles = useMiles,
            sampleDataMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun updateDailyGoal(goal: Int) {
        viewModelScope.launch {
            repository.updateDailyGoal(goal)
        }
    }

    fun updateUseMiles(useMiles: Boolean) {
        viewModelScope.launch {
            repository.updateUseMiles(useMiles)
        }
    }

    fun generateSampleData() {
        viewModelScope.launch {
            repository.generateSampleHistoricalData()
            _sampleDataMessage.value = "30 days of sample step history added!"
        }
    }

    fun clearSampleDataMessage() {
        _sampleDataMessage.value = null
    }

    class Factory(private val repository: StepRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repository) as T
        }
    }
}
