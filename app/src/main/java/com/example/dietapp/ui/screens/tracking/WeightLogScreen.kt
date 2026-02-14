package com.example.dietapp.ui.screens.tracking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dietapp.ui.components.WeightChartCard
import com.example.dietapp.ui.components.WeightEntry
import com.example.dietapp.ui.theme.DietAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightLogScreen(onBack: () -> Unit = {}) {
    var weightInput by remember { mutableStateOf("") }
    var entries by remember {
        mutableStateOf(
            listOf(
                WeightEntry("Mon", 78f), WeightEntry("Tue", 77.5f),
                WeightEntry("Wed", 77.8f), WeightEntry("Thu", 77.2f),
                WeightEntry("Fri", 76.9f), WeightEntry("Sat", 76.5f),
                WeightEntry("Sun", 76.2f)
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weight Log", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
        ) {
            // Current stats
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Current Weight", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row {
                        Text("76.2", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("kg", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 16.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("↓ 1.8 kg this week", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            WeightChartCard(entries = entries, currentWeight = 76.2f, goalWeight = 72f)

            Spacer(modifier = Modifier.height(20.dp))

            // Log entry
            Text("Log Today's Weight", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = weightInput,
                onValueChange = { weightInput = it },
                label = { Text("Weight (kg)") },
                leadingIcon = { Icon(Icons.Outlined.MonitorWeight, null) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { /* TODO: save weight */ }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp)) {
                Text("Save Entry", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WeightLogPreview() { DietAppTheme { WeightLogScreen() } }
