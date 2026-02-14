package com.example.dietapp.presentation.foodsearch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dietapp.domain.model.FoodItem
import com.example.dietapp.presentation.components.DietTopBar
import com.example.dietapp.presentation.components.LoadingIndicator
import com.example.dietapp.ui.theme.CaloriesColor
import com.example.dietapp.ui.theme.CarbsColor
import com.example.dietapp.ui.theme.FatColor
import com.example.dietapp.ui.theme.ProteinColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSearchScreen(
    onNavigateBack: () -> Unit = {},
    onFoodSelected: (FoodItem) -> Unit = {},
    viewModel: FoodSearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            DietTopBar(
                title = "Search Food",
                subtitle = "Find Indian foods & track nutrition",
                showBackButton = true,
                onBackClick = onNavigateBack,
                showNotification = false
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search dal, roti, sabzi...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearSearch() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    } else if (uiState.isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { /* handled by debounce */ })
            )

            // Category Chips
            if (uiState.categories.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    item {
                        FilterChip(selected = uiState.selectedCategory == null, onClick = { viewModel.onCategorySelected(null) }, label = { Text("All") })
                    }
                    items(uiState.categories) { category ->
                        val emoji = when (category.lowercase()) {
                            "dal" -> "🫘"; "roti" -> "🫓"; "rice" -> "🍚"; "sabzi" -> "🥬"; "curry" -> "🍛"
                            "snack" -> "🍿"; "beverage" -> "🍵"; "dessert" -> "🍮"; "salad" -> "🥗"; "breakfast" -> "🌅"
                            else -> "🍽️"
                        }
                        FilterChip(
                            selected = uiState.selectedCategory == category,
                            onClick = { viewModel.onCategorySelected(category) },
                            label = { Text("$emoji ${category.replaceFirstChar { it.uppercase() }}") }
                        )
                    }
                }
            }

            // Results
            if (uiState.isLoading) {
                LoadingIndicator()
            } else if (uiState.foodItems.isEmpty()) {
                EmptySearchState(query = uiState.searchQuery)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.foodItems, key = { it.id }) { food ->
                        FoodItemCard(food = food, onClick = { onFoodSelected(food) })
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodItemCard(food: FoodItem, onClick: () -> Unit) {
    // FoodItem model: name, nameHindi, calories, protein, carbs, fat, fiber, category, unit, isIndianFood
    val isVeg = food.category != "curry" || food.isIndianFood // approximate; real model uses isIndianFood

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    val emoji = when (food.category.lowercase()) {
                        "dal" -> "🫘"; "roti" -> "🫓"; "rice" -> "🍚"; "sabzi" -> "🥬"; "curry" -> "🍛"
                        "snack" -> "🍿"; "beverage" -> "🍵"; "dessert" -> "🍮"; "salad" -> "🥗"; "breakfast" -> "🌅"
                        else -> "🍽️"
                    }
                    Text(text = emoji, style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(food.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                food.nameHindi?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("${food.quantity} ${food.unit}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("${food.calories} kcal", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CaloriesColor)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MiniMacro("P", "${food.protein.toInt()}", ProteinColor)
                    MiniMacro("C", "${food.carbs.toInt()}", CarbsColor)
                    MiniMacro("F", "${food.fat.toInt()}", FatColor)
                }
            }
        }
    }
}

@Composable
private fun MiniMacro(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Text("$label:${value}g", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Medium)
}

@Composable
private fun EmptySearchState(query: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Text("🔍", style = MaterialTheme.typography.displayMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            if (query.isNotEmpty()) "No results for \"$query\"" else "Search for Indian foods",
            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text("Try searching for dal, paneer, roti, or poha", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
