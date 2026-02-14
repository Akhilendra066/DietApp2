package com.example.dietapp.domain.repository

import com.example.dietapp.domain.model.MealPlan
import com.example.dietapp.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface MealPlanRepository {

    fun getMealPlansByDate(userId: Long, date: String): Flow<Resource<List<MealPlan>>>

    fun getMealPlansByDateRange(userId: Long, startDate: String, endDate: String): Flow<Resource<List<MealPlan>>>

    fun getFavoriteMealPlans(userId: Long): Flow<Resource<List<MealPlan>>>

    suspend fun generateAiMealPlan(
        userId: Long,
        date: String,
        targetCalories: Int,
        dietaryPreference: String,
        goalType: String,
        activityLevel: String
    ): Resource<List<MealPlan>>

    suspend fun toggleFavorite(mealPlanId: Long, isFavorite: Boolean)

    fun getTotalCaloriesForDate(userId: Long, date: String): Flow<Int?>
}
