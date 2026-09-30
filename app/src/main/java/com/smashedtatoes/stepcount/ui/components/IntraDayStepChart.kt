package com.smashedtatoes.stepcount.ui.components

import android.graphics.Paint
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smashedtatoes.stepcount.data.db.HourlyStepRecord
import com.smashedtatoes.stepcount.ui.theme.NeonCyan
import com.smashedtatoes.stepcount.ui.theme.NeonPink
import com.smashedtatoes.stepcount.ui.theme.NeonPurple
import com.smashedtatoes.stepcount.ui.theme.NeonYellow
import com.smashedtatoes.stepcount.ui.theme.VaporwaveOutline
import com.smashedtatoes.stepcount.ui.theme.VaporwaveSurface
import java.text.NumberFormat
import java.util.Locale

@Composable
fun IntraDayStepChart(
    hourlyRecords: List<HourlyStepRecord>,
    dateLabel: String,
    modifier: Modifier = Modifier
) {
    val recordsMap = hourlyRecords.associateBy { it.hour }
    val hourlyStepsList = (0..23).map { hour -> recordsMap[hour]?.steps ?: 0 }
    val maxHourSteps = hourlyStepsList.maxOrNull() ?: 0
    val peakHour = hourlyStepsList.indexOfFirst { it == maxHourSteps && maxHourSteps > 0 }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VaporwaveSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, VaporwaveOutline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Stacked Header: Title on top, Peak detail below
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "24-HOUR STEP BREAKDOWN",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                )

                if (maxHourSteps > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    val formattedPeak = NumberFormat.getNumberInstance(Locale.US).format(maxHourSteps)
                    val timeString = if (peakHour >= 0) {
                        val hourLabel = when {
                            peakHour == 0 -> "12am"
                            peakHour == 12 -> "12pm"
                            peakHour > 12 -> "${peakHour - 12}pm"
                            else -> "${peakHour}am"
                        }
                        "at $hourLabel (%02d:00)".format(peakHour)
                    } else ""

                    Text(
                        text = "Peak: $formattedPeak steps $timeString",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NeonYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (maxHourSteps == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hourly step breakdown available for $dateLabel",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val bottomPadding = 26.dp.toPx()
                    val chartHeight = height - bottomPadding
                    val scaleMax = maxHourSteps * 1.15f

                    val barCount = 24
                    val spacing = 2.dp.toPx()
                    val barWidth = (width - (spacing * (barCount + 1))) / barCount

                    hourlyStepsList.forEachIndexed { hour, steps ->
                        val barHeight = if (steps > 0) (chartHeight * (steps / scaleMax)).coerceAtLeast(6f) else 0f
                        val x = spacing + hour * (barWidth + spacing)
                        val y = chartHeight - barHeight

                        if (steps > 0) {
                            val isPeak = hour == peakHour
                            val gradientBrush = Brush.verticalGradient(
                                colors = if (isPeak) listOf(NeonYellow, NeonPink) else listOf(NeonCyan, NeonPurple),
                                startY = y,
                                endY = chartHeight
                            )

                            drawRoundRect(
                                brush = gradientBrush,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                        }

                        // Draw time labels at 0 (12am), 6 (6am), 12 (12pm), 18 (6pm), 23 (11pm)
                        if (hour == 0 || hour == 6 || hour == 12 || hour == 18 || hour == 23) {
                            val label = when (hour) {
                                0 -> "12am"
                                6 -> "6am"
                                12 -> "12pm"
                                18 -> "6pm"
                                else -> "11pm"
                            }
                            drawIntoCanvas { canvas ->
                                val textPaint = Paint().apply {
                                    color = NeonCyan.toArgb()
                                    textSize = 10.sp.toPx()
                                    textAlign = Paint.Align.CENTER
                                    isAntiAlias = true
                                }
                                canvas.nativeCanvas.drawText(
                                    label,
                                    x + barWidth / 2,
                                    height - 4.dp.toPx(),
                                    textPaint
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
