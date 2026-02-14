package com.example.dietapp.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dietapp.ui.theme.*

private data class PlanOption(val name: String, val price: String, val features: List<String>, val isPremium: Boolean)

private val plans = listOf(
    PlanOption("Free", "₹0", listOf("Basic meal plans", "Water tracking", "Weight log"), false),
    PlanOption("Pro Monthly", "₹199/mo", listOf("AI meal plans", "Smart grocery list", "AI chat", "Progress reports", "Recipe steps"), true),
    PlanOption("Pro Yearly", "₹1499/yr", listOf("Everything in Pro", "Consultation booking", "Lab report analysis", "Family plan", "Priority support"), true)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(onBack: () -> Unit = {}) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Subscription", fontWeight = FontWeight.SemiBold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Icon(Icons.Outlined.WorkspacePremium, null, tint = Secondary, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Upgrade Your Plan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Unlock premium AI-powered nutrition features", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))

            plans.forEach { plan ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = if (plan.isPremium) BorderStroke(2.dp, Brush.linearGradient(listOf(GradientStart, GradientEnd))) else null,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(if (plan.isPremium) 4.dp else 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(plan.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text(plan.price, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (plan.isPremium) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        plan.features.forEach { f ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Icon(Icons.Outlined.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(f, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if (plan.isPremium) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(12.dp)) {
                                Text("Subscribe", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SubPreview() { DietAppTheme { SubscriptionScreen() } }
