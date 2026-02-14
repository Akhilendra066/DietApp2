package com.example.dietapp.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dietapp.ui.components.GradientBackground
import com.example.dietapp.ui.theme.DietAppTheme

private val dietTypes = listOf("Vegetarian", "Non-Vegetarian", "Vegan", "Eggetarian", "Pescatarian", "Jain")
private val commonAllergies = listOf("Gluten", "Dairy", "Nuts", "Soy", "Eggs", "Shellfish", "Lactose")
private val cuisines = listOf("North Indian", "South Indian", "Bengali", "Gujarati", "Punjabi", "Continental", "Pan-Asian")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DietPreferenceScreen(
    onNext: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    GradientBackground() {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Step 3 of 5", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Diet Preferences", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Help us plan meals you'll love.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(28.dp))

            Text("Diet Type", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                dietTypes.forEach { d ->
                    FilterChip(selected = uiState.dietType == d, onClick = { viewModel.onDietSelect(d) }, label = { Text(d) }, shape = RoundedCornerShape(12.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Text("Allergies (if any)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                commonAllergies.forEach { a ->
                    FilterChip(selected = uiState.allergies.contains(a), onClick = { viewModel.toggleAllergy(a) }, label = { Text(a) }, shape = RoundedCornerShape(12.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Text("Cuisine Preferences", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                cuisines.forEach { c ->
                    FilterChip(selected = uiState.cuisinePreferences.contains(c), onClick = { viewModel.toggleCuisine(c) }, label = { Text(c) }, shape = RoundedCornerShape(12.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                Text("Continue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DietPrefPreview() { DietAppTheme { DietPreferenceScreen() } }
