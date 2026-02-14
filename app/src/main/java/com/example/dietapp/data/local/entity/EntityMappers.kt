package com.example.dietapp.data.local.entity

import com.example.dietapp.domain.model.DietLog
import com.example.dietapp.domain.model.FoodItem
import com.example.dietapp.domain.model.MealPlan
import com.example.dietapp.domain.model.User

/**
 * Extension functions to map Entities to Domain models and vice versa.
 */

// UserEntity -> User
fun UserEntity.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        age = age,
        gender = gender,
        heightCm = heightCm,
        weightKg = weightKg,
        activityLevel = activityLevel,
        dietaryPreference = dietaryPreference,
        goalType = goalType,
        targetCalories = targetCalories
    )
}

// User -> UserEntity
fun User.toEntity(firebaseUid: String? = null): UserEntity {
    return UserEntity(
        id = id,
        name = name,
        email = email,
        age = age,
        gender = gender,
        heightCm = heightCm,
        weightKg = weightKg,
        activityLevel = activityLevel,
        dietaryPreference = dietaryPreference,
        goalType = goalType,
        targetCalories = targetCalories,
        firebaseUid = firebaseUid
    )
}

// MealPlanEntity -> MealPlan
fun MealPlanEntity.toDomain(foodItems: List<FoodItem> = emptyList()): MealPlan {
    return MealPlan(
        id = id,
        date = date,
        mealType = mealType,
        name = name,
        nameHindi = nameHindi,
        description = description,
        totalCalories = totalCalories,
        totalProtein = totalProtein,
        totalCarbs = totalCarbs,
        totalFat = totalFat,
        totalFiber = totalFiber,
        imageUrl = imageUrl,
        isAiGenerated = isAiGenerated,
        isFavorite = isFavorite,
        foodItems = foodItems
    )
}

// FoodItemEntity -> FoodItem
fun FoodItemEntity.toDomain(): FoodItem {
    return FoodItem(
        id = id,
        name = name,
        nameHindi = nameHindi,
        quantity = quantity,
        unit = unit,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        fiber = fiber,
        category = category,
        isIndianFood = isIndianFood,
        imageUrl = imageUrl
    )
}

// DietLogEntity -> DietLog
fun DietLogEntity.toDomain(): DietLog {
    return DietLog(
        id = id,
        date = date,
        mealType = mealType,
        foodName = foodName,
        foodNameHindi = foodNameHindi,
        quantity = quantity,
        unit = unit,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        fiber = fiber,
        waterMl = waterMl,
        notes = notes,
        timestamp = timestamp
    )
}

// DietLog -> DietLogEntity
fun DietLog.toEntity(userId: Long): DietLogEntity {
    return DietLogEntity(
        id = id,
        userId = userId,
        date = date,
        mealType = mealType,
        foodName = foodName,
        foodNameHindi = foodNameHindi,
        quantity = quantity,
        unit = unit,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        fiber = fiber,
        waterMl = waterMl,
        notes = notes,
        timestamp = timestamp
    )
}
