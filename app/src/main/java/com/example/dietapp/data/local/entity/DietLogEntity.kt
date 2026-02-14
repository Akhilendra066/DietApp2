package com.example.dietapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "diet_logs",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("date"), Index("mealType")]
)
data class DietLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val date: String, // yyyy-MM-dd format
    val mealType: String, // breakfast, lunch, dinner, snack
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
    val isSynced: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
