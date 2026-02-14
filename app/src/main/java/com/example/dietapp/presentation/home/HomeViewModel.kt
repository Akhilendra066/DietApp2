package com.example.dietapp.presentation.home

import com.example.dietapp.domain.model.DailyNutritionSummary
import com.example.dietapp.domain.model.MealPlan
import com.example.dietapp.domain.model.User
import com.example.dietapp.domain.usecase.GetDailyNutritionUseCase
import com.example.dietapp.domain.usecase.GetMealPlansUseCase
import com.example.dietapp.domain.usecase.GetUserProfileUseCase
import com.example.dietapp.domain.util.Resource
import com.example.dietapp.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class HomeUiState(
    val user: User? = null,
    val todayMealPlans: List<MealPlan> = emptyList(),
    val nutritionSummary: DailyNutritionSummary? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val greeting: String = getGreeting()
) {
    companion object {
        fun getGreeting(): String {
            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            return when {
                hour < 12 -> "Good Morning ☀️"
                hour < 17 -> "Good Afternoon 🌤️"
                hour < 21 -> "Good Evening 🌅"
                else -> "Good Night 🌙"
            }
        }
    }
}

sealed class HomeEvent {
    data class ShowError(val message: String) : HomeEvent()
    data object NavigateToMealPlan : HomeEvent()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getMealPlansUseCase: GetMealPlansUseCase,
    private val getDailyNutritionUseCase: GetDailyNutritionUseCase
) : BaseViewModel<HomeUiState, HomeEvent>(HomeUiState()) {

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        launchSafe(
            onError = { updateState { copy(isLoading = false, error = it.message) } }
        ) {
            getUserProfileUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        updateState {
                            copy(user = result.data, isLoading = false, error = null)
                        }
                        loadTodayData(result.data.id)
                    }
                    is Resource.Error -> {
                        updateState {
                            copy(isLoading = false, error = result.message)
                        }
                    }
                    is Resource.Loading -> {
                        updateState { copy(isLoading = true) }
                    }
                }
            }
        }
    }

    private fun loadTodayData(userId: Long) {
        val today = currentState.selectedDate

        // Load meal plans
        launchSafe {
            getMealPlansUseCase(userId, today).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        updateState { copy(todayMealPlans = result.data) }
                    }
                    is Resource.Error -> {
                        updateState {
                            copy(
                                todayMealPlans = result.data ?: emptyList(),
                                error = result.message
                            )
                        }
                    }
                    is Resource.Loading -> {
                        updateState {
                            copy(todayMealPlans = result.data ?: todayMealPlans)
                        }
                    }
                }
            }
        }

        // Load nutrition summary
        launchSafe {
            getDailyNutritionUseCase(userId, today).collect { result ->
                if (result is Resource.Success) {
                    updateState {
                        copy(
                            nutritionSummary = result.data.copy(
                                targetCalories = user?.targetCalories ?: 2000
                            )
                        )
                    }
                }
            }
        }
    }

    fun onDateChanged(date: String) {
        updateState { copy(selectedDate = date) }
        currentState.user?.let { loadTodayData(it.id) }
    }

    fun onRetry() {
        updateState { copy(isLoading = true, error = null) }
        loadUserProfile()
    }

    fun dismissError() {
        updateState { copy(error = null) }
    }
}
