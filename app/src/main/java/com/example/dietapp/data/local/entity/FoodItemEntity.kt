package com.example.dietapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "food_items",
    foreignKeys = [
        ForeignKey(
            entity = MealPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealPlanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("mealPlanId"), Index("category")]
)
data class FoodItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mealPlanId: Long? = null,
    val name: String,
    val nameHindi: String? = null,
    val quantity: Float = 1f,
    val unit: String = "serving", // serving, grams, ml, piece, cup, tablespoon
    val calories: Int = 0,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f,
    val fiber: Float = 0f,
    val category: String = "other", // dal, roti, rice, sabzi, salad, fruit, dairy, snack, beverage, other
    val isIndianFood: Boolean = true,
    val imageUrl: String? = null,
    val isCached: Boolean = false,
    val lastSyncedAt: Long? = null
)
