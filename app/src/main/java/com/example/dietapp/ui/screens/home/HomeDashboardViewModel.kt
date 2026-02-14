package com.example.dietapp.ui.screens.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class HomeDashboardUiState(
    val userName: String = "Akhilendra",
    val greeting: String = "Good Morning",
    val todayCalories: Int = 1450,
    val goalCalories: Int = 2000,
    val proteinG: Float = 62f,
    val carbsG: Float = 180f,
    val fatG: Float = 48f,
    val waterMl: Int = 1200,
    val waterGoalMl: Int = 3000,
    val currentWeightKg: Float = 76.2f,
    val goalWeightKg: Float = 72f,
    val streakDays: Int = 7,
    val isLoading: Boolean = false
)

@HiltViewModel
class HomeDashboardViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(HomeDashboardUiState())
    val uiState: StateFlow<HomeDashboardUiState> = _uiState.asStateFlow()

    fun addWaterGlass() {
        _uiState.value = _uiState.value.copy(
            waterMl = (_uiState.value.waterMl + 250).coerceAtMost(_uiState.value.waterGoalMl)
        )
    }
}
