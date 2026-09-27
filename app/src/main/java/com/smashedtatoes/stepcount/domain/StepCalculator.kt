package com.smashedtatoes.stepcount.domain

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object StepCalculator {

    private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

    fun getCurrentDateString(): String {
        return LocalDate.now().format(DATE_FORMATTER)
    }

    fun calculateStepsForToday(
        currentSensorReading: Int,
        baselineSensorReading: Int,
        storedStepsBase: Int
    ): Int {
        if (currentSensorReading < baselineSensorReading) {
            // Device was rebooted, resetting raw sensor steps to 0
            return (storedStepsBase + currentSensorReading).coerceAtLeast(0)
        }
        val stepsDelta = currentSensorReading - baselineSensorReading
        return (storedStepsBase + stepsDelta).coerceAtLeast(0)
    }

    fun calculateDistanceKm(steps: Int): Double {
        // Average stride length = 0.762 meters
        return (steps * 0.000762)
    }

    fun calculateDistanceMiles(steps: Int): Double {
        // Average stride length in miles = 0.0004735 miles
        return (steps * 0.0004735)
    }

    fun calculateCalories(steps: Int): Int {
        // Approx 0.04 calories burned per step
        return (steps * 0.04).toInt()
    }
}
