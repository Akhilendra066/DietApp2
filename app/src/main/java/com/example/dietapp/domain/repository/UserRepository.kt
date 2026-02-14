package com.example.dietapp.domain.repository

import com.example.dietapp.domain.model.DailyNutritionSummary
import com.example.dietapp.domain.model.DietLog
import com.example.dietapp.domain.model.User
import com.example.dietapp.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    fun getCurrentUser(): Flow<Resource<User>>

    suspend fun saveUser(user: User): Resource<Long>

    suspend fun updateUser(user: User): Resource<Unit>

    fun getDietLogsByDate(userId: Long, date: String): Flow<Resource<List<DietLog>>>

    suspend fun logDiet(userId: Long, dietLog: DietLog): Resource<Long>

    suspend fun deleteDietLog(dietLog: DietLog)

    fun getDailyNutritionSummary(userId: Long, date: String): Flow<Resource<DailyNutritionSummary>>

    suspend fun syncDietLogs(): Resource<Unit>
}
