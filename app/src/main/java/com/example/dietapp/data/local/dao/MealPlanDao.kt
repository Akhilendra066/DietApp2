package com.example.dietapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dietapp.data.local.entity.MealPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlan(mealPlan: MealPlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlans(mealPlans: List<MealPlanEntity>)

    @Update
    suspend fun updateMealPlan(mealPlan: MealPlanEntity)

    @Delete
    suspend fun deleteMealPlan(mealPlan: MealPlanEntity)

    @Query("SELECT * FROM meal_plans WHERE id = :id")
    fun getMealPlanById(id: Long): Flow<MealPlanEntity?>

    @Query("SELECT * FROM meal_plans WHERE userId = :userId AND date = :date ORDER BY mealType")
    fun getMealPlansByDate(userId: Long, date: String): Flow<List<MealPlanEntity>>

    @Query("SELECT * FROM meal_plans WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date, mealType")
    fun getMealPlansByDateRange(userId: Long, startDate: String, endDate: String): Flow<List<MealPlanEntity>>

    @Query("SELECT * FROM meal_plans WHERE userId = :userId AND isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteMealPlans(userId: Long): Flow<List<MealPlanEntity>>

    @Query("SELECT * FROM meal_plans WHERE userId = :userId AND isAiGenerated = 1 ORDER BY createdAt DESC")
    fun getAiGeneratedMealPlans(userId: Long): Flow<List<MealPlanEntity>>

    @Query("SELECT SUM(totalCalories) FROM meal_plans WHERE userId = :userId AND date = :date")
    fun getTotalCaloriesForDate(userId: Long, date: String): Flow<Int?>

    @Query("UPDATE meal_plans SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM meal_plans WHERE userId = :userId AND date = :date")
    suspend fun deleteMealPlansForDate(userId: Long, date: String)
}
