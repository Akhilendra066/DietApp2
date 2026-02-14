package com.example.dietapp.ui.screens.mealplan

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
import com.example.dietapp.ui.theme.DietAppTheme

private data class GroceryItem(val name: String, val quantity: String, val category: String)

private val sampleGrocery = listOf(
    GroceryItem("Toor Dal", "500g", "Pulses"), GroceryItem("Basmati Rice", "1 kg", "Grains"),
    GroceryItem("Onion", "500g", "Vegetables"), GroceryItem("Tomato", "500g", "Vegetables"),
    GroceryItem("Paneer", "200g", "Dairy"), GroceryItem("Ghee", "100ml", "Dairy"),
    GroceryItem("Coriander", "1 bunch", "Herbs"), GroceryItem("Cucumber", "2 pcs", "Vegetables"),
    GroceryItem("Poha", "250g", "Grains"), GroceryItem("Peanuts", "100g", "Nuts"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryListScreen(onBack: () -> Unit = {}) {
    var checkedItems by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grocery List", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        val grouped = sampleGrocery.groupBy { it.category }

        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            grouped.forEach { (category, items) ->
                item {
                    Text(
                        category,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(items) { item ->
                    val checked = checkedItems.contains(item.name)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (checked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(if (checked) 0.dp else 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                checkedItems = if (checked) checkedItems - item.name else checkedItems + item.name
                            }, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    if (checked) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                    null,
                                    tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                item.name,
                                style = MaterialTheme.typography.bodyLarge,
                                textDecoration = if (checked) TextDecoration.LineThrough else null,
                                modifier = Modifier.weight(1f)
                            )
                            Text(item.quantity, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Estimated Cost", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("≈ ₹650", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GroceryPreview() { DietAppTheme { GroceryListScreen() } }
