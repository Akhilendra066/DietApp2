package com.example.dietapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dietapp.ui.theme.CarbsColor
import com.example.dietapp.ui.theme.DietAppTheme
import com.example.dietapp.ui.theme.FatColor
import com.example.dietapp.ui.theme.ProteinColor

data class MacroData(
    val label: String,
    val grams: Float,
    val color: Color
)

@Composable
fun DonutMacroChart(
    protein: Float,
    carbs: Float,
    fat: Float,
    totalCalories: Int,
    size: Dp = 140.dp,
    strokeWidth: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    val total = protein + carbs + fat
    val segments = if (total > 0f) listOf(
        MacroData("Protein", protein, ProteinColor),
        MacroData("Carbs", carbs, CarbsColor),
        MacroData("Fat", fat, FatColor)
    ) else emptyList()

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animProgress.animateTo(1f, animationSpec = tween(1000))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(size)
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                val trackColor = Color.Gray.copy(alpha = 0.1f)

                // Background track
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = stroke
                )

                if (total > 0f) {
                    var startAngle = -90f
                    segments.forEach { seg ->
                        val sweep = (seg.grams / total) * 360f * animProgress.value
                        drawArc(
                            color = seg.color,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = stroke
                        )
                        startAngle += sweep
                    }
                }
            }

            // Center label
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$totalCalories",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "kcal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Legend
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            segments.forEach { seg ->
                LegendItem(label = seg.label, value = "${seg.grams.toInt()}g", color = seg.color)
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .padding(0.dp)
                .then(
                    Modifier.size(8.dp)
                )
        ) {
            Canvas(modifier = Modifier.size(8.dp)) {
                drawCircle(color = color)
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DonutMacroChartPreview() {
    DietAppTheme {
        DonutMacroChart(
            protein = 85f,
            carbs = 220f,
            fat = 65f,
            totalCalories = 1850,
            modifier = Modifier.padding(16.dp)
        )
    }
}
