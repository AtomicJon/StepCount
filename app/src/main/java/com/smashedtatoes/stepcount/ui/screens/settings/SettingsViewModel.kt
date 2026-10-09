package com.smashedtatoes.stepcount.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smashedtatoes.stepcount.data.repository.StepRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class SettingsUiState(
    val dailyGoal: Int = 10000,
    val useMiles: Boolean = false,
    val sampleDataMessage: String? = null,
    val isDeveloperMenuVisible: Boolean = com.smashedtatoes.stepcount.BuildConfig.DEBUG,
    val exportMessage: String? = null,
)

class SettingsViewModel(
    private val repository: StepRepository
) : ViewModel() {

    private val _sampleDataMessage = MutableStateFlow<String?>(null)
    private val _exportMessage = MutableStateFlow<String?>(null)

    private val _exportFileEvent = MutableSharedFlow<File>()
    val exportFileEvent: SharedFlow<File> = _exportFileEvent.asSharedFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.dailyGoalFlow,
        repository.useMilesFlow,
        _sampleDataMessage,
        _exportMessage
    ) { goal, useMiles, sampleMsg, exportMsg ->
        SettingsUiState(
            dailyGoal = goal,
            useMiles = useMiles,
            sampleDataMessage = sampleMsg,
            isDeveloperMenuVisible = com.smashedtatoes.stepcount.BuildConfig.DEBUG,
            exportMessage = exportMsg
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

    fun exportDailyCsv(context: Context) {
        viewModelScope.launch {
            try {
                val file = repository.exportDailyCsv(context)
                _exportMessage.value = "Daily step data exported (${file.name})"
                _exportFileEvent.emit(file)
            } catch (e: Exception) {
                _exportMessage.value = "Export failed: ${e.localizedMessage}"
            }
        }
    }

    fun exportHourlyCsv(context: Context) {
        viewModelScope.launch {
            try {
                val file = repository.exportHourlyCsv(context)
                _exportMessage.value = "Hourly step data exported (${file.name})"
                _exportFileEvent.emit(file)
            } catch (e: Exception) {
                _exportMessage.value = "Export failed: ${e.localizedMessage}"
            }
        }
    }

    class Factory(private val repository: StepRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repository) as T
        }
    }
}
