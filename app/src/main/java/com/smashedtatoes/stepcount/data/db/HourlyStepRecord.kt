package com.smashedtatoes.stepcount.data.db

import androidx.room.Entity

@Entity(tableName = "hourly_step_records", primaryKeys = ["date", "hour"])
data class HourlyStepRecord(
    val date: String, // YYYY-MM-DD
    val hour: Int,    // 0..23
    val steps: Int
)
