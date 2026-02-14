package com.example.dietapp.ui.screens.tracking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dietapp.ui.components.DonutMacroChart
import com.example.dietapp.ui.components.ProgressCard
import com.example.dietapp.ui.components.WeightChartCard
import com.example.dietapp.ui.components.WeightEntry
import com.example.dietapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressReportScreen(onBack: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress Report", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Text("This Week", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            ProgressCard(title = "Avg Calories", value = "1,680", subtitle = "/ 2,000 kcal", progress = 0.84f, color = CaloriesColor, icon = Icons.Outlined.LocalFireDepartment)
            Spacer(modifier = Modifier.height(12.dp))
            ProgressCard(title = "Water Intake", value = "2.1L", subtitle = "/ 3L avg", progress = 0.7f, color = WaterColor, icon = Icons.Outlined.WaterDrop)
            Spacer(modifier = Modifier.height(12.dp))
            ProgressCard(title = "Meals Logged", value = "24", subtitle = "/ 28", progress = 0.86f, color = Primary, icon = Icons.Outlined.Restaurant)
            Spacer(modifier = Modifier.height(12.dp))
            ProgressCard(title = "Weight Change", value = "-1.8", subtitle = "kg this week", progress = 0.6f, color = ChartBlue, icon = Icons.Outlined.MonitorWeight)

            Spacer(modifier = Modifier.height(24.dp))

            Text("Macro Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            DonutMacroChart(protein = 85f, carbs = 220f, fat = 65f, totalCalories = 1680)

            Spacer(modifier = Modifier.height(24.dp))

            WeightChartCard(
                entries = listOf(WeightEntry("Mon", 78f), WeightEntry("Tue", 77.5f), WeightEntry("Wed", 77.8f), WeightEntry("Thu", 77.2f), WeightEntry("Fri", 76.9f), WeightEntry("Sat", 76.5f), WeightEntry("Sun", 76.2f)),
                currentWeight = 76.2f,
                goalWeight = 72f
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProgressPreview() { DietAppTheme { ProgressReportScreen() } }
