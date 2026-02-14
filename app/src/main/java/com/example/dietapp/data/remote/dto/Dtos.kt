package com.example.dietapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Generic API response wrapper.
 */
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("data") val data: T? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("message") val message: String? = null
)

/**
 * DTO for Meal Plan from API.
 */
data class MealPlanDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("date") val date: String,
    @SerializedName("meal_type") val mealType: String,
    @SerializedName("name") val name: String,
    @SerializedName("name_hindi") val nameHindi: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("total_calories") val totalCalories: Int = 0,
    @SerializedName("total_protein") val totalProtein: Float = 0f,
    @SerializedName("total_carbs") val totalCarbs: Float = 0f,
    @SerializedName("total_fat") val totalFat: Float = 0f,
    @SerializedName("total_fiber") val totalFiber: Float = 0f,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("is_ai_generated") val isAiGenerated: Boolean = false,
    @SerializedName("food_items") val foodItems: List<FoodItemDto>? = null
)

/**
 * DTO for Food Item from API.
 */
data class FoodItemDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("name") val name: String,
    @SerializedName("name_hindi") val nameHindi: String? = null,
    @SerializedName("quantity") val quantity: Float = 1f,
    @SerializedName("unit") val unit: String = "serving",
    @SerializedName("calories") val calories: Int = 0,
    @SerializedName("protein") val protein: Float = 0f,
    @SerializedName("carbs") val carbs: Float = 0f,
    @SerializedName("fat") val fat: Float = 0f,
    @SerializedName("fiber") val fiber: Float = 0f,
    @SerializedName("category") val category: String = "other",
    @SerializedName("is_indian_food") val isIndianFood: Boolean = true,
    @SerializedName("image_url") val imageUrl: String? = null
)

/**
 * Request DTO for AI meal plan generation.
 */
data class MealPlanRequestDto(
    @SerializedName("user_id") val userId: Long,
    @SerializedName("date") val date: String,
    @SerializedName("target_calories") val targetCalories: Int,
    @SerializedName("dietary_preference") val dietaryPreference: String,
    @SerializedName("goal_type") val goalType: String,
    @SerializedName("activity_level") val activityLevel: String
)
