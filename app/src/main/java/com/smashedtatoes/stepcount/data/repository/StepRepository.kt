package com.smashedtatoes.stepcount.data.repository

import com.smashedtatoes.stepcount.data.db.StepDao
import com.smashedtatoes.stepcount.data.db.StepRecord
import com.smashedtatoes.stepcount.data.pref.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.Random

interface StepRepository {
    fun getTodayRecordFlow(todayDate: String): Flow<StepRecord?>
    fun getAllRecordsFlow(): Flow<List<StepRecord>>
    fun getRecentRecordsFlow(limit: Int): Flow<List<StepRecord>>
    fun getRecordsBetweenDatesFlow(startDate: String, endDate: String): Flow<List<StepRecord>>
    val dailyGoalFlow: Flow<Int>
    val activeTrackingDateFlow: Flow<String>
    val baselineSensorReadingFlow: Flow<Int>
    val storedStepsBaseFlow: Flow<Int>
    val useMilesFlow: Flow<Boolean>

    suspend fun saveStepRecord(record: StepRecord)
    suspend fun updateDailyGoal(goal: Int)
    suspend fun updateTrackingState(date: String, baselineSensor: Int, storedBase: Int)
    suspend fun updateUseMiles(useMiles: Boolean)
    suspend fun generateSampleHistoricalData()
}

class StepRepositoryImpl(
    private val stepDao: StepDao,
    private val preferencesRepository: UserPreferencesRepository
) : StepRepository {

    override fun getTodayRecordFlow(todayDate: String): Flow<StepRecord?> {
        return stepDao.getRecordForDateFlow(todayDate)
    }

    override fun getAllRecordsFlow(): Flow<List<StepRecord>> {
        return stepDao.getAllRecordsFlow()
    }

    override fun getRecentRecordsFlow(limit: Int): Flow<List<StepRecord>> {
        return stepDao.getRecentRecordsFlow(limit)
    }

    override fun getRecordsBetweenDatesFlow(startDate: String, endDate: String): Flow<List<StepRecord>> {
        return stepDao.getRecordsBetweenDatesFlow(startDate, endDate)
    }

    override val dailyGoalFlow: Flow<Int> = preferencesRepository.dailyGoalFlow
    override val activeTrackingDateFlow: Flow<String> = preferencesRepository.activeTrackingDateFlow
    override val baselineSensorReadingFlow: Flow<Int> = preferencesRepository.baselineSensorReadingFlow
    override val storedStepsBaseFlow: Flow<Int> = preferencesRepository.storedStepsBaseFlow
    override val useMilesFlow: Flow<Boolean> = preferencesRepository.useMilesFlow

    override suspend fun saveStepRecord(record: StepRecord) {
        stepDao.insertOrUpdateRecord(record)
    }

    override suspend fun updateDailyGoal(goal: Int) {
        preferencesRepository.updateDailyGoal(goal)
    }

    override suspend fun updateTrackingState(date: String, baselineSensor: Int, storedBase: Int) {
        preferencesRepository.updateTrackingState(date, baselineSensor, storedBase)
    }

    override suspend fun updateUseMiles(useMiles: Boolean) {
        preferencesRepository.updateUseMiles(useMiles)
    }

    override suspend fun generateSampleHistoricalData() {
        val today = LocalDate.now()
        val sampleRecords = mutableListOf<StepRecord>()
        val random = Random()
        for (i in 1..30) {
            val dateStr = today.minusDays(i.toLong()).toString()
            val steps = 3000 + random.nextInt(10000)
            sampleRecords.add(StepRecord(date = dateStr, steps = steps, goal = 10000))
        }
        stepDao.insertAllRecords(sampleRecords)
    }
}
