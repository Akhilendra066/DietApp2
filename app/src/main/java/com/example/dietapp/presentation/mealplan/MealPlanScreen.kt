package com.example.dietapp.presentation.mealplan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dietapp.domain.model.MealPlan
import com.example.dietapp.presentation.components.DietPrimaryButton
import com.example.dietapp.presentation.components.DietTextButton
import com.example.dietapp.presentation.components.DietTopBar
import com.example.dietapp.presentation.components.LoadingIndicator
import com.example.dietapp.ui.theme.CaloriesColor
import com.example.dietapp.ui.theme.CarbsColor
import com.example.dietapp.ui.theme.FatColor
import com.example.dietapp.ui.theme.ProteinColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: MealPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    if (uiState.showAiDialog) {
        AiMealPlanDialog(
            onDismiss = { viewModel.hideAiDialog() },
            onGenerate = { pref, cal ->
                viewModel.generateAiMealPlan(dietaryPreference = pref, targetCalories = cal)
            }
        )
    }

    Scaffold(
        topBar = {
            DietTopBar(
                title = "Meal Plans",
                subtitle = "AI-powered Indian meal planning",
                showBackButton = true,
                onBackClick = onNavigateBack,
                showNotification = false
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showAiDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                icon = {
                    if (uiState.isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    }
                },
                text = {
                    Text(if (uiState.isGenerating) "Generating..." else "AI Generate")
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.filterFavoritesOnly,
                    onClick = { viewModel.toggleFavoritesFilter() },
                    label = { Text("Favorites") },
                    leadingIcon = {
                        Icon(
                            if (uiState.filterFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                FilterChip(
                    selected = false,
                    onClick = { /* TODO: AI filter */ },
                    label = { Text("AI Generated") },
                    leadingIcon = {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )
            }

            if (uiState.isLoading) {
                LoadingIndicator()
            } else if (uiState.mealPlans.isEmpty()) {
                EmptyMealPlanState(onGenerate = { viewModel.showAiDialog() })
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.mealPlans, key = { it.id }) { plan ->
                        MealPlanListCard(
                            mealPlan = plan,
                            onToggleFavorite = { viewModel.toggleFavorite(plan) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun MealPlanListCard(mealPlan: MealPlan, onToggleFavorite: () -> Unit) {
    val mealEmoji = when (mealPlan.mealType) {
        "breakfast" -> "🌅"; "lunch" -> "☀️"; "dinner" -> "🌙"; "snack" -> "🍎"; else -> "🍽️"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(text = mealEmoji, style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = mealPlan.name, style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        mealPlan.nameHindi?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (mealPlan.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (mealPlan.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                MacroLabel("${mealPlan.totalCalories}", "kcal", CaloriesColor)
                MacroLabel("${mealPlan.totalProtein.toInt()}g", "Protein", ProteinColor)
                MacroLabel("${mealPlan.totalCarbs.toInt()}g", "Carbs", CarbsColor)
                MacroLabel("${mealPlan.totalFat.toInt()}g", "Fat", FatColor)
            }
            AnimatedVisibility(visible = mealPlan.isAiGenerated) {
                Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Generated", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun MacroLabel(value: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyMealPlanState(onGenerate: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🥗", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No meal plans yet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Generate your first AI-powered Indian meal plan", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        DietPrimaryButton(text = "✨ Generate Meal Plan", onClick = onGenerate, modifier = Modifier.fillMaxWidth(0.7f))
    }
}

@Composable
private fun AiMealPlanDialog(onDismiss: () -> Unit, onGenerate: (String, Int) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate AI Meal Plan", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text("Our AI will create a personalized Indian meal plan based on your dietary preferences and calorie goals.", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("🍛 Includes breakfast, lunch, dinner & snacks", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = { DietPrimaryButton(text = "Generate", onClick = { onGenerate("vegetarian", 2000) }, modifier = Modifier.padding(horizontal = 8.dp)) },
        dismissButton = { DietTextButton(text = "Cancel", onClick = onDismiss) },
        shape = RoundedCornerShape(20.dp)
    )
}
