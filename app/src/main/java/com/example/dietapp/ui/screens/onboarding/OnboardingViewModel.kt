package com.example.dietapp.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class OnboardingUiState(
    val fullName: String = "",
    val age: String = "",
    val gender: String = "",
    val heightCm: String = "",
    val weightKg: String = "",
    val goalWeight: String = "",
    val activityLevel: String = "",
    val selectedGoal: String = "",
    val dietType: String = "",
    val allergies: List<String> = emptyList(),
    val cuisinePreferences: List<String> = emptyList(),
    val weeklyBudget: String = "",
    val region: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onNameChange(v: String) = _uiState.update { it.copy(fullName = v) }
    fun onAgeChange(v: String) = _uiState.update { it.copy(age = v) }
    fun onGenderSelect(v: String) = _uiState.update { it.copy(gender = v) }
    fun onHeightChange(v: String) = _uiState.update { it.copy(heightCm = v) }
    fun onWeightChange(v: String) = _uiState.update { it.copy(weightKg = v) }
    fun onGoalWeightChange(v: String) = _uiState.update { it.copy(goalWeight = v) }
    fun onActivitySelect(v: String) = _uiState.update { it.copy(activityLevel = v) }
    fun onGoalSelect(v: String) = _uiState.update { it.copy(selectedGoal = v) }
    fun onDietSelect(v: String) = _uiState.update { it.copy(dietType = v) }
    fun onBudgetChange(v: String) = _uiState.update { it.copy(weeklyBudget = v) }
    fun onRegionSelect(v: String) = _uiState.update { it.copy(region = v) }

    fun toggleAllergy(allergy: String) {
        _uiState.update { s ->
            val list = s.allergies.toMutableList()
            if (list.contains(allergy)) list.remove(allergy) else list.add(allergy)
            s.copy(allergies = list)
        }
    }

    fun toggleCuisine(cuisine: String) {
        _uiState.update { s ->
            val list = s.cuisinePreferences.toMutableList()
            if (list.contains(cuisine)) list.remove(cuisine) else list.add(cuisine)
            s.copy(cuisinePreferences = list)
        }
    }

    fun saveOnboarding() {
        _uiState.update { it.copy(isLoading = true) }
        // TODO: Save to repository
        _uiState.update { it.copy(isLoading = false) }
    }
}
