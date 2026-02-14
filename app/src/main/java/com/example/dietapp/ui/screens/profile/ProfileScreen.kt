package com.example.dietapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dietapp.ui.theme.*

@Composable
fun ProfileScreen(
    onNavigateToEdit: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    onNavigateToConsultation: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Avatar + Info
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.size(96.dp).clip(CircleShape).background(Brush.linearGradient(listOf(GradientStart, GradientEnd))),
                contentAlignment = Alignment.Center
            ) {
                Text("A", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = OnPrimary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Akhilendra Singh", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("akhilendra@example.com", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            AssistChip(onClick = onNavigateToEdit, label = { Text("Edit Profile") }, leadingIcon = { Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(16.dp)) }, shape = RoundedCornerShape(12.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stats
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("76.2 kg", "Weight")
                StatItem("72 kg", "Goal")
                StatItem("7", "Streak")
                StatItem("Veg", "Diet")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Menu
        ProfileMenuItem(Icons.Outlined.CardMembership, "Subscription", "Free Plan", onNavigateToSubscription)
        ProfileMenuItem(Icons.Outlined.MedicalServices, "Book Consultation", "Talk to a dietitian", onNavigateToConsultation)
        ProfileMenuItem(Icons.Outlined.Settings, "Settings", "Preferences & more", onNavigateToSettings)
        ProfileMenuItem(Icons.Outlined.Info, "About", "Version 1.0.0", {})
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ProfileMenuItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfilePreview() { DietAppTheme { ProfileScreen() } }
