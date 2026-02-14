package com.example.dietapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dietapp.ui.theme.ChartGreen
import com.example.dietapp.ui.theme.DietAppTheme

data class WeightEntry(val label: String, val weightKg: Float)

@Composable
fun WeightChartCard(
    entries: List<WeightEntry>,
    currentWeight: Float,
    goalWeight: Float,
    modifier: Modifier = Modifier
) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(entries) {
        anim.snapTo(0f)
        anim.animateTo(1f, tween(1200))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Weight Trend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${currentWeight}kg → Goal: ${goalWeight}kg",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (entries.size >= 2) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    val minW = entries.minOf { it.weightKg } - 2f
                    val maxW = entries.maxOf { it.weightKg } + 2f
                    val range = maxW - minW
                    val stepX = size.width / (entries.size - 1)

                    val points = entries.mapIndexed { i, e ->
                        Offset(
                            x = i * stepX,
                            y = size.height - ((e.weightKg - minW) / range * size.height)
                        )
                    }

                    // Grid lines
                    for (i in 0..3) {
                        val y = size.height * i / 3f
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.1f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Goal line
                    val goalY = size.height - ((goalWeight - minW) / range * size.height)
                    drawLine(
                        color = ChartGreen.copy(alpha = 0.4f),
                        start = Offset(0f, goalY),
                        end = Offset(size.width, goalY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(8f, 8f)
                        )
                    )

                    // Animated path
                    val visibleCount = (points.size * anim.value).toInt().coerceAtLeast(2)
                    val visiblePoints = points.take(visibleCount)

                    val path = Path().apply {
                        moveTo(visiblePoints[0].x, visiblePoints[0].y)
                        for (i in 1 until visiblePoints.size) {
                            val cp1 = Offset(
                                (visiblePoints[i - 1].x + visiblePoints[i].x) / 2,
                                visiblePoints[i - 1].y
                            )
                            val cp2 = Offset(
                                (visiblePoints[i - 1].x + visiblePoints[i].x) / 2,
                                visiblePoints[i].y
                            )
                            cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, visiblePoints[i].x, visiblePoints[i].y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = ChartGreen,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Dots
                    visiblePoints.forEach { p ->
                        drawCircle(color = ChartGreen, radius = 5.dp.toPx(), center = p)
                        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = p)
                    }
                }
            }

            // X-axis labels
            if (entries.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WeightChartCardPreview() {
    DietAppTheme {
        WeightChartCard(
            entries = listOf(
                WeightEntry("Mon", 78f),
                WeightEntry("Tue", 77.5f),
                WeightEntry("Wed", 77.8f),
                WeightEntry("Thu", 77.2f),
                WeightEntry("Fri", 76.9f),
                WeightEntry("Sat", 76.5f),
                WeightEntry("Sun", 76.2f)
            ),
            currentWeight = 76.2f,
            goalWeight = 72f,
            modifier = Modifier.padding(16.dp)
        )
    }
}
