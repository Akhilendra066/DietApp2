package com.example.dietapp.presentation.dietlog

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dietapp.presentation.components.*
import com.example.dietapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietLogScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: DietLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DietLogEvent.LogSuccess -> snackbarHostState.showSnackbar("Meal logged!")
                is DietLogEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    if (uiState.showLogDialog) {
        LogMealDialog(
            isLogging = uiState.isLogging,
            onDismiss = { viewModel.hideLogDialog() },
            onLog = { name, type, cal, p, c, f, fi ->
                viewModel.logMeal(name, type, cal, p, c, f, fi)
            }
        )
    }

    Scaffold(
        topBar = { DietTopBar(title = "Diet Log", subtitle = uiState.selectedDate, showBackButton = true, onBackClick = onNavigateBack, showNotification = false) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showLogDialog() }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "Log Meal")
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingIndicator(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    uiState.nutritionSummary?.let { s ->
                        CalorieProgressCard(s.totalCalories, s.targetCalories, s.calorieProgress)
                    }
                }
                item {
                    uiState.nutritionSummary?.let { s ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            NutrientCard("Protein", "${s.totalProtein.toInt()}g", ProteinColor, Modifier.weight(1f))
                            NutrientCard("Carbs", "${s.totalCarbs.toInt()}g", CarbsColor, Modifier.weight(1f))
                            NutrientCard("Fat", "${s.totalFat.toInt()}g", FatColor, Modifier.weight(1f))
                            NutrientCard("Fiber", "${s.totalFiber.toInt()}g", FiberColor, Modifier.weight(1f))
                        }
                    }
                }
                val meals = listOf("breakfast" to "🌅", "lunch" to "☀️", "dinner" to "🌙", "snack" to "🍎")
                meals.forEach { (type, emoji) ->
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(emoji, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.width(8.dp))
                            Text(type.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    item {
                        Card(
                            onClick = { viewModel.showLogDialog() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text("Add ${type.replaceFirstChar { it.uppercase() }}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun CalorieProgressCard(consumed: Int, target: Int, progress: Float) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.fillMaxWidth().padding(20.dp).animateContentSize()) {
            Text("Today's Calories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("$consumed", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = CaloriesColor); Text("consumed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Column(horizontalAlignment = Alignment.End) { Text("$target", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)); Text("target", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)), color = if (progress > 1f) MaterialTheme.colorScheme.error else CaloriesColor, trackColor = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(Modifier.height(4.dp))
            val rem = target - consumed
            Text(if (rem > 0) "$rem kcal remaining" else "Goal reached! 🎉", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun NutrientCard(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogMealDialog(isLogging: Boolean, onDismiss: () -> Unit, onLog: (String, String, Int, Float, Float, Float, Float) -> Unit) {
    var foodName by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableStateOf("breakfast") }
    var expanded by remember { mutableStateOf(false) }
    val mealTypes = listOf("breakfast", "lunch", "dinner", "snack")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log a Meal 🍽️", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(foodName, { foodName = it }, label = { Text("Food Name") }, placeholder = { Text("e.g. Dal Tadka") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
                    OutlinedTextField(selectedMealType.replaceFirstChar { it.uppercase() }, {}, readOnly = true, label = { Text("Meal Type") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable), shape = RoundedCornerShape(12.dp))
                    ExposedDropdownMenu(expanded, { expanded = false }) { mealTypes.forEach { t -> DropdownMenuItem(text = { Text(t.replaceFirstChar { it.uppercase() }) }, onClick = { selectedMealType = t; expanded = false }) } }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(calories, { calories = it }, label = { Text("Calories") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(protein, { protein = it }, label = { Text("Protein") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, shape = RoundedCornerShape(12.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(carbs, { carbs = it }, label = { Text("Carbs") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(fat, { fat = it }, label = { Text("Fat") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, shape = RoundedCornerShape(12.dp))
                }
            }
        },
        confirmButton = {
            DietPrimaryButton(text = "Log Meal", onClick = { onLog(foodName, selectedMealType, calories.toIntOrNull() ?: 0, protein.toFloatOrNull() ?: 0f, carbs.toFloatOrNull() ?: 0f, fat.toFloatOrNull() ?: 0f, 0f) }, isLoading = isLogging, enabled = foodName.isNotBlank() && calories.isNotBlank(), modifier = Modifier.padding(horizontal = 8.dp))
        },
        dismissButton = { DietTextButton(text = "Cancel", onClick = onDismiss) },
        shape = RoundedCornerShape(20.dp)
    )
}
