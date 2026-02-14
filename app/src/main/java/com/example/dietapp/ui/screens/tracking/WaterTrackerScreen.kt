package com.example.dietapp.ui.screens.tracking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dietapp.ui.components.WaterTrackerCard
import com.example.dietapp.ui.theme.DietAppTheme
import com.example.dietapp.ui.theme.WaterColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterTrackerScreen(onBack: () -> Unit = {}) {
    var currentMl by remember { mutableIntStateOf(1200) }
    val goalMl = 3000
    val glassSize = 250

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Water Tracker", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
        ) {
            WaterTrackerCard(currentMl = currentMl, goalMl = goalMl, onAddGlass = { currentMl = (currentMl + glassSize).coerceAtMost(goalMl) })

            Spacer(modifier = Modifier.height(24.dp))

            Text("Quick Add", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(100 to "Small", 250 to "Glass", 500 to "Bottle").forEach { (ml, label) ->
                    OutlinedButton(
                        onClick = { currentMl = (currentMl + ml).coerceAtMost(goalMl) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${ml}ml", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = WaterColor)
                            Text(label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // History
            Text("Today's Log", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            listOf("8:00 AM" to 250, "10:30 AM" to 250, "1:00 PM" to 500, "3:30 PM" to 200).forEach { (time, ml) ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(time, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("💧 ${ml}ml", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = WaterColor)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WaterPreview() { DietAppTheme { WaterTrackerScreen() } }
