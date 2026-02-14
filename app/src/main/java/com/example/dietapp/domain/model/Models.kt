package com.example.dietapp.domain.model

/**
 * Domain model for User profile.
 */
data class User(
    val id: Long = 0,
    val name: String,
    val email: String,
    val age: Int,
    val gender: String,
    val heightCm: Float,
    val weightKg: Float,
    val activityLevel: String,
    val dietaryPreference: String,
    val goalType: String,
    val targetCalories: Int,
    val bmi: Float = calculateBmi(heightCm, weightKg)
) {
    companion object {
        fun calculateBmi(heightCm: Float, weightKg: Float): Float {
            if (heightCm <= 0) return 0f
            val heightM = heightCm / 100f
            return weightKg / (heightM * heightM)
        }

        fun getBmiCategory(bmi: Float): String = when {
            bmi < 18.5f -> "Underweight"
            bmi < 25f -> "Normal"
            bmi < 30f -> "Overweight"
            else -> "Obese"
        }

        fun calculateDailyCalories(
            weightKg: Float,
            heightCm: Float,
            age: Int,
            gender: String,
            activityLevel: String,
            goalType: String
        ): Int {
            // Mifflin-St Jeor Equation
            val bmr = if (gender == "male") {
                10 * weightKg + 6.25f * heightCm - 5 * age + 5
            } else {
                10 * weightKg + 6.25f * heightCm - 5 * age - 161
            }

            val activityMultiplier = when (activityLevel) {
                "sedentary" -> 1.2f
                "light" -> 1.375f
                "moderate" -> 1.55f
                "active" -> 1.725f
                "very_active" -> 1.9f
                else -> 1.2f
            }

            val tdee = bmr * activityMultiplier

            return when (goalType) {
                "lose_weight" -> (tdee - 500).toInt()
                "gain_weight" -> (tdee + 500).toInt()
                else -> tdee.toInt()
            }
        }
    }
}

/**
 * Domain model for Meal Plan.
 */
data class MealPlan(
    val id: Long = 0,
    val date: String,
    val mealType: String,
    val name: String,
    val nameHindi: String? = null,
    val description: String? = null,
    val totalCalories: Int = 0,
    val totalProtein: Float = 0f,
    val totalCarbs: Float = 0f,
    val totalFat: Float = 0f,
    val totalFiber: Float = 0f,
    val imageUrl: String? = null,
    val isAiGenerated: Boolean = false,
    val isFavorite: Boolean = false,
    val foodItems: List<FoodItem> = emptyList()
)

/**
 * Domain model for Food Item.
 */
data class FoodItem(
    val id: Long = 0,
    val name: String,
    val nameHindi: String? = null,
    val quantity: Float = 1f,
    val unit: String = "serving",
    val calories: Int = 0,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f,
    val fiber: Float = 0f,
    val category: String = "other",
    val isIndianFood: Boolean = true,
    val imageUrl: String? = null
)

/**
 * Domain model for Diet Log entry.
 */
data class DietLog(
    val id: Long = 0,
    val date: String,
    val mealType: String,
    val foodName: String,
    val foodNameHindi: String? = null,
    val quantity: Float = 1f,
    val unit: String = "serving",
    val calories: Int = 0,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f,
    val fiber: Float = 0f,
    val waterMl: Int = 0,
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Daily nutrition summary.
 */
data class DailyNutritionSummary(
    val date: String,
    val totalCalories: Int = 0,
    val targetCalories: Int = 0,
    val totalProtein: Float = 0f,
    val totalCarbs: Float = 0f,
    val totalFat: Float = 0f,
    val totalFiber: Float = 0f,
    val totalWaterMl: Int = 0,
    val mealsLogged: Int = 0
) {
    val calorieProgress: Float
        get() = if (targetCalories > 0) totalCalories.toFloat() / targetCalories else 0f

    val isCalorieGoalMet: Boolean
        get() = totalCalories in (targetCalories - 100)..(targetCalories + 100)
}
