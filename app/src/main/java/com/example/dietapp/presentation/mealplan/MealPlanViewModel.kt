package com.example.dietapp.presentation.mealplan

import com.example.dietapp.domain.model.MealPlan
import com.example.dietapp.domain.usecase.GenerateAiMealPlanUseCase
import com.example.dietapp.domain.usecase.GetMealPlansUseCase
import com.example.dietapp.domain.usecase.ToggleFavoriteMealPlanUseCase
import com.example.dietapp.domain.util.Resource
import com.example.dietapp.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class MealPlanUiState(
    val mealPlans: List<MealPlan> = emptyList(),
    val isLoading: Boolean = true,
    val isGenerating: Boolean = false,
    val error: String? = null,
    val selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val showAiDialog: Boolean = false,
    val filterFavoritesOnly: Boolean = false
)

sealed class MealPlanEvent {
    data class ShowError(val message: String) : MealPlanEvent()
    data class MealPlanGenerated(val plans: List<MealPlan>) : MealPlanEvent()
}

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val getMealPlansUseCase: GetMealPlansUseCase,
    private val generateAiMealPlanUseCase: GenerateAiMealPlanUseCase,
    private val toggleFavoriteMealPlanUseCase: ToggleFavoriteMealPlanUseCase
) : BaseViewModel<MealPlanUiState, MealPlanEvent>(MealPlanUiState()) {

    init {
        loadMealPlans()
    }

    private fun loadMealPlans() {
        launchSafe(
            onError = { updateState { copy(isLoading = false, error = it.message) } }
        ) {
            getMealPlansUseCase(userId = 1L, date = currentState.selectedDate).collect { result ->
                when (result) {
                    is Resource.Success -> updateState {
                        copy(
                            mealPlans = if (filterFavoritesOnly) result.data.filter { it.isFavorite } else result.data,
                            isLoading = false,
                            error = null
                        )
                    }
                    is Resource.Error -> updateState {
                        copy(
                            mealPlans = result.data ?: emptyList(),
                            isLoading = false,
                            error = result.message
                        )
                    }
                    is Resource.Loading -> updateState {
                        copy(mealPlans = result.data ?: mealPlans, isLoading = true)
                    }
                }
            }
        }
    }

    fun generateAiMealPlan(
        dietaryPreference: String = "vegetarian",
        targetCalories: Int = 2000,
        goalType: String = "maintain",
        activityLevel: String = "sedentary"
    ) {
        updateState { copy(isGenerating = true, showAiDialog = false) }
        launchSafe(
            onError = {
                updateState { copy(isGenerating = false, error = it.message) }
            }
        ) {
            val result = generateAiMealPlanUseCase(
                userId = 1L,
                date = currentState.selectedDate,
                targetCalories = targetCalories,
                dietaryPreference = dietaryPreference,
                goalType = goalType,
                activityLevel = activityLevel
            )
            when (result) {
                is Resource.Success -> {
                    sendEvent(MealPlanEvent.MealPlanGenerated(result.data))
                    updateState { copy(isGenerating = false) }
                    loadMealPlans()
                }
                is Resource.Error -> updateState {
                    copy(isGenerating = false, error = result.message)
                }
                is Resource.Loading -> { /* keep generating state */ }
            }
        }
    }

    fun toggleFavorite(mealPlan: MealPlan) {
        launchSafe {
            toggleFavoriteMealPlanUseCase(mealPlan.id, !mealPlan.isFavorite)
            loadMealPlans()
        }
    }

    fun onDateChanged(date: String) {
        updateState { copy(selectedDate = date, isLoading = true) }
        loadMealPlans()
    }

    fun toggleFavoritesFilter() {
        updateState { copy(filterFavoritesOnly = !filterFavoritesOnly) }
        loadMealPlans()
    }

    fun showAiDialog() = updateState { copy(showAiDialog = true) }
    fun hideAiDialog() = updateState { copy(showAiDialog = false) }
    fun dismissError() = updateState { copy(error = null) }
}
