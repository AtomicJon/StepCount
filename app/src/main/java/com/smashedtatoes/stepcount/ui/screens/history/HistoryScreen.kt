package com.smashedtatoes.stepcount.ui.screens.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smashedtatoes.stepcount.data.db.HourlyStepRecord
import com.smashedtatoes.stepcount.data.db.StepRecord
import com.smashedtatoes.stepcount.ui.components.IntraDayStepChart
import com.smashedtatoes.stepcount.ui.components.MonthCalendarView
import com.smashedtatoes.stepcount.ui.theme.NeonCyan
import com.smashedtatoes.stepcount.ui.theme.NeonPink
import com.smashedtatoes.stepcount.ui.theme.NeonYellow
import com.smashedtatoes.stepcount.ui.theme.VaporwaveOutline
import com.smashedtatoes.stepcount.ui.theme.VaporwaveSurface
import com.smashedtatoes.stepcount.ui.theme.VaporwaveSurfaceVariant
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
    ) {
        // Title
        Text(
            text = "STEP HISTORY",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = NeonCyan,
                fontSize = 24.sp,
                letterSpacing = 2.sp
            ),
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle Segment Buttons (List vs Calendar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.setViewMode(HistoryViewMode.LIST) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (uiState.viewMode == HistoryViewMode.LIST) NeonPink else VaporwaveSurface,
                    contentColor = if (uiState.viewMode == HistoryViewMode.LIST) MaterialTheme.colorScheme.onPrimary else NeonCyan
                ),
                border = BorderStroke(1.dp, if (uiState.viewMode == HistoryViewMode.LIST) NeonPink else VaporwaveOutline),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("List View", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { viewModel.setViewMode(HistoryViewMode.CALENDAR) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (uiState.viewMode == HistoryViewMode.CALENDAR) NeonPink else VaporwaveSurface,
                    contentColor = if (uiState.viewMode == HistoryViewMode.CALENDAR) MaterialTheme.colorScheme.onPrimary else NeonCyan
                ),
                border = BorderStroke(1.dp, if (uiState.viewMode == HistoryViewMode.CALENDAR) NeonPink else VaporwaveOutline),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Month Calendar", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.viewMode == HistoryViewMode.CALENDAR) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                MonthCalendarView(
                    records = uiState.records,
                    goal = uiState.dailyGoal,
                    onDateSelected = { dateStr -> viewModel.setSelectedDate(dateStr) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    IntraDayStepChart(
                        hourlyRecords = uiState.hourlyRecordsForSelectedDate,
                        dateLabel = uiState.selectedDate
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            if (uiState.records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No historical step records yet.\nKeep stepping!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(
                        items = uiState.records,
                        key = { it.date }
                    ) { record ->
                        val isSelected = record.date == uiState.selectedDate
                        HistoryRecordItem(
                            record = record,
                            goal = record.goal,
                            isSelected = isSelected,
                            hourlyRecords = if (isSelected) uiState.hourlyRecordsForSelectedDate else emptyList(),
                            onClick = { viewModel.setSelectedDate(record.date) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryRecordItem(
    record: StepRecord,
    goal: Int,
    isSelected: Boolean,
    hourlyRecords: List<HourlyStepRecord>,
    onClick: () -> Unit
) {
    val progress = (record.steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
    val isGoalMet = record.steps >= goal && record.steps > 0

    val formattedDate = try {
        val parsed = LocalDate.parse(record.date)
        parsed.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"))
    } catch (e: Exception) {
        record.date
    }

    val formattedSteps = NumberFormat.getNumberInstance(Locale.US).format(record.steps)
    val formattedGoal = NumberFormat.getNumberInstance(Locale.US).format(goal)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = VaporwaveSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) NeonPink else VaporwaveOutline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                )

                if (isGoalMet) {
                    Text(
                        text = "★ GOAL MET",
                        color = NeonYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$formattedSteps / $formattedGoal steps",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (isGoalMet) NeonPink else NeonCyan,
                trackColor = VaporwaveSurfaceVariant,
                strokeCap = StrokeCap.Round
            )

            if (isSelected) {
                Spacer(modifier = Modifier.height(14.dp))
                IntraDayStepChart(
                    hourlyRecords = hourlyRecords,
                    dateLabel = record.date
                )
            }
        }
    }
}
