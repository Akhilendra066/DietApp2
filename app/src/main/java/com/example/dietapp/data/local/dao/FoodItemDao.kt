package com.example.dietapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dietapp.data.local.entity.FoodItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItem(foodItem: FoodItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItems(foodItems: List<FoodItemEntity>)

    @Update
    suspend fun updateFoodItem(foodItem: FoodItemEntity)

    @Delete
    suspend fun deleteFoodItem(foodItem: FoodItemEntity)

    @Query("SELECT * FROM food_items WHERE id = :id")
    fun getFoodItemById(id: Long): Flow<FoodItemEntity?>

    @Query("SELECT * FROM food_items WHERE mealPlanId = :mealPlanId")
    fun getFoodItemsByMealPlan(mealPlanId: Long): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM food_items WHERE category = :category ORDER BY name")
    fun getFoodItemsByCategory(category: String): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM food_items WHERE name LIKE '%' || :query || '%' OR nameHindi LIKE '%' || :query || '%' ORDER BY name")
    fun searchFoodItems(query: String): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM food_items WHERE isIndianFood = 1 ORDER BY category, name")
    fun getIndianFoodItems(): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM food_items WHERE isCached = 1 ORDER BY name")
    fun getCachedFoodItems(): Flow<List<FoodItemEntity>>

    @Query("SELECT DISTINCT category FROM food_items ORDER BY category")
    fun getAllCategories(): Flow<List<String>>

    @Query("DELETE FROM food_items WHERE isCached = 1 AND lastSyncedAt < :timestamp")
    suspend fun deleteStaleCache(timestamp: Long)
}
