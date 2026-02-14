package com.example.dietapp.domain.usecase

import com.example.dietapp.domain.model.MealPlan
import com.example.dietapp.domain.repository.MealPlanRepository
import com.example.dietapp.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMealPlansUseCase @Inject constructor(
    private val mealPlanRepository: MealPlanRepository
) {
    operator fun invoke(userId: Long, date: String): Flow<Resource<List<MealPlan>>> {
        return mealPlanRepository.getMealPlansByDate(userId, date)
    }
}

class GenerateAiMealPlanUseCase @Inject constructor(
    private val mealPlanRepository: MealPlanRepository
) {
    suspend operator fun invoke(
        userId: Long,
        date: String,
        targetCalories: Int,
        dietaryPreference: String,
        goalType: String,
        activityLevel: String
    ): Resource<List<MealPlan>> {
        return mealPlanRepository.generateAiMealPlan(
            userId = userId,
            date = date,
            targetCalories = targetCalories,
            dietaryPreference = dietaryPreference,
            goalType = goalType,
            activityLevel = activityLevel
        )
    }
}

class ToggleFavoriteMealPlanUseCase @Inject constructor(
    private val mealPlanRepository: MealPlanRepository
) {
    suspend operator fun invoke(mealPlanId: Long, isFavorite: Boolean) {
        mealPlanRepository.toggleFavorite(mealPlanId, isFavorite)
    }
}
