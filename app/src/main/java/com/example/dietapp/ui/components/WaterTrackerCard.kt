package com.example.dietapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dietapp.ui.theme.DietAppTheme
import com.example.dietapp.ui.theme.WaterColor

@Composable
fun WaterTrackerCard(
    currentMl: Int,
    goalMl: Int,
    onAddGlass: () -> Unit,
    onClick :() -> Unit={},
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = if (goalMl > 0) (currentMl.toFloat() / goalMl).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(800),
        label = "water_progress"
    )

    Card(
        onClick=onClick,modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular progress
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(72.dp)
            ) {
                Canvas(modifier = Modifier.size(72.dp)) {
                    val stroke = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(
                        color = WaterColor.copy(alpha = 0.15f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = stroke
                    )
                    drawArc(
                        color = WaterColor,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = stroke
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.WaterDrop,
                    contentDescription = null,
                    tint = WaterColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Water Intake",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${currentMl}ml / ${goalMl}ml",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WaterColor,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Progress bar
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                ) {
                    drawRoundRect(
                        color = WaterColor.copy(alpha = 0.12f),
                        cornerRadius = CornerRadius(4.dp.toPx()),
                        size = Size(size.width, size.height)
                    )
                    drawRoundRect(
                        color = WaterColor,
                        cornerRadius = CornerRadius(4.dp.toPx()),
                        size = Size(size.width * progress, size.height)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            FilledIconButton(
                onClick = onAddGlass,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = WaterColor.copy(alpha = 0.12f),
                    contentColor = WaterColor
                )
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add glass")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WaterTrackerCardPreview() {
    DietAppTheme {
        WaterTrackerCard(
            currentMl = 1500,
            goalMl = 3000,
            onAddGlass = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
