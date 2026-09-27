package com.smashedtatoes.stepcount.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smashedtatoes.stepcount.data.db.StepRecord
import com.smashedtatoes.stepcount.data.repository.StepRepository
import com.smashedtatoes.stepcount.domain.StepCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val stepsToday: Int = 0,
    val dailyGoal: Int = 10000,
    val distanceFormatted: String = "0.0 km",
    val calories: Int = 0,
    val recentRecords: List<StepRecord> = emptyList()
)

class HomeViewModel(
    private val repository: StepRepository
) : ViewModel() {

    private val todayStr = StepCalculator.getCurrentDateString()

    val uiState: StateFlow<HomeUiState> = combine(
        repository.getTodayRecordFlow(todayStr),
        repository.dailyGoalFlow,
        repository.getRecentRecordsFlow(7),
        repository.useMilesFlow
    ) { todayRecord, goal, recentRecords, useMiles ->
        val steps = todayRecord?.steps ?: 0
        val dist = if (useMiles) {
            "%.2f mi".format(StepCalculator.calculateDistanceMiles(steps))
        } else {
            "%.2f km".format(StepCalculator.calculateDistanceKm(steps))
        }
        val cals = StepCalculator.calculateCalories(steps)

        HomeUiState(
            stepsToday = steps,
            dailyGoal = goal,
            distanceFormatted = dist,
            calories = cals,
            recentRecords = recentRecords
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    class Factory(private val repository: StepRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}
