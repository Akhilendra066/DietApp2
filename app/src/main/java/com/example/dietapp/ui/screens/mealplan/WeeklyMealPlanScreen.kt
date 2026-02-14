package com.example.dietapp.ui.screens.mealplan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dietapp.ui.components.MealCard
import com.example.dietapp.ui.components.MealCardData
import com.example.dietapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyMealPlanScreen(
    onNavigateToMealDetails: (String) -> Unit = {},
    onNavigateToGroceryList: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: WeeklyMealPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal Plan", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = onNavigateToGroceryList) { Icon(Icons.Outlined.ShoppingCart, "Grocery List") }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::generatePlan,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Outlined.AutoAwesome, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate AI Plan")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Day selector
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                itemsIndexed(uiState.days) { index, day ->
                    val selected = index == uiState.selectedDay
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                            .clickable { viewModel.selectDay(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            day.day,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Meals for selected day
            val selectedDayPlan = uiState.days.getOrNull(uiState.selectedDay)
            if (selectedDayPlan != null) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(selectedDayPlan.meals) { meal ->
                        val typeColor = when (meal.mealType.lowercase()) {
                            "breakfast" -> BreakfastColor
                            "lunch" -> LunchColor
                            "dinner" -> DinnerColor
                            else -> SnackColor
                        }
                        MealCard(
                            meal = MealCardData(
                                name = meal.name,
                                mealType = meal.mealType,
                                calories = meal.calories,
                                proteinG = meal.proteinG,
                                carbsG = meal.carbsG,
                                fatG = meal.fatG,
                                imageEmoji = meal.emoji,
                                typeColor = typeColor
                            ),
                            onClick = { onNavigateToMealDetails(meal.id) },
                            onSwap = { /* TODO */ }
                        )
                    }

                    // Daily total
                    item {
                        val total = selectedDayPlan.meals.sumOf { it.calories }
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Daily Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text("$total kcal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WeeklyPreview() { DietAppTheme { WeeklyMealPlanScreen() } }
