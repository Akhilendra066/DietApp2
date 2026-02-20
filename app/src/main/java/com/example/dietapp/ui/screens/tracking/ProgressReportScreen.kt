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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dietapp.ui.theme.*

// ── Weekly average data model ────────────────────────────────
private data class WeeklyAvgItem(
    val name: String,
    val avg: Float,
    val goal: Float,
    val unit: String,
    val color: Color,
    val icon: ImageVector? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressReportScreen(
    onBack: () -> Unit = {},
    onNavigateToNutrientDetail: (String) -> Unit = {}
) {
    // Weekly averages for all fields from Today's Nutrition
    val macros = listOf(
        WeeklyAvgItem("Calories", 1737f, 2100f, "kcal", CaloriesColor, Icons.Outlined.LocalFireDepartment),
        WeeklyAvgItem("Protein", 87f, 120f, "g", ProteinColor, Icons.Outlined.Egg),
        WeeklyAvgItem("Carbs", 216f, 260f, "g", CarbsColor, Icons.Outlined.BakeryDining),
        WeeklyAvgItem("Fat", 59f, 70f, "g", FatColor, Icons.Outlined.WaterDrop),
        WeeklyAvgItem("Water", 2386f, 3000f, "ml", WaterColor, Icons.Outlined.WaterDrop)
    )

    val micros = listOf(
        WeeklyAvgItem("Vitamin A", 707f, 900f, "μg", Color(0xFFFF8A65)),
        WeeklyAvgItem("Vitamin C", 74f, 90f, "mg", Color(0xFFFFB74D)),
        WeeklyAvgItem("Vitamin D", 9f, 15f, "μg", Color(0xFFFFD54F)),
        WeeklyAvgItem("Vitamin B12", 1.9f, 2.4f, "μg", Color(0xFFE57373)),
        WeeklyAvgItem("Iron", 13f, 18f, "mg", Color(0xFF90A4AE)),
        WeeklyAvgItem("Calcium", 840f, 1000f, "mg", Color(0xFFE0E0E0)),
        WeeklyAvgItem("Sodium", 1957f, 2300f, "mg", Color(0xFF80CBC4)),
        WeeklyAvgItem("Potassium", 2900f, 3500f, "mg", Color(0xFFCE93D8)),
        WeeklyAvgItem("Zinc", 8f, 11f, "mg", Color(0xFFA5D6A7)),
        WeeklyAvgItem("Fiber", 20f, 28f, "g", FiberColor)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weekly Progress", fontWeight = FontWeight.SemiBold) },
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
            Text("This Week's Averages", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Tap any item to see daily breakdown", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            // ── Macronutrients & Water ───────────────────────
            macros.forEach { item ->
                AvgProgressCard(item) { onNavigateToNutrientDetail(item.name) }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Micronutrients ───────────────────────────────
            Text("Micronutrients", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    micros.forEachIndexed { index, item ->
                        MicroAvgRow(item) { onNavigateToNutrientDetail(item.name) }
                        if (index < micros.lastIndex) {
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
// Macro / Water Average Progress Card
// ═══════════════════════════════════════════════════════════════
@Composable
private fun AvgProgressCard(item: WeeklyAvgItem, onClick: () -> Unit) {
    val progress by animateFloatAsState(
        targetValue = (item.avg / item.goal).coerceIn(0f, 1f),
        animationSpec = tween(800),
        label = "${item.name}_avg_progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.icon != null) {
                Icon(item.icon, null, tint = item.color, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    Text(
                        "${fmtVal(item.avg)} / ${fmtVal(item.goal)} ${item.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Canvas(modifier = Modifier.fillMaxWidth().height(6.dp)) {
                    drawRoundRect(
                        color = item.color.copy(alpha = 0.12f),
                        cornerRadius = CornerRadius(3.dp.toPx()),
                        size = Size(size.width, size.height)
                    )
                    drawRoundRect(
                        color = item.color,
                        cornerRadius = CornerRadius(3.dp.toPx()),
                        size = Size(size.width * progress, size.height)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${(progress * 100).toInt()}% of daily goal",
                    style = MaterialTheme.typography.labelSmall,
                    color = item.color
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Micronutrient Average Row
// ═══════════════════════════════════════════════════════════════
@Composable
private fun MicroAvgRow(item: WeeklyAvgItem, onClick: () -> Unit) {
    val progress by animateFloatAsState(
        targetValue = (item.avg / item.goal).coerceIn(0f, 1f),
        animationSpec = tween(800),
        label = "${item.name}_micro_avg"
    )

    Column(modifier = Modifier.clickable { onClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${fmtVal(item.avg)} / ${fmtVal(item.goal)} ${item.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = item.color,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(5.dp)) {
            drawRoundRect(
                color = item.color.copy(alpha = 0.15f),
                cornerRadius = CornerRadius(3.dp.toPx()),
                size = Size(size.width, size.height)
            )
            drawRoundRect(
                color = item.color,
                cornerRadius = CornerRadius(3.dp.toPx()),
                size = Size(size.width * progress, size.height)
            )
        }
    }
}

private fun fmtVal(value: Float): String {
    return if (value == value.toLong().toFloat()) value.toLong().toString() else "%.1f".format(value)
}

@Preview(showBackground = true)
@Composable
private fun ProgressPreview() { DietAppTheme { ProgressReportScreen() } }
