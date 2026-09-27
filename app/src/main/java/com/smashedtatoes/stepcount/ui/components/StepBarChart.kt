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
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No step history yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val chartRecords = records.take(7).reversed()
                val maxSteps = maxOf(goal, chartRecords.maxOfOrNull { it.steps } ?: goal) * 1.15f

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val bottomPadding = 30.dp.toPx()
                    val chartHeight = height - bottomPadding

                    val barCount = chartRecords.size
                    val spacing = width / (barCount * 1.8f)
                    val barWidth = (width - (spacing * (barCount + 1))) / barCount

                    // Draw goal line
                    val goalY = chartHeight - (chartHeight * (goal / maxSteps))
                    drawLine(
                        color = NeonYellow,
                        start = Offset(0f, goalY),
                        end = Offset(width, goalY),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                    )

                    // Draw bars
                    chartRecords.forEachIndexed { index, record ->
                        val barHeight = (chartHeight * (record.steps / maxSteps)).coerceAtLeast(10f)
                        val x = spacing + index * (barWidth + spacing)
                        val y = chartHeight - barHeight

                        val gradientBrush = Brush.verticalGradient(
                            colors = listOf(NeonPink, NeonPurple),
                            startY = y,
                            endY = chartHeight
                        )

                        drawRoundRect(
                            brush = gradientBrush,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )

                        // Label date below bar
                        val parsedDate = try {
                            LocalDate.parse(record.date)
                        } catch (e: Exception) {
                            null
                        }
                        val dayLabel = parsedDate?.format(DateTimeFormatter.ofPattern("EEE")) ?: record.date.takeLast(2)

                        drawIntoCanvas { canvas ->
                            val textPaint = Paint().apply {
                                color = NeonCyan.toArgb()
                                textSize = 11.sp.toPx()
                                textAlign = Paint.Align.CENTER
                                isAntiAlias = true
                            }
                            canvas.nativeCanvas.drawText(
                                dayLabel,
                                x + barWidth / 2,
                                height - 6.dp.toPx(),
                                textPaint
                            )
                        }
                    }
                }
            }
        }
    }
}
