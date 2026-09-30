package com.smashedtatoes.stepcount.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smashedtatoes.stepcount.data.db.HourlyStepRecord
import com.smashedtatoes.stepcount.data.db.StepRecord
import com.smashedtatoes.stepcount.data.repository.StepRepository
import com.smashedtatoes.stepcount.domain.StepCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

enum class HistoryViewMode {
    LIST,
    CALENDAR
}

data class HistoryUiState(
    val records: List<StepRecord> = emptyList(),
    val dailyGoal: Int = 10000,
    val viewMode: HistoryViewMode = HistoryViewMode.LIST,
    val selectedDate: String = StepCalculator.getCurrentDateString(),
    val hourlyRecordsForSelectedDate: List<HourlyStepRecord> = emptyList()
)

class HistoryViewModel(
    private val repository: StepRepository
) : ViewModel() {

    private val _viewMode = MutableStateFlow(HistoryViewMode.LIST)
    private val _selectedDate = MutableStateFlow(StepCalculator.getCurrentDateString())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HistoryUiState> = combine(
        repository.getAllRecordsFlow(),
        repository.dailyGoalFlow,
        _viewMode,
        _selectedDate,
        _selectedDate.flatMapLatest { repository.getHourlyRecordsForDateFlow(it) }
    ) { records, goal, viewMode, selectedDate, hourlyRecords ->
        HistoryUiState(
            records = records,
            dailyGoal = goal,
            viewMode = viewMode,
            selectedDate = selectedDate,
            hourlyRecordsForSelectedDate = hourlyRecords
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun setViewMode(mode: HistoryViewMode) {
        _viewMode.value = mode
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    class Factory(private val repository: StepRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(repository) as T
        }
    }
}
