package com.smashedtatoes.stepcount.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class StepCalculatorTest {

    @Test
    fun testStepCalculation_NormalMovement() {
        val steps = StepCalculator.calculateStepsForToday(
            currentSensorReading = 5000,
            baselineSensorReading = 1000,
            storedStepsBase = 0
        )
        assertEquals(4000, steps)
    }

    @Test
    fun testStepCalculation_DeviceReboot() {
        val steps = StepCalculator.calculateStepsForToday(
            currentSensorReading = 200,
            baselineSensorReading = 5000, // old reading before reboot
            storedStepsBase = 4000
        )
        assertEquals(4200, steps)
    }

    @Test
    fun testCalculations() {
        val steps = 10000
        val km = StepCalculator.calculateDistanceKm(steps)
        val miles = StepCalculator.calculateDistanceMiles(steps)
        val calories = StepCalculator.calculateCalories(steps)

        assertEquals(7.62, km, 0.01)
        assertEquals(4.735, miles, 0.01)
        assertEquals(400, calories)
    }
}
