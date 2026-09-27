package com.smashedtatoes.stepcount.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smashedtatoes.stepcount.data.db.StepRecord
import com.smashedtatoes.stepcount.data.repository.StepRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class HistoryViewMode {
    LIST,
    CALENDAR
}

data class HistoryUiState(
    val records: List<StepRecord> = emptyList(),
    val dailyGoal: Int = 10000,
    val viewMode: HistoryViewMode = HistoryViewMode.LIST
)

class HistoryViewModel(
    private val repository: StepRepository
) : ViewModel() {

    private val _viewMode = MutableStateFlow(HistoryViewMode.LIST)

    val uiState: StateFlow<HistoryUiState> = combine(
        repository.getAllRecordsFlow(),
        repository.dailyGoalFlow,
        _viewMode
    ) { records, goal, viewMode ->
        HistoryUiState(
            records = records,
            dailyGoal = goal,
            viewMode = viewMode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun setViewMode(mode: HistoryViewMode) {
        _viewMode.value = mode
    }

    class Factory(private val repository: StepRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(repository) as T
        }
    }
}
