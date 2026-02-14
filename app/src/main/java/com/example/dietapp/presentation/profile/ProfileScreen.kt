package com.example.dietapp.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dietapp.domain.model.User
import com.example.dietapp.presentation.components.*
import com.example.dietapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onNavigateBack: () -> Unit = {}, viewModel: ProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.ProfileSaved -> snackbarHostState.showSnackbar("Profile saved!")
                is ProfileEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = { DietTopBar(title = "Profile", subtitle = if (uiState.isEditing) "Edit Mode" else null, showBackButton = true, onBackClick = onNavigateBack, showNotification = false) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingIndicator(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Header
                item {
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(80.dp)) {
                                Icon(Icons.Default.Person, null, Modifier.padding(20.dp).size(40.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Spacer(Modifier.height(12.dp))
                            if (uiState.isEditing) {
                                OutlinedTextField(uiState.editName, { viewModel.onNameChanged(it) }, label = { Text("Name") }, singleLine = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                            } else {
                                Text(uiState.editName.ifEmpty { "Set up your profile" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(8.dp))
                            IconButton(onClick = { viewModel.toggleEditing() }) {
                                Icon(Icons.Default.Edit, if (uiState.isEditing) "Cancel" else "Edit", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                // BMI & TDEE
                item {
                    uiState.user?.let { u ->
                        val bmi = User.calculateBmi(u.heightCm, u.weightKg)
                        val tdee = User.calculateDailyCalories(u.weightKg, u.heightCm, u.age, u.gender, u.activityLevel, u.goalType)
                        val bmiCat = User.getBmiCategory(bmi)
                        val bmiColor = when { bmi < 18.5f -> CarbsColor; bmi < 25f -> MaterialTheme.colorScheme.tertiary; bmi < 30f -> CaloriesColor; else -> MaterialTheme.colorScheme.error }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = bmiColor.copy(alpha = 0.1f))) {
                                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("BMI", style = MaterialTheme.typography.labelMedium); Text("%.1f".format(bmi), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = bmiColor); Text(bmiCat, style = MaterialTheme.typography.labelSmall, color = bmiColor)
                                }
                            }
                            Card(Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CaloriesColor.copy(alpha = 0.1f))) {
                                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("TDEE", style = MaterialTheme.typography.labelMedium); Text("$tdee", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = CaloriesColor); Text("kcal/day", style = MaterialTheme.typography.labelSmall, color = CaloriesColor)
                                }
                            }
                            Card(Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ProteinColor.copy(alpha = 0.1f))) {
                                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Target", style = MaterialTheme.typography.labelMedium); Text("${u.targetCalories}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = ProteinColor); Text("kcal/day", style = MaterialTheme.typography.labelSmall, color = ProteinColor)
                                }
                            }
                        }
                    }
                }
                // Body & Diet Info
                item { Text("Body Measurements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                item {
                    if (uiState.isEditing) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            DropdownField("Gender", uiState.editGender, listOf("male", "female", "other")) { viewModel.onGenderChanged(it) }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                NumberField("Age", uiState.editAge, { viewModel.onAgeChanged(it) }, Modifier.weight(1f))
                                NumberField("Height (cm)", uiState.editHeightCm, { viewModel.onHeightChanged(it) }, Modifier.weight(1f))
                            }
                            NumberField("Weight (kg)", uiState.editWeightKg, { viewModel.onWeightChanged(it) }, Modifier.fillMaxWidth())
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoRow(Icons.Default.Person, "Gender", uiState.editGender.replaceFirstChar { it.uppercase() })
                            InfoRow(Icons.Default.Person, "Age", "${uiState.editAge} years")
                            InfoRow(Icons.Default.Height, "Height", "${uiState.editHeightCm} cm")
                            InfoRow(Icons.Default.MonitorWeight, "Weight", "${uiState.editWeightKg} kg")
                        }
                    }
                }
                item { Text("Diet Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                item {
                    if (uiState.isEditing) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            DropdownField("Activity Level", uiState.editActivityLevel, listOf("sedentary", "light", "moderate", "active", "very_active")) { viewModel.onActivityLevelChanged(it) }
                            DropdownField("Dietary Preference", uiState.editDietaryPreference, listOf("vegetarian", "non_vegetarian", "vegan", "eggetarian")) { viewModel.onDietaryPrefChanged(it) }
                            DropdownField("Goal", uiState.editGoalType, listOf("lose_weight", "gain_weight", "maintain", "muscle_gain")) { viewModel.onGoalChanged(it) }
                            NumberField("Target Calories", uiState.editTargetCalories, { viewModel.onTargetCaloriesChanged(it) }, Modifier.fillMaxWidth())
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoRow(Icons.Default.FitnessCenter, "Activity", uiState.editActivityLevel.replace('_', ' ').replaceFirstChar { it.uppercase() })
                            InfoRow(Icons.Default.Restaurant, "Diet", uiState.editDietaryPreference.replace('_', ' ').replaceFirstChar { it.uppercase() })
                            InfoRow(Icons.Default.TrendingUp, "Goal", uiState.editGoalType.replace('_', ' ').replaceFirstChar { it.uppercase() })
                            InfoRow(Icons.Default.LocalFireDepartment, "Target Calories", "${uiState.editTargetCalories} kcal")
                        }
                    }
                }
                if (uiState.isEditing) { item { DietPrimaryButton(text = "Save Profile", onClick = { viewModel.saveProfile() }, isLoading = uiState.isSaving) } }
                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun NumberField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(value, onValueChange, label = { Text(label) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = RoundedCornerShape(12.dp), modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(label: String, selected: String, options: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
        OutlinedTextField(selected.replace('_', ' ').replaceFirstChar { it.uppercase() }, {}, readOnly = true, label = { Text(label) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable), shape = RoundedCornerShape(12.dp))
        ExposedDropdownMenu(expanded, { expanded = false }) { options.forEach { o -> DropdownMenuItem(text = { Text(o.replace('_', ' ').replaceFirstChar { it.uppercase() }) }, onClick = { onSelected(o); expanded = false }) } }
    }
}
