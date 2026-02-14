package com.example.dietapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.dietapp.data.local.dao.DietLogDao
import com.example.dietapp.data.local.dao.FoodItemDao
import com.example.dietapp.data.local.dao.MealPlanDao
import com.example.dietapp.data.local.dao.UserDao
import com.example.dietapp.data.local.entity.DietLogEntity
import com.example.dietapp.data.local.entity.FoodItemEntity
import com.example.dietapp.data.local.entity.MealPlanEntity
import com.example.dietapp.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        MealPlanEntity::class,
        FoodItemEntity::class,
        DietLogEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class DietDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun foodItemDao(): FoodItemDao
    abstract fun dietLogDao(): DietLogDao

    companion object {
        const val DATABASE_NAME = "diet_planner_db"
    }
}
