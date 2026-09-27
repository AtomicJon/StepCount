package com.smashedtatoes.stepcount.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smashedtatoes.stepcount.data.db.StepRecord
import com.smashedtatoes.stepcount.ui.theme.NeonCyan
import com.smashedtatoes.stepcount.ui.theme.NeonPink
import com.smashedtatoes.stepcount.ui.theme.NeonPurple
import com.smashedtatoes.stepcount.ui.theme.NeonYellow
import com.smashedtatoes.stepcount.ui.theme.VaporwaveSurface
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun formatStepCountShort(steps: Int): String {
    return when {
        steps >= 10000 -> "${steps / 1000}k"
        steps >= 1000 -> if (steps % 1000 < 100) "${steps / 1000}k" else "%.1fk".format(steps / 1000.0)
        else -> steps.toString()
    }
}

@Composable
fun VaporwaveStepChart(
    records: List<StepRecord>,
    goal: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = VaporwaveSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Recent Step History",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    fontSize = 18.sp
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No step history yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val chartRecords = records.take(7).reversed()
                val maxSteps = maxOf(goal, chartRecords.maxOfOrNull { it.steps } ?: goal) * 1.2f

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val bottomPadding = 42.dp.toPx()
                    val topPadding = 24.dp.toPx()
                    val chartHeight = height - bottomPadding - topPadding

                    val barCount = chartRecords.size
                    val spacing = width / (barCount * 1.8f)
                    val barWidth = (width - (spacing * (barCount + 1))) / barCount

                    // Goal line
                    val goalY = topPadding + chartHeight - (chartHeight * (goal / maxSteps))
                    drawLine(
                        color = NeonYellow,
                        start = Offset(0f, goalY),
                        end = Offset(width, goalY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                    )

                    // Draw goal text scale label
                    drawIntoCanvas { canvas ->
                        val goalLabelPaint = Paint().apply {
                            color = NeonYellow.toArgb()
                            textSize = 10.sp.toPx()
                            textAlign = Paint.Align.RIGHT
                            isAntiAlias = true
                            isFakeBoldText = true
                        }
                        canvas.nativeCanvas.drawText(
                            "Goal: ${formatStepCountShort(goal)}",
                            width - 8.dp.toPx(),
                            goalY - 4.dp.toPx(),
                            goalLabelPaint
                        )
                    }

                    // Draw bars & step values below/above
                    chartRecords.forEachIndexed { index, record ->
                        val barHeight = (chartHeight * (record.steps / maxSteps)).coerceAtLeast(8f)
                        val x = spacing + index * (barWidth + spacing)
                        val y = topPadding + chartHeight - barHeight

                        val isGoalMet = record.steps >= goal
                        val gradientBrush = Brush.verticalGradient(
                            colors = if (isGoalMet) listOf(NeonPink, NeonPurple) else listOf(NeonCyan.copy(alpha = 0.8f), NeonPurple),
                            startY = y,
                            endY = topPadding + chartHeight
                        )

                        drawRoundRect(
                            brush = gradientBrush,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )

                        // Parse date
                        val parsedDate = try {
                            LocalDate.parse(record.date)
                        } catch (e: Exception) {
                            null
                        }
                        val dayLabel = parsedDate?.format(DateTimeFormatter.ofPattern("EEE")) ?: record.date.takeLast(2)
                        val countText = formatStepCountShort(record.steps)

                        drawIntoCanvas { canvas ->
                            // Step Count Label (above or inside top of bar)
                            val countPaint = Paint().apply {
                                color = if (isGoalMet) NeonYellow.toArgb() else Color.White.toArgb()
                                textSize = 10.sp.toPx()
                                textAlign = Paint.Align.CENTER
                                isAntiAlias = true
                                isFakeBoldText = true
                            }
                            val countY = (y - 5.dp.toPx()).coerceAtLeast(topPadding - 2.dp.toPx())
                            canvas.nativeCanvas.drawText(
                                countText,
                                x + barWidth / 2,
                                countY,
                                countPaint
                            )

                            // Day Label below bar
                            val dayPaint = Paint().apply {
                                color = NeonCyan.toArgb()
                                textSize = 11.sp.toPx()
                                textAlign = Paint.Align.CENTER
                                isAntiAlias = true
                            }
                            canvas.nativeCanvas.drawText(
                                dayLabel,
                                x + barWidth / 2,
                                height - 8.dp.toPx(),
                                dayPaint
                            )
                        }
                    }
                }
            }
        }
    }
}
