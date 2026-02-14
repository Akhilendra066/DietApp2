package com.example.dietapp.ui.screens.tracking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dietapp.ui.theme.*

private data class MealToLog(val id: String, val name: String, val mealType: String, val emoji: String, val calories: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealCompletionScreen(onBack: () -> Unit = {}) {
    val meals = remember {
        listOf(
            MealToLog("1", "Poha with Peanuts", "Breakfast", "🍚", 320),
            MealToLog("2", "Dal Tadka + Rice", "Lunch", "🍛", 520),
            MealToLog("3", "Masala Chai + Biscuit", "Snack", "☕", 120),
            MealToLog("4", "Paneer Bhurji + Roti", "Dinner", "🫓", 480)
        )
    }
    var completed by remember { mutableStateOf(setOf("1")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal Completion", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Mark meals you've eaten today", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
            }
            items(meals) { meal ->
                val done = completed.contains(meal.id)
                val typeColor = when (meal.mealType.lowercase()) {
                    "breakfast" -> BreakfastColor; "lunch" -> LunchColor; "dinner" -> DinnerColor; else -> SnackColor
                }
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = if (done) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(if (done) 0.dp else 1.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(meal.emoji, style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(meal.mealType.uppercase(), style = MaterialTheme.typography.labelSmall, color = typeColor, fontWeight = FontWeight.Bold)
                            Text(meal.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, textDecoration = if (done) TextDecoration.LineThrough else null)
                            Text("${meal.calories} kcal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { completed = if (done) completed - meal.id else completed + meal.id }) {
                            Icon(
                                if (done) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                null,
                                tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Completed", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("${completed.size}/${meals.size} meals", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MealCompletionPreview() { DietAppTheme { MealCompletionScreen() } }
