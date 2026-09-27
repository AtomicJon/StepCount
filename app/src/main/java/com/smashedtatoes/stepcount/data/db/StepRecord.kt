package com.smashedtatoes.stepcount.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_records")
data class StepRecord(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val steps: Int,
    val goal: Int
)
