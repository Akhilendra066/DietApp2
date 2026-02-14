package com.example.dietapp.ui.screens.premium

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dietapp.ui.theme.DietAppTheme

private data class FamilyMember(val name: String, val relation: String, val dietPlan: String)
private val members = listOf(
    FamilyMember("Akhilendra", "You", "Vegetarian • Weight Loss"),
    FamilyMember("Priya", "Spouse", "Non-Veg • Maintenance"),
    FamilyMember("Arjun", "Child", "Balanced • Growth")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyPlanScreen(onBack: () -> Unit = {}) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Family Plan", fontWeight = FontWeight.SemiBold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { /* TODO */ }, shape = RoundedCornerShape(16.dp), containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Outlined.GroupAdd, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Member")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("Manage meal plans for your family", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(members) { member ->
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(member.relation, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(member.dietPlan, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedButton(onClick = { /* TODO */ }, shape = RoundedCornerShape(12.dp)) { Text("Manage") }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FamilyPreview() { DietAppTheme { FamilyPlanScreen() } }
