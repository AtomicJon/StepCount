package com.smashedtatoes.stepcount.export

import com.smashedtatoes.stepcount.data.db.HourlyStepRecord
import com.smashedtatoes.stepcount.data.db.StepRecord
import com.smashedtatoes.stepcount.data.export.CsvStepExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StepDataExporterTest {

    @Test
    fun testExportDailyDataCsv_Formatting() {
        val records = listOf(
            StepRecord(date = "2026-03-30", steps = 10000, goal = 10000),
            StepRecord(date = "2026-03-29", steps = 5000, goal = 10000)
        )

        val csv = CsvStepExporter.exportDailyDataCsv(records)
        val lines = csv.trim().split("\n")

        assertEquals(3, lines.size)
        assertEquals("Date,Steps,Goal,Goal Met,Distance (miles),Distance (km),Calories (kcal)", lines[0])
        assertTrue(lines[1].startsWith("2026-03-30,10000,10000,Yes,"))
        assertTrue(lines[2].startsWith("2026-03-29,5000,10000,No,"))
    }

    @Test
    fun testExportHourlyDataCsv_Formatting() {
        val hourlyRecords = listOf(
            HourlyStepRecord(date = "2026-03-30", hour = 8, steps = 1200),
            HourlyStepRecord(date = "2026-03-30", hour = 9, steps = 800)
        )

        val csv = CsvStepExporter.exportHourlyDataCsv(hourlyRecords)
        val lines = csv.trim().split("\n")

        assertEquals(3, lines.size)
        assertEquals("Date,Hour,Steps,Distance (miles),Distance (km),Calories (kcal)", lines[0])
        assertTrue(lines[1].startsWith("2026-03-30,8,1200,"))
        assertTrue(lines[2].startsWith("2026-03-30,9,800,"))
    }
}
