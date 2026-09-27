package com.smashedtatoes.stepcount.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smashedtatoes.stepcount.data.db.StepRecord
import com.smashedtatoes.stepcount.ui.theme.NeonCyan
import com.smashedtatoes.stepcount.ui.theme.NeonPink
import com.smashedtatoes.stepcount.ui.theme.NeonYellow
import com.smashedtatoes.stepcount.ui.theme.VaporwaveOutline
import com.smashedtatoes.stepcount.ui.theme.VaporwaveSurface
import com.smashedtatoes.stepcount.ui.theme.VaporwaveSurfaceVariant
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MonthCalendarView(
    records: List<StepRecord>,
    goal: Int,
    modifier: Modifier = Modifier
) {
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val recordsMap = remember(records) {
        records.associateBy { it.date }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = VaporwaveSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, VaporwaveOutline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month header controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentYearMonth = currentYearMonth.minusMonths(1) }) {
                    Text(text = "◄", color = NeonCyan, fontSize = 20.sp)
                }

                Text(
                    text = currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NeonPink,
                        fontSize = 18.sp
                    )
                )

                IconButton(onClick = { currentYearMonth = currentYearMonth.plusMonths(1) }) {
                    Text(text = "►", color = NeonCyan, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day name labels
            val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            Row(modifier = Modifier.fillMaxWidth()) {
                dayNames.forEach { dayName ->
                    Text(
                        text = dayName,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Days grid
            val firstDayOfMonth = currentYearMonth.atDay(1)
            val dayOfWeekOffset = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0
            val daysInMonth = currentYearMonth.lengthOfMonth()

            val totalGridSlots = dayOfWeekOffset + daysInMonth
            val rows = (totalGridSlots + 6) / 7

            Column {
                for (row in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0..6) {
                            val slotIndex = row * 7 + col
                            val dayNum = slotIndex - dayOfWeekOffset + 1

                            if (dayNum in 1..daysInMonth) {
                                val date = currentYearMonth.atDay(dayNum)
                                val dateStr = date.toString()
                                val record = recordsMap[dateStr]
                                val steps = record?.steps ?: 0
                                val isGoalReached = steps >= (record?.goal ?: goal) && steps > 0
                                val isSelected = date == selectedDate

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> NeonPink.copy(alpha = 0.4f)
                                                isGoalReached -> NeonYellow.copy(alpha = 0.25f)
                                                steps > 0 -> VaporwaveSurfaceVariant
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 0.dp,
                                            color = if (isSelected) NeonPink else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedDate = date },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = dayNum.toString(),
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected || isGoalReached) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isGoalReached -> NeonYellow
                                                isSelected -> NeonPink
                                                steps > 0 -> NeonCyan
                                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            }
                                        )
                                        if (isGoalReached) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(NeonYellow)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Selected Day Detail Card
            Spacer(modifier = Modifier.height(16.dp))
            val selectedRecord = recordsMap[selectedDate.toString()]
            val selectedSteps = selectedRecord?.steps ?: 0
            val formattedSelectedSteps = NumberFormat.getNumberInstance(Locale.US).format(selectedSteps)
            val formattedGoal = NumberFormat.getNumberInstance(Locale.US).format(selectedRecord?.goal ?: goal)

            Card(
                colors = CardDefaults.cardColors(containerColor = VaporwaveSurfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = selectedDate.format(DateTimeFormatter.ofPattern("E, MMM d, yyyy")),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        )
                        Text(
                            text = "$formattedSelectedSteps / $formattedGoal steps",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    if (selectedSteps >= (selectedRecord?.goal ?: goal) && selectedSteps > 0) {
                        Text(
                            text = "GOAL REACHED! ★",
                            color = NeonYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
