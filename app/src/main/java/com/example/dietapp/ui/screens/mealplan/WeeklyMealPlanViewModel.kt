package com.example.dietapp.ui.screens.mealplan

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class MealItem(
    val id: String,
    val name: String,
    val mealType: String,
    val calories: Int,
    val proteinG: Float,
    val carbsG: Float,
    val fatG: Float,
    val emoji: String
)

data class DayPlan(val day: String, val meals: List<MealItem>)

data class WeeklyMealPlanUiState(
    val selectedDay: Int = 0,
    val days: List<DayPlan> = sampleWeeklyPlan(),
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false
)

fun sampleWeeklyPlan(): List<DayPlan> {
    val meals = listOf(
        MealItem("1", "Poha with Peanuts", "Breakfast", 320, 8f, 52f, 10f, "🍚"),
        MealItem("2", "Dal Tadka + Rice + Salad", "Lunch", 520, 18f, 72f, 14f, "🍛"),
        MealItem("3", "Masala Chai + Marie Biscuit", "Snack", 120, 3f, 18f, 4f, "☕"),
        MealItem("4", "Paneer Bhurji + Roti", "Dinner", 480, 24f, 42f, 22f, "🫓")
    )
    return listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").map { DayPlan(it, meals) }
}

@HiltViewModel
class WeeklyMealPlanViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(WeeklyMealPlanUiState())
    val uiState: StateFlow<WeeklyMealPlanUiState> = _uiState.asStateFlow()

    fun selectDay(index: Int) { _uiState.value = _uiState.value.copy(selectedDay = index) }

    fun generatePlan() {
        _uiState.value = _uiState.value.copy(isGenerating = true)
        // TODO: Call AI service
        _uiState.value = _uiState.value.copy(isGenerating = false)
    }
}
