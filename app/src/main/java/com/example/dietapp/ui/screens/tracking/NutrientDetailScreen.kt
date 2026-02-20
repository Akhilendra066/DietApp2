package com.example.dietapp.ui.screens.tracking

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dietapp.ui.theme.*

// ── Data models ──────────────────────────────────────────────
data class DailyIntake(val day: String, val value: Float)
data class ConsumptionEntry(val time: String, val date: String, val source: String, val amount: Float, val unit: String)

// ── Static sample data provider ──────────────────────────────
private fun getSampleDataForNutrient(name: String): Triple<List<DailyIntake>, List<ConsumptionEntry>, Pair<Float, String>> {
    return when (name) {
        "Calories" -> Triple(
            listOf(DailyIntake("Mon", 1850f), DailyIntake("Tue", 1720f), DailyIntake("Wed", 2010f), DailyIntake("Thu", 1680f), DailyIntake("Fri", 1900f), DailyIntake("Sat", 1550f), DailyIntake("Sun", 1450f)),
            listOf(
                ConsumptionEntry("8:15 AM", "Sun, 16 Feb", "Oats with Banana", 320f, "kcal"),
                ConsumptionEntry("10:30 AM", "Sun, 16 Feb", "Mixed Nuts (30g)", 180f, "kcal"),
                ConsumptionEntry("1:00 PM", "Sun, 16 Feb", "Dal Rice with Sabzi", 520f, "kcal"),
                ConsumptionEntry("4:00 PM", "Sun, 16 Feb", "Green Tea + Biscuits", 80f, "kcal"),
                ConsumptionEntry("8:30 PM", "Sun, 16 Feb", "Chapati with Paneer", 350f, "kcal")
            ),
            Pair(2100f, "kcal")
        )
        "Protein" -> Triple(
            listOf(DailyIntake("Mon", 92f), DailyIntake("Tue", 78f), DailyIntake("Wed", 105f), DailyIntake("Thu", 88f), DailyIntake("Fri", 95f), DailyIntake("Sat", 70f), DailyIntake("Sun", 85f)),
            listOf(
                ConsumptionEntry("8:15 AM", "Sun, 16 Feb", "Oats with Milk", 12f, "g"),
                ConsumptionEntry("10:30 AM", "Sun, 16 Feb", "Boiled Eggs (2)", 14f, "g"),
                ConsumptionEntry("1:00 PM", "Sun, 16 Feb", "Chicken Curry", 32f, "g"),
                ConsumptionEntry("4:00 PM", "Sun, 16 Feb", "Greek Yogurt", 10f, "g"),
                ConsumptionEntry("8:30 PM", "Sun, 16 Feb", "Paneer Tikka", 17f, "g")
            ),
            Pair(120f, "g")
        )
        "Carbs" -> Triple(
            listOf(DailyIntake("Mon", 240f), DailyIntake("Tue", 210f), DailyIntake("Wed", 260f), DailyIntake("Thu", 195f), DailyIntake("Fri", 230f), DailyIntake("Sat", 200f), DailyIntake("Sun", 180f)),
            listOf(
                ConsumptionEntry("8:15 AM", "Sun, 16 Feb", "Oats with Banana", 52f, "g"),
                ConsumptionEntry("1:00 PM", "Sun, 16 Feb", "Rice (1.5 cups)", 68f, "g"),
                ConsumptionEntry("4:00 PM", "Sun, 16 Feb", "Biscuits (4)", 22f, "g"),
                ConsumptionEntry("8:30 PM", "Sun, 16 Feb", "Chapati (3)", 38f, "g")
            ),
            Pair(260f, "g")
        )
        "Fat" -> Triple(
            listOf(DailyIntake("Mon", 62f), DailyIntake("Tue", 55f), DailyIntake("Wed", 70f), DailyIntake("Thu", 48f), DailyIntake("Fri", 65f), DailyIntake("Sat", 58f), DailyIntake("Sun", 52f)),
            listOf(
                ConsumptionEntry("8:15 AM", "Sun, 16 Feb", "Ghee in Oats", 8f, "g"),
                ConsumptionEntry("10:30 AM", "Sun, 16 Feb", "Mixed Nuts", 14f, "g"),
                ConsumptionEntry("1:00 PM", "Sun, 16 Feb", "Cooking Oil", 12f, "g"),
                ConsumptionEntry("8:30 PM", "Sun, 16 Feb", "Paneer", 18f, "g")
            ),
            Pair(70f, "g")
        )
        "Water" -> Triple(
            listOf(DailyIntake("Mon", 2400f), DailyIntake("Tue", 2800f), DailyIntake("Wed", 2100f), DailyIntake("Thu", 3000f), DailyIntake("Fri", 2600f), DailyIntake("Sat", 1800f), DailyIntake("Sun", 1200f)),
            listOf(
                ConsumptionEntry("7:00 AM", "Sun, 16 Feb", "Morning Glass", 250f, "ml"),
                ConsumptionEntry("9:30 AM", "Sun, 16 Feb", "Post Workout", 500f, "ml"),
                ConsumptionEntry("12:00 PM", "Sun, 16 Feb", "Before Lunch", 200f, "ml"),
                ConsumptionEntry("3:00 PM", "Sun, 16 Feb", "Afternoon", 250f, "ml")
            ),
            Pair(3000f, "ml")
        )
        "Vitamin A" -> Triple(
            listOf(DailyIntake("Mon", 780f), DailyIntake("Tue", 600f), DailyIntake("Wed", 850f), DailyIntake("Thu", 700f), DailyIntake("Fri", 620f), DailyIntake("Sat", 750f), DailyIntake("Sun", 650f)),
            listOf(
                ConsumptionEntry("8:15 AM", "Sun, 16 Feb", "Carrot Juice", 450f, "μg"),
                ConsumptionEntry("1:00 PM", "Sun, 16 Feb", "Spinach Sabzi", 140f, "μg"),
                ConsumptionEntry("8:30 PM", "Sun, 16 Feb", "Sweet Potato", 60f, "μg")
            ),
            Pair(900f, "μg")
        )
        "Vitamin C" -> Triple(
            listOf(DailyIntake("Mon", 85f), DailyIntake("Tue", 60f), DailyIntake("Wed", 95f), DailyIntake("Thu", 70f), DailyIntake("Fri", 80f), DailyIntake("Sat", 55f), DailyIntake("Sun", 72f)),
            listOf(
                ConsumptionEntry("8:15 AM", "Sun, 16 Feb", "Orange Juice", 42f, "mg"),
                ConsumptionEntry("1:00 PM", "Sun, 16 Feb", "Lemon Salad Dressing", 15f, "mg"),
                ConsumptionEntry("4:00 PM", "Sun, 16 Feb", "Amla", 15f, "mg")
            ),
            Pair(90f, "mg")
        )
        "Sodium" -> Triple(
            listOf(DailyIntake("Mon", 2100f), DailyIntake("Tue", 1950f), DailyIntake("Wed", 2300f), DailyIntake("Thu", 1800f), DailyIntake("Fri", 2050f), DailyIntake("Sat", 1700f), DailyIntake("Sun", 1800f)),
            listOf(
                ConsumptionEntry("8:15 AM", "Sun, 16 Feb", "Oats with Salt", 300f, "mg"),
                ConsumptionEntry("1:00 PM", "Sun, 16 Feb", "Dal + Rice", 650f, "mg"),
                ConsumptionEntry("4:00 PM", "Sun, 16 Feb", "Biscuits", 200f, "mg"),
                ConsumptionEntry("8:30 PM", "Sun, 16 Feb", "Paneer Curry", 650f, "mg")
            ),
            Pair(2300f, "mg")
        )
        else -> Triple(
            listOf(DailyIntake("Mon", 10f), DailyIntake("Tue", 8f), DailyIntake("Wed", 12f), DailyIntake("Thu", 9f), DailyIntake("Fri", 11f), DailyIntake("Sat", 7f), DailyIntake("Sun", 8f)),
            listOf(
                ConsumptionEntry("8:15 AM", "Sun, 16 Feb", "Breakfast", 3f, ""),
                ConsumptionEntry("1:00 PM", "Sun, 16 Feb", "Lunch", 4f, ""),
                ConsumptionEntry("8:30 PM", "Sun, 16 Feb", "Dinner", 3f, "")
            ),
            Pair(15f, "")
        )
    }
}

private fun getColorForNutrient(name: String): Color {
    return when (name) {
        "Calories" -> CaloriesColor
        "Protein" -> ProteinColor
        "Carbs" -> CarbsColor
        "Fat" -> FatColor
        "Water" -> WaterColor
        "Fiber" -> FiberColor
        "Vitamin A" -> Color(0xFFFF8A65)
        "Vitamin C" -> Color(0xFFFFB74D)
        "Vitamin D" -> Color(0xFFFFD54F)
        "Vitamin B12" -> Color(0xFFE57373)
        "Iron" -> Color(0xFF90A4AE)
        "Calcium" -> Color(0xFFE0E0E0)
        "Sodium" -> Color(0xFF80CBC4)
        "Potassium" -> Color(0xFFCE93D8)
        "Zinc" -> Color(0xFFA5D6A7)
        else -> Primary
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutrientDetailScreen(
    nutrientName: String,
    onBack: () -> Unit = {}
) {
    val (dailyIntakes, consumptionLog, goalInfo) = getSampleDataForNutrient(nutrientName)
    val (goal, unit) = goalInfo
    val color = getColorForNutrient(nutrientName)
    val weeklyAvg = dailyIntakes.map { it.value }.average().toFloat()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(nutrientName, fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // ── Summary Card ─────────────────────────────────
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Today", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            formatDetailValue(dailyIntakes.last().value),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "/ ${formatDetailValue(goal)} $unit",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Weekly avg: ${formatDetailValue(weeklyAvg)} $unit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 7-Day Chart ──────────────────────────────────
            Text("Last 7 Days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    WeeklyBarChart(
                        data = dailyIntakes,
                        goal = goal,
                        color = color,
                        modifier = Modifier.fillMaxWidth().height(180.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        ChartLegendItem("Goal", formatDetailValue(goal), unit, color.copy(alpha = 0.3f))
                        ChartLegendItem("Average", formatDetailValue(weeklyAvg), unit, color)
                        ChartLegendItem("Highest", formatDetailValue(dailyIntakes.maxOf { it.value }), unit, color)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Consumption Log ──────────────────────────────
            Text("Consumption Log", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    consumptionLog.forEachIndexed { index, entry ->
                        ConsumptionLogRow(entry, color)
                        if (index < consumptionLog.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
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
// Bar Chart
// ═══════════════════════════════════════════════════════════════
@Composable
private fun WeeklyBarChart(
    data: List<DailyIntake>,
    goal: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { animProgress.animateTo(1f, animationSpec = tween(1000)) }

    val maxValue = maxOf(data.maxOf { it.value }, goal) * 1.15f
    val surfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val barCount = data.size
            val totalSpacing = size.width * 0.4f
            val barWidth = (size.width - totalSpacing) / barCount
            val spacing = totalSpacing / (barCount + 1)

            // Goal line
            val goalY = size.height * (1 - goal / maxValue)
            drawLine(
                color = color.copy(alpha = 0.3f),
                start = Offset(0f, goalY),
                end = Offset(size.width, goalY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )

            // Bars
            data.forEachIndexed { index, intake ->
                val barHeight = (intake.value / maxValue) * size.height * animProgress.value
                val x = spacing + index * (barWidth + spacing)
                val barColor = if (intake.value >= goal) color else color.copy(alpha = 0.6f)

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Day labels
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            data.forEach { intake ->
                Text(
                    intake.day,
                    style = MaterialTheme.typography.labelSmall,
                    color = surfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ChartLegendItem(label: String, value: String, unit: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size(8.dp)) { drawCircle(color = color) }
        Spacer(modifier = Modifier.height(4.dp))
        Text("$value $unit", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ═══════════════════════════════════════════════════════════════
// Consumption Log Row
// ═══════════════════════════════════════════════════════════════
@Composable
private fun ConsumptionLogRow(entry: ConsumptionEntry, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.AccessTime, null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.source, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                "${entry.time} · ${entry.date}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            "${formatDetailValue(entry.amount)} ${entry.unit}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

private fun formatDetailValue(value: Float): String {
    return if (value == value.toLong().toFloat()) value.toLong().toString() else "%.1f".format(value)
}

@Preview(showBackground = true)
@Composable
private fun NutrientDetailPreview() { DietAppTheme { NutrientDetailScreen("Protein") } }
