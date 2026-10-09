package com.smashedtatoes.stepcount.data.export

import com.smashedtatoes.stepcount.data.db.HourlyStepRecord
import com.smashedtatoes.stepcount.data.db.StepRecord
import com.smashedtatoes.stepcount.domain.StepCalculator
import java.util.Locale

interface StepExporter {
    fun exportDailyDataCsv(records: List<StepRecord>): String
    fun exportHourlyDataCsv(hourlyRecords: List<HourlyStepRecord>): String
}

object CsvStepExporter : StepExporter {

    override fun exportDailyDataCsv(records: List<StepRecord>): String {
        val sb = StringBuilder()
        sb.append("Date,Steps,Goal,Goal Met,Distance (miles),Distance (km),Calories (kcal)\n")
        
        records.sortedByDescending { it.date }.forEach { record ->
            val distanceMi = String.format(Locale.US, "%.2f", StepCalculator.calculateDistanceMiles(record.steps))
            val distanceKm = String.format(Locale.US, "%.2f", StepCalculator.calculateDistanceKm(record.steps))
            val calories = StepCalculator.calculateCalories(record.steps)
            val goalMet = if ((record.steps >= record.goal) && record.steps > 0) "Yes" else "No"
            sb.append("${record.date},${record.steps},${record.goal},$goalMet,$distanceMi,$distanceKm,$calories\n")
        }
        
        return sb.toString()
    }

    override fun exportHourlyDataCsv(hourlyRecords: List<HourlyStepRecord>): String {
        val sb = StringBuilder()
        sb.append("Date,Hour,Steps,Distance (miles),Distance (km),Calories (kcal)\n")
        
        hourlyRecords.sortedWith(compareByDescending<HourlyStepRecord> { it.date }.thenBy { it.hour }).forEach { record ->
            val distanceMi = String.format(Locale.US, "%.2f", StepCalculator.calculateDistanceMiles(record.steps))
            val distanceKm = String.format(Locale.US, "%.2f", StepCalculator.calculateDistanceKm(record.steps))
            val calories = StepCalculator.calculateCalories(record.steps)
            sb.append("${record.date},${record.hour},${record.steps},$distanceMi,$distanceKm,$calories\n")
        }
        
        return sb.toString()
    }
}
