package com.smashedtatoes.stepcount.data.repository

import android.content.Context
import com.smashedtatoes.stepcount.data.db.HourlyStepRecord
import com.smashedtatoes.stepcount.data.db.StepDao
import com.smashedtatoes.stepcount.data.db.StepRecord
import com.smashedtatoes.stepcount.data.export.CsvStepExporter
import com.smashedtatoes.stepcount.data.export.StepExporter
import com.smashedtatoes.stepcount.data.pref.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.time.LocalDate
import java.util.Random

interface StepRepository {
    fun getTodayRecordFlow(todayDate: String): Flow<StepRecord?>
    fun getAllRecordsFlow(): Flow<List<StepRecord>>
    suspend fun getAllRecords(): List<StepRecord>
    fun getRecentRecordsFlow(limit: Int): Flow<List<StepRecord>>
    fun getRecordsBetweenDatesFlow(startDate: String, endDate: String): Flow<List<StepRecord>>
    fun getHourlyRecordsForDateFlow(date: String): Flow<List<HourlyStepRecord>>
    suspend fun getAllHourlyRecords(): List<HourlyStepRecord>

    val dailyGoalFlow: Flow<Int>
    val activeTrackingDateFlow: Flow<String>
    val baselineSensorReadingFlow: Flow<Int>
    val storedStepsBaseFlow: Flow<Int>
    val useMilesFlow: Flow<Boolean>

    suspend fun saveStepRecord(record: StepRecord)
    suspend fun saveHourlyStepRecord(record: HourlyStepRecord)
    suspend fun updateDailyGoal(goal: Int)
    suspend fun updateTrackingState(date: String, baselineSensor: Int, storedBase: Int)
    suspend fun updateUseMiles(useMiles: Boolean)
    suspend fun generateSampleHistoricalData()
    suspend fun exportDailyCsv(context: Context): File
    suspend fun exportHourlyCsv(context: Context): File
}

class StepRepositoryImpl(
    private val stepDao: StepDao,
    private val preferencesRepository: UserPreferencesRepository,
    private val stepExporter: StepExporter = CsvStepExporter,
) : StepRepository {

    override fun getTodayRecordFlow(todayDate: String): Flow<StepRecord?> {
        return stepDao.getRecordForDateFlow(todayDate)
    }

    override fun getAllRecordsFlow(): Flow<List<StepRecord>> {
        return stepDao.getAllRecordsFlow()
    }

    override suspend fun getAllRecords(): List<StepRecord> {
        return stepDao.getAllRecords()
    }

    override fun getRecentRecordsFlow(limit: Int): Flow<List<StepRecord>> {
        return stepDao.getRecentRecordsFlow(limit)
    }

    override fun getRecordsBetweenDatesFlow(startDate: String, endDate: String): Flow<List<StepRecord>> {
        return stepDao.getRecordsBetweenDatesFlow(startDate, endDate)
    }

    override fun getHourlyRecordsForDateFlow(date: String): Flow<List<HourlyStepRecord>> {
        return stepDao.getHourlyRecordsForDateFlow(date)
    }

    override suspend fun getAllHourlyRecords(): List<HourlyStepRecord> {
        return stepDao.getAllHourlyRecords()
    }

    override val dailyGoalFlow: Flow<Int> = preferencesRepository.dailyGoalFlow
    override val activeTrackingDateFlow: Flow<String> = preferencesRepository.activeTrackingDateFlow
    override val baselineSensorReadingFlow: Flow<Int> = preferencesRepository.baselineSensorReadingFlow
    override val storedStepsBaseFlow: Flow<Int> = preferencesRepository.storedStepsBaseFlow
    override val useMilesFlow: Flow<Boolean> = preferencesRepository.useMilesFlow

    override suspend fun saveStepRecord(record: StepRecord) {
        stepDao.insertOrUpdateRecord(record)
    }

    override suspend fun saveHourlyStepRecord(record: HourlyStepRecord) {
        stepDao.insertOrUpdateHourlyRecord(record)
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
        val hourlyRecords = mutableListOf<HourlyStepRecord>()
        val random = Random()

        val hourlyWeights = doubleArrayOf(
            0.0, 0.0, 0.0, 0.0, 0.0, 0.05,
            0.15, 0.35, 0.25, 0.15, 0.10, 0.15,
            0.30, 0.20, 0.15, 0.15, 0.20, 0.35,
            0.30, 0.20, 0.10, 0.05, 0.0, 0.0
        )
        val totalWeight = hourlyWeights.sum()

        for (i in 0..30) {
            val dateStr = today.minusDays(i.toLong()).toString()
            val totalSteps = 3000 + random.nextInt(10000)
            sampleRecords.add(StepRecord(date = dateStr, steps = totalSteps, goal = 10000))

            var remainingSteps = totalSteps
            for (h in 0..23) {
                val targetStepsForHour = if (h == 23) {
                    remainingSteps
                } else {
                    val basePortion = (totalSteps * (hourlyWeights[h] / totalWeight)).toInt()
                    val jitter = if (basePortion > 0) random.nextInt(100) - 50 else 0
                    (basePortion + jitter).coerceIn(0, remainingSteps)
                }
                remainingSteps -= targetStepsForHour
                hourlyRecords.add(HourlyStepRecord(date = dateStr, hour = h, steps = targetStepsForHour))
            }
        }

        stepDao.insertAllRecords(sampleRecords)
        stepDao.insertAllHourlyRecords(hourlyRecords)
    }

    override suspend fun exportDailyCsv(context: Context): File {
        val records = getAllRecords()
        val csvContent = stepExporter.exportDailyDataCsv(records)
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val dateStr = LocalDate.now().toString()
        val file = File(exportDir, "step_count_daily_$dateStr.csv")
        file.writeText(csvContent)
        return file
    }

    override suspend fun exportHourlyCsv(context: Context): File {
        val hourlyRecords = getAllHourlyRecords()
        val csvContent = stepExporter.exportHourlyDataCsv(hourlyRecords)
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val dateStr = LocalDate.now().toString()
        val file = File(exportDir, "step_count_hourly_$dateStr.csv")
        file.writeText(csvContent)
        return file
    }
}
