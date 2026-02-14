package com.example.dietapp.presentation.dietlog

import com.example.dietapp.domain.model.DailyNutritionSummary
import com.example.dietapp.domain.model.DietLog
import com.example.dietapp.domain.usecase.GetDailyNutritionUseCase
import com.example.dietapp.domain.usecase.LogDietUseCase
import com.example.dietapp.domain.util.Resource
import com.example.dietapp.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class DietLogUiState(
    val selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val nutritionSummary: DailyNutritionSummary? = null,
    val isLoading: Boolean = true,
    val isLogging: Boolean = false,
    val error: String? = null,
    val showLogDialog: Boolean = false,
    val targetCalories: Int = 2000
)

sealed class DietLogEvent {
    data class ShowError(val message: String) : DietLogEvent()
    data object LogSuccess : DietLogEvent()
}

@HiltViewModel
class DietLogViewModel @Inject constructor(
    private val getDailyNutritionUseCase: GetDailyNutritionUseCase,
    private val logDietUseCase: LogDietUseCase
) : BaseViewModel<DietLogUiState, DietLogEvent>(DietLogUiState()) {

    init {
        loadNutrition()
    }

    private fun loadNutrition() {
        launchSafe(
            onError = { updateState { copy(isLoading = false, error = it.message) } }
        ) {
            getDailyNutritionUseCase(userId = 1L, date = currentState.selectedDate).collect { result ->
                when (result) {
                    is Resource.Success -> updateState {
                        copy(
                            nutritionSummary = result.data.copy(targetCalories = targetCalories),
                            isLoading = false,
                            error = null
                        )
                    }
                    is Resource.Error -> updateState {
                        copy(isLoading = false, error = result.message)
                    }
                    is Resource.Loading -> updateState { copy(isLoading = true) }
                }
            }
        }
    }

    fun logMeal(
        foodName: String,
        mealType: String,
        calories: Int,
        proteinG: Float,
        carbsG: Float,
        fatG: Float,
        fiberG: Float,
        quantity: Float = 1f
    ) {
        val dietLog = DietLog(
            date = currentState.selectedDate,
            mealType = mealType,
            foodName = foodName,
            quantity = quantity,
            calories = calories,
            protein = proteinG,
            carbs = carbsG,
            fat = fatG,
            fiber = fiberG
        )

        updateState { copy(isLogging = true) }
        launchSafe(
            onError = {
                updateState { copy(isLogging = false, error = it.message) }
            }
        ) {
            val result = logDietUseCase(userId = 1L, dietLog = dietLog)
            when (result) {
                is Resource.Success -> {
                    updateState { copy(isLogging = false, showLogDialog = false) }
                    sendEvent(DietLogEvent.LogSuccess)
                    loadNutrition()
                }
                is Resource.Error -> updateState {
                    copy(isLogging = false, error = result.message)
                }
                is Resource.Loading -> { /* keep logging state */ }
            }
        }
    }

    fun onDateChanged(date: String) {
        updateState { copy(selectedDate = date, isLoading = true) }
        loadNutrition()
    }

    fun showLogDialog() = updateState { copy(showLogDialog = true) }
    fun hideLogDialog() = updateState { copy(showLogDialog = false) }
    fun dismissError() = updateState { copy(error = null) }
}
