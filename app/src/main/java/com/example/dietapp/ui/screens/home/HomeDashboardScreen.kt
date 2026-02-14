package com.example.dietapp.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dietapp.ui.components.*
import com.example.dietapp.ui.theme.*

@Composable
fun HomeDashboardScreen(
    onNavigateToMealPlan: () -> Unit = {},
    onNavigateToWeightLog: () -> Unit = {},
    onNavigateToWaterTracker: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToProgress: () -> Unit = {},
    viewModel: HomeDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Greeting
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(uiState.greeting, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${uiState.userName} 👋", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🔥", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${uiState.streakDays} days", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Macro donut
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Today's Nutrition", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("${uiState.todayCalories} / ${uiState.goalCalories} kcal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    DonutMacroChart(
                        protein = uiState.proteinG,
                        carbs = uiState.carbsG,
                        fat = uiState.fatG,
                        totalCalories = uiState.todayCalories
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RoundedIconButton(icon = Icons.Outlined.MenuBook, label = "Meal Plan", color = Primary, onClick = onNavigateToMealPlan)
                RoundedIconButton(icon = Icons.Outlined.MonitorWeight, label = "Weight", color = ChartBlue, onClick = onNavigateToWeightLog)
                RoundedIconButton(icon = Icons.Outlined.WaterDrop, label = "Water", color = WaterColor, onClick = onNavigateToWaterTracker)
                RoundedIconButton(icon = Icons.Outlined.BarChart, label = "Progress", color = ChartOrange, onClick = onNavigateToProgress)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Water tracker
            WaterTrackerCard(
                currentMl = uiState.waterMl,
                goalMl = uiState.waterGoalMl,
                onAddGlass = viewModel::addWaterGlass
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Weight card
            WeightChartCard(
                entries = listOf(
                    WeightEntry("Mon", 78f), WeightEntry("Tue", 77.5f),
                    WeightEntry("Wed", 77.8f), WeightEntry("Thu", 77.2f),
                    WeightEntry("Fri", 76.9f), WeightEntry("Sat", 76.5f),
                    WeightEntry("Sun", 76.2f)
                ),
                currentWeight = uiState.currentWeightKg,
                goalWeight = uiState.goalWeightKg
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Calorie progress
            ProgressCard(
                title = "Calories",
                value = "${uiState.todayCalories}",
                subtitle = "/ ${uiState.goalCalories} kcal",
                progress = uiState.todayCalories.toFloat() / uiState.goalCalories,
                color = CaloriesColor,
                icon = Icons.Outlined.LocalFireDepartment
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomePreview() { DietAppTheme { HomeDashboardScreen() } }
