package com.example.dietapp.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dietapp.ui.theme.DietAppTheme

// ── Option Lists (matching onboarding) ──────────────────────
private val goalOptions = listOf("Lose Weight", "Build Muscle", "Maintain Weight", "Gain Weight")
private val activityLevels = listOf("Sedentary", "Lightly Active", "Moderately Active", "Active", "Very Active")
private val dietTypes = listOf("Vegetarian", "Non-Vegetarian", "Vegan", "Eggetarian", "Pescatarian", "Jain")
private val commonAllergies = listOf("Gluten", "Dairy", "Nuts", "Soy", "Eggs", "Shellfish", "Lactose")
private val regions = listOf("North India", "South India", "East India", "West India", "Central India", "North-East")
private val unitOptions = listOf("Metric (kg/cm)", "Imperial (lb/ft)")
private val languageOptions = listOf("English", "Hindi")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(onBack: () -> Unit = {}, onLogout: () -> Unit = {}) {

    // ── Health & Body state ──────────────────────────────────
    var currentWeight by remember { mutableStateOf("76.2") }
    var goalWeight by remember { mutableStateOf("72") }
    var height by remember { mutableStateOf("175") }
    var showReportSheet by remember { mutableStateOf(false) }

    // ── Diet Preferences state ───────────────────────────────
    var selectedGoal by remember { mutableStateOf("Lose Weight") }
    var selectedActivity by remember { mutableStateOf("Moderately Active") }
    var selectedDiet by remember { mutableStateOf("Vegetarian") }
    var selectedAllergies by remember { mutableStateOf(listOf<String>()) }
    var selectedRegion by remember { mutableStateOf("North India") }
    var weeklyBudget by remember { mutableStateOf("1200") }

    // ── Notifications state ──────────────────────────────────
    var notifications by remember { mutableStateOf(true) }
    var mealReminders by remember { mutableStateOf(true) }
    var waterReminders by remember { mutableStateOf(true) }

    // ── Account state ────────────────────────────────────────
    var selectedUnit by remember { mutableStateOf("Metric (kg/cm)") }
    var selectedLanguage by remember { mutableStateOf("English") }
    var showClearCacheDialog by remember { mutableStateOf(false) }

    // ── Expansion state ──────────────────────────────────────
    var expandedItem by remember { mutableStateOf<String?>(null) }

    // ── Lab Report Bottom Sheet ──────────────────────────────
    if (showReportSheet) {
        ModalBottomSheet(onDismissRequest = { showReportSheet = false }) {
            Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
                Text("Upload Lab Report", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Choose how to add your report", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    ReportOptionCard(
                        icon = Icons.Outlined.CameraAlt,
                        label = "Camera",
                        modifier = Modifier.weight(1f),
                        onClick = { /* TODO: Launch camera */ showReportSheet = false }
                    )
                    ReportOptionCard(
                        icon = Icons.Outlined.PhotoLibrary,
                        label = "Gallery",
                        modifier = Modifier.weight(1f),
                        onClick = { /* TODO: Launch gallery */ showReportSheet = false }
                    )
                }
            }
        }
    }

    // ── Clear Cache Dialog ───────────────────────────────────
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            confirmButton = {
                TextButton(onClick = { /* TODO: Clear cache */ showClearCacheDialog = false }) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) { Text("Cancel") }
            },
            title = { Text("Clear Cache?") },
            text = { Text("This will remove cached images and temporary data. Your account data will not be affected.") },
            icon = { Icon(Icons.Outlined.DeleteSweep, null) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // SECTION 1: Health & Body
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            SectionHeader("Health & Body")

            SettingsExpandableCard(
                icon = Icons.Outlined.MonitorWeight,
                title = "Current Weight",
                currentValue = "$currentWeight kg",
                expanded = expandedItem == "weight",
                onClick = { expandedItem = if (expandedItem == "weight") null else "weight" }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = currentWeight,
                        onValueChange = { currentWeight = it },
                        label = { Text("Weight (kg)") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { expandedItem = null },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp)
                    ) { Text("Update") }
                }
            }

            SettingsExpandableCard(
                icon = Icons.Outlined.FlagCircle,
                title = "Goal Weight",
                currentValue = "$goalWeight kg",
                expanded = expandedItem == "goalWeight",
                onClick = { expandedItem = if (expandedItem == "goalWeight") null else "goalWeight" }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = goalWeight,
                        onValueChange = { goalWeight = it },
                        label = { Text("Goal Weight (kg)") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { expandedItem = null },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp)
                    ) { Text("Update") }
                }
            }

            SettingsExpandableCard(
                icon = Icons.Outlined.Height,
                title = "Height",
                currentValue = "$height cm",
                expanded = expandedItem == "height",
                onClick = { expandedItem = if (expandedItem == "height") null else "height" }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("Height (cm)") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { expandedItem = null },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp)
                    ) { Text("Update") }
                }
            }

            SettingsClickableCard(
                icon = Icons.Outlined.Description,
                title = "Upload Lab Report",
                subtitle = "Add report via Camera or Gallery",
                onClick = { showReportSheet = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // SECTION 2: Diet Preferences
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            SectionHeader("Diet Preferences")

            SettingsExpandableCard(
                icon = Icons.Outlined.FitnessCenter,
                title = "Fitness Goal",
                currentValue = selectedGoal,
                expanded = expandedItem == "goal",
                onClick = { expandedItem = if (expandedItem == "goal") null else "goal" }
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    goalOptions.forEach { option ->
                        FilterChip(
                            selected = selectedGoal == option,
                            onClick = { selectedGoal = option },
                            label = { Text(option) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            SettingsExpandableCard(
                icon = Icons.Outlined.DirectionsWalk,
                title = "Activity Level",
                currentValue = selectedActivity,
                expanded = expandedItem == "activity",
                onClick = { expandedItem = if (expandedItem == "activity") null else "activity" }
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    activityLevels.forEach { option ->
                        FilterChip(
                            selected = selectedActivity == option,
                            onClick = { selectedActivity = option },
                            label = { Text(option) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            SettingsExpandableCard(
                icon = Icons.Outlined.Restaurant,
                title = "Diet Type",
                currentValue = selectedDiet,
                expanded = expandedItem == "diet",
                onClick = { expandedItem = if (expandedItem == "diet") null else "diet" }
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    dietTypes.forEach { option ->
                        FilterChip(
                            selected = selectedDiet == option,
                            onClick = { selectedDiet = option },
                            label = { Text(option) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            SettingsExpandableCard(
                icon = Icons.Outlined.DoNotDisturb,
                title = "Allergies",
                currentValue = if (selectedAllergies.isEmpty()) "None" else selectedAllergies.joinToString(", "),
                expanded = expandedItem == "allergies",
                onClick = { expandedItem = if (expandedItem == "allergies") null else "allergies" }
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    commonAllergies.forEach { allergy ->
                        FilterChip(
                            selected = selectedAllergies.contains(allergy),
                            onClick = {
                                selectedAllergies = if (selectedAllergies.contains(allergy))
                                    selectedAllergies - allergy
                                else
                                    selectedAllergies + allergy
                            },
                            label = { Text(allergy) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            SettingsExpandableCard(
                icon = Icons.Outlined.LocationOn,
                title = "Region",
                currentValue = selectedRegion,
                expanded = expandedItem == "region",
                onClick = { expandedItem = if (expandedItem == "region") null else "region" }
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    regions.forEach { option ->
                        FilterChip(
                            selected = selectedRegion == option,
                            onClick = { selectedRegion = option },
                            label = { Text(option) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            SettingsExpandableCard(
                icon = Icons.Outlined.CurrencyRupee,
                title = "Weekly Budget",
                currentValue = "₹$weeklyBudget",
                expanded = expandedItem == "budget",
                onClick = { expandedItem = if (expandedItem == "budget") null else "budget" }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = weeklyBudget,
                        onValueChange = { weeklyBudget = it },
                        label = { Text("Weekly budget (₹)") },
                        leadingIcon = { Icon(Icons.Outlined.CurrencyRupee, null) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { expandedItem = null },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp)
                    ) { Text("Save") }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // SECTION 3: Notifications
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            SectionHeader("Notifications")

            SettingsToggle("Push Notifications", "Enable all notifications", Icons.Outlined.Notifications, notifications) { notifications = it }
            SettingsToggle("Meal Reminders", "Breakfast, lunch, dinner alerts", Icons.Outlined.Restaurant, mealReminders) { mealReminders = it }
            SettingsToggle("Water Reminders", "Hourly hydration nudges", Icons.Outlined.WaterDrop, waterReminders) { waterReminders = it }

            Spacer(modifier = Modifier.height(24.dp))

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // SECTION 4: Account
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            SectionHeader("Account")

            SettingsExpandableCard(
                icon = Icons.Outlined.Straighten,
                title = "Units",
                currentValue = selectedUnit,
                expanded = expandedItem == "units",
                onClick = { expandedItem = if (expandedItem == "units") null else "units" }
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    unitOptions.forEach { option ->
                        FilterChip(
                            selected = selectedUnit == option,
                            onClick = { selectedUnit = option },
                            label = { Text(option) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            SettingsExpandableCard(
                icon = Icons.Outlined.Language,
                title = "Language",
                currentValue = selectedLanguage,
                expanded = expandedItem == "language",
                onClick = { expandedItem = if (expandedItem == "language") null else "language" }
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    languageOptions.forEach { option ->
                        FilterChip(
                            selected = selectedLanguage == option,
                            onClick = { selectedLanguage = option },
                            label = { Text(option) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            SettingsClickableCard(
                icon = Icons.Outlined.DeleteSweep,
                title = "Clear Cache",
                subtitle = "Free up storage space",
                onClick = { showClearCacheDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Sign Out ─────────────────────────────────────
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Outlined.Logout, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ═════════════════════════════════════════════════════════════
// Reusable Composables
// ═════════════════════════════════════════════════════════════

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun SettingsExpandableCard(
    icon: ImageVector,
    title: String,
    currentValue: String,
    expanded: Boolean,
    onClick: () -> Unit,
    expandedContent: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .clickable(onClick = onClick)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    Text(currentValue, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                    if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    "Toggle",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)) {
                    expandedContent()
                }
            }
        }
    }
}

@Composable
private fun SettingsClickableCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                Icons.Outlined.ChevronRight,
                "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun ReportOptionCard(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = modifier.height(120.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsPreview() { DietAppTheme { SettingsScreen() } }
