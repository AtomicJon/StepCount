package com.smashedtatoes.stepcount.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {

    @Query("SELECT * FROM step_records WHERE date = :date LIMIT 1")
    fun getRecordForDateFlow(date: String): Flow<StepRecord?>

    @Query("SELECT * FROM step_records WHERE date = :date LIMIT 1")
    suspend fun getRecordForDate(date: String): StepRecord?

    @Query("SELECT * FROM step_records ORDER BY date DESC")
    fun getAllRecordsFlow(): Flow<List<StepRecord>>

    @Query("SELECT * FROM step_records ORDER BY date DESC LIMIT :limit")
    fun getRecentRecordsFlow(limit: Int): Flow<List<StepRecord>>

    @Query("SELECT * FROM step_records WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getRecordsBetweenDatesFlow(startDate: String, endDate: String): Flow<List<StepRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRecord(record: StepRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRecords(records: List<StepRecord>)

    @Query("DELETE FROM step_records")
    suspend fun deleteAllRecords()
}
