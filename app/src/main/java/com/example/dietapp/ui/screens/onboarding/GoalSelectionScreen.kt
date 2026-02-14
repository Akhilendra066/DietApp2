package com.example.dietapp.ui.screens.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dietapp.ui.components.GradientBackground
import com.example.dietapp.ui.theme.DietAppTheme

private data class GoalOption(val id: String, val title: String, val subtitle: String, val icon: ImageVector)

private val goalOptions = listOf(
    GoalOption("lose_weight", "Lose Weight", "Shed extra kilos healthily", Icons.AutoMirrored.Filled.TrendingDown),
    GoalOption("gain_muscle", "Build Muscle", "Increase lean muscle mass", Icons.Filled.FitnessCenter),
    GoalOption("maintain", "Maintain Weight", "Stay fit and healthy", Icons.Filled.FavoriteBorder),
    GoalOption("gain_weight", "Gain Weight", "Healthy weight gain plan", Icons.AutoMirrored.Filled.TrendingUp)
)

@Composable
fun GoalSelectionScreen(
    onNext: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    GradientBackground {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Step 2 of 5", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("What's your goal?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("We'll tailor your plan accordingly.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(32.dp))

            goalOptions.forEach { goal ->
                val selected = uiState.selectedGoal == goal.id
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { viewModel.onGoalSelect(goal.id) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                    ),
                    border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 0.dp else 1.dp)
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(goal.icon, null, tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(goal.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(goal.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (selected) {
                            RadioButton(selected = true, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onNext, enabled = uiState.selectedGoal.isNotBlank(), modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
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
private fun GoalPreview() { DietAppTheme { GoalSelectionScreen() } }
