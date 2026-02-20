package com.example.dietapp.ui.screens.tracking

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dietapp.ui.components.DonutMacroChart
import com.example.dietapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingHubScreen(
    onNavigateToNutrientDetail: (String) -> Unit = {}
) {

    // ── Sample data (display-only) ───────────────────────────
    val todayCalories = 1450
    val goalCalories = 2100
    val proteinG = 85f
    val carbsG = 180f
    val fatG = 52f
    val proteinGoal = 120f
    val carbsGoal = 260f
    val fatGoal = 70f

    val waterMl = 1200
    val waterGoalMl = 3000

    // Micronutrient data
    val micronutrients = listOf(
        MicronutrientData("Vitamin A", 650f, 900f, "μg", Color(0xFFFF8A65)),
        MicronutrientData("Vitamin C", 72f, 90f, "mg", Color(0xFFFFB74D)),
        MicronutrientData("Vitamin D", 8f, 15f, "μg", Color(0xFFFFD54F)),
        MicronutrientData("Vitamin B12", 1.8f, 2.4f, "μg", Color(0xFFE57373)),
        MicronutrientData("Iron", 12f, 18f, "mg", Color(0xFF90A4AE)),
        MicronutrientData("Calcium", 820f, 1000f, "mg", Color(0xFFE0E0E0)),
        MicronutrientData("Sodium", 1800f, 2300f, "mg", Color(0xFF80CBC4)),
        MicronutrientData("Potassium", 2800f, 3500f, "mg", Color(0xFFCE93D8)),
        MicronutrientData("Zinc", 7f, 11f, "mg", Color(0xFFA5D6A7)),
        MicronutrientData("Fiber", 18f, 28f, "g", FiberColor)
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Today's Nutrition", fontWeight = FontWeight.SemiBold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // SECTION 1: Calories (clickable)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            Card(
                modifier = Modifier.clickable { onNavigateToNutrientDetail("Calories") },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Today's Nutrition", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("$todayCalories / $goalCalories kcal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    DonutMacroChart(
                        protein = proteinG,
                        carbs = carbsG,
                        fat = fatG,
                        totalCalories = todayCalories
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // SECTION 2: Macronutrients (each clickable)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            Text("Macronutrients", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            MacroProgressRow("Protein", proteinG, proteinGoal, ProteinColor, Icons.Outlined.Egg) { onNavigateToNutrientDetail("Protein") }
            Spacer(modifier = Modifier.height(8.dp))
            MacroProgressRow("Carbs", carbsG, carbsGoal, CarbsColor, Icons.Outlined.BakeryDining) { onNavigateToNutrientDetail("Carbs") }
            Spacer(modifier = Modifier.height(8.dp))
            MacroProgressRow("Fat", fatG, fatGoal, FatColor, Icons.Outlined.WaterDrop) { onNavigateToNutrientDetail("Fat") }

            Spacer(modifier = Modifier.height(20.dp))

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // SECTION 3: Water (clickable)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            WaterDisplayCard(currentMl = waterMl, goalMl = waterGoalMl) { onNavigateToNutrientDetail("Water") }

            Spacer(modifier = Modifier.height(20.dp))

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // SECTION 4: Micronutrients (each row clickable)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            Text("Micronutrients", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    micronutrients.forEachIndexed { index, nutrient ->
                        MicronutrientRow(nutrient) { onNavigateToNutrientDetail(nutrient.name) }
                        if (index < micronutrients.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 10.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Data class for micronutrients
// ═══════════════════════════════════════════════════════════════
private data class MicronutrientData(
    val name: String,
    val current: Float,
    val goal: Float,
    val unit: String,
    val color: Color
)

// ═══════════════════════════════════════════════════════════════
// Micronutrient Row (clickable)
// ═══════════════════════════════════════════════════════════════
@Composable
private fun MicronutrientRow(data: MicronutrientData, onClick: () -> Unit = {}) {
    val progress by animateFloatAsState(
        targetValue = (data.current / data.goal).coerceIn(0f, 1f),
        animationSpec = tween(800),
        label = "${data.name}_progress"
    )

    Column(modifier = Modifier.clickable { onClick() }) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(data.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                "${formatValue(data.current)} / ${formatValue(data.goal)} ${data.unit}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(6.dp)) {
            drawRoundRect(
                color = data.color.copy(alpha = 0.15f),
                cornerRadius = CornerRadius(3.dp.toPx()),
                size = Size(size.width, size.height)
            )
            drawRoundRect(
                color = data.color,
                cornerRadius = CornerRadius(3.dp.toPx()),
                size = Size(size.width * progress, size.height)
            )
        }
    }
}

private fun formatValue(value: Float): String {
    return if (value == value.toLong().toFloat()) value.toLong().toString() else "%.1f".format(value)
}

// ═══════════════════════════════════════════════════════════════
// Macro Progress Row (clickable)
// ═══════════════════════════════════════════════════════════════
@Composable
private fun MacroProgressRow(
    label: String,
    current: Float,
    goal: Float,
    color: Color,
    icon: ImageVector,
    onClick: () -> Unit = {}
) {
    val progress by animateFloatAsState(
        targetValue = (current / goal).coerceIn(0f, 1f),
        animationSpec = tween(800),
        label = "${label}_progress"
    )

    Card(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    Text("${current.toInt()}g / ${goal.toInt()}g", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Canvas(modifier = Modifier.fillMaxWidth().height(6.dp)) {
                    drawRoundRect(
                        color = color.copy(alpha = 0.12f),
                        cornerRadius = CornerRadius(3.dp.toPx()),
                        size = Size(size.width, size.height)
                    )
                    drawRoundRect(
                        color = color,
                        cornerRadius = CornerRadius(3.dp.toPx()),
                        size = Size(size.width * progress, size.height)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Water Display Card (clickable, no add button)
// ═══════════════════════════════════════════════════════════════
@Composable
private fun WaterDisplayCard(currentMl: Int, goalMl: Int, onClick: () -> Unit = {}) {
    val progress by animateFloatAsState(
        targetValue = if (goalMl > 0) (currentMl.toFloat() / goalMl).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(800),
        label = "water_display_progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(72.dp)
            ) {
                Canvas(modifier = Modifier.size(72.dp)) {
                    val stroke = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(color = WaterColor.copy(alpha = 0.15f), startAngle = -90f, sweepAngle = 360f, useCenter = false, style = stroke)
                    drawArc(color = WaterColor, startAngle = -90f, sweepAngle = 360f * progress, useCenter = false, style = stroke)
                }
                Icon(Icons.Outlined.WaterDrop, null, tint = WaterColor, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("Water Intake", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${currentMl}ml / ${goalMl}ml", style = MaterialTheme.typography.bodyMedium, color = WaterColor, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
                    drawRoundRect(color = WaterColor.copy(alpha = 0.12f), cornerRadius = CornerRadius(4.dp.toPx()), size = Size(size.width, size.height))
                    drawRoundRect(color = WaterColor, cornerRadius = CornerRadius(4.dp.toPx()), size = Size(size.width * progress, size.height))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TrackingHubPreview() { DietAppTheme { TrackingHubScreen() } }
