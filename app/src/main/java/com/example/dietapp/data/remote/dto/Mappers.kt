package com.example.dietapp.data.remote.dto

import com.example.dietapp.data.local.entity.FoodItemEntity
import com.example.dietapp.data.local.entity.MealPlanEntity
import com.example.dietapp.domain.model.FoodItem
import com.example.dietapp.domain.model.MealPlan

/**
 * Extension functions to map DTOs to entities and domain models.
 */

// MealPlanDto -> MealPlanEntity
fun MealPlanDto.toEntity(userId: Long): MealPlanEntity {
    return MealPlanEntity(
        id = id,
        userId = userId,
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
        isAiGenerated = isAiGenerated
    )
}

// MealPlanDto -> MealPlan (Domain model)
fun MealPlanDto.toDomain(): MealPlan {
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
        isFavorite = false,
        foodItems = foodItems?.map { it.toDomain() } ?: emptyList()
    )
}

// FoodItemDto -> FoodItemEntity
fun FoodItemDto.toEntity(mealPlanId: Long? = null): FoodItemEntity {
    return FoodItemEntity(
        id = id,
        mealPlanId = mealPlanId,
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
        imageUrl = imageUrl,
        isCached = true,
        lastSyncedAt = System.currentTimeMillis()
    )
}

// FoodItemDto -> FoodItem (Domain model)
fun FoodItemDto.toDomain(): FoodItem {
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
