package com.example.dietapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dietapp.data.local.entity.DietLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DietLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDietLog(dietLog: DietLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDietLogs(dietLogs: List<DietLogEntity>)

    @Update
    suspend fun updateDietLog(dietLog: DietLogEntity)

    @Delete
    suspend fun deleteDietLog(dietLog: DietLogEntity)

    @Query("SELECT * FROM diet_logs WHERE id = :id")
    fun getDietLogById(id: Long): Flow<DietLogEntity?>

    @Query("SELECT * FROM diet_logs WHERE userId = :userId AND date = :date ORDER BY mealType, timestamp")
    fun getDietLogsByDate(userId: Long, date: String): Flow<List<DietLogEntity>>

    @Query("SELECT * FROM diet_logs WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC, timestamp DESC")
    fun getDietLogsByDateRange(userId: Long, startDate: String, endDate: String): Flow<List<DietLogEntity>>

    @Query("SELECT SUM(calories) FROM diet_logs WHERE userId = :userId AND date = :date")
    fun getTotalCaloriesForDate(userId: Long, date: String): Flow<Int?>

    @Query("SELECT SUM(protein) FROM diet_logs WHERE userId = :userId AND date = :date")
    fun getTotalProteinForDate(userId: Long, date: String): Flow<Float?>

    @Query("SELECT SUM(carbs) FROM diet_logs WHERE userId = :userId AND date = :date")
    fun getTotalCarbsForDate(userId: Long, date: String): Flow<Float?>

    @Query("SELECT SUM(fat) FROM diet_logs WHERE userId = :userId AND date = :date")
    fun getTotalFatForDate(userId: Long, date: String): Flow<Float?>

    @Query("SELECT SUM(waterMl) FROM diet_logs WHERE userId = :userId AND date = :date")
    fun getTotalWaterForDate(userId: Long, date: String): Flow<Int?>

    @Query("SELECT * FROM diet_logs WHERE isSynced = 0")
    suspend fun getUnsyncedLogs(): List<DietLogEntity>

    @Query("UPDATE diet_logs SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)

    @Query("DELETE FROM diet_logs WHERE userId = :userId AND date = :date")
    suspend fun deleteDietLogsForDate(userId: Long, date: String)
}
