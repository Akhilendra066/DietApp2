package com.example.dietapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val age: Int,
    val gender: String, // male, female, other
    val heightCm: Float,
    val weightKg: Float,
    val activityLevel: String, // sedentary, light, moderate, active, very_active
    val dietaryPreference: String, // vegetarian, non_vegetarian, vegan, eggetarian
    val goalType: String, // lose_weight, maintain_weight, gain_weight
    val targetCalories: Int,
    val firebaseUid: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
