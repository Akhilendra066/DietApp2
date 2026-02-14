package com.example.dietapp.data.repository

import com.example.dietapp.data.local.dao.DietLogDao
import com.example.dietapp.data.local.dao.UserDao
import com.example.dietapp.data.local.entity.toDomain
import com.example.dietapp.data.local.entity.toEntity
import com.example.dietapp.domain.model.DailyNutritionSummary
import com.example.dietapp.domain.model.DietLog
import com.example.dietapp.domain.model.User
import com.example.dietapp.domain.repository.UserRepository
import com.example.dietapp.domain.util.Resource
import com.example.dietapp.util.ErrorHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val dietLogDao: DietLogDao,
    private val errorHandler: ErrorHandler
) : UserRepository {

    override fun getCurrentUser(): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            userDao.getCurrentUser().collect { entity ->
                if (entity != null) {
                    emit(Resource.Success(entity.toDomain()))
                } else {
                    emit(Resource.Error("No user profile found. Please set up your profile."))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get current user")
            emit(Resource.Error(errorHandler.getErrorMessage(e), throwable = e))
        }
    }

    override suspend fun saveUser(user: User): Resource<Long> {
        return try {
            val id = userDao.insertUser(user.toEntity())
            Resource.Success(id)
        } catch (e: Exception) {
            Timber.e(e, "Failed to save user")
            Resource.Error(errorHandler.getErrorMessage(e), throwable = e)
        }
    }

    override suspend fun updateUser(user: User): Resource<Unit> {
        return try {
            userDao.updateUser(user.toEntity())
            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update user")
            Resource.Error(errorHandler.getErrorMessage(e), throwable = e)
        }
    }

    override fun getDietLogsByDate(userId: Long, date: String): Flow<Resource<List<DietLog>>> {
        return dietLogDao.getDietLogsByDate(userId, date).map { entities ->
            Resource.Success(entities.map { it.toDomain() }) as Resource<List<DietLog>>
        }
    }

    override suspend fun logDiet(userId: Long, dietLog: DietLog): Resource<Long> {
        return try {
            val id = dietLogDao.insertDietLog(dietLog.toEntity(userId))
            Resource.Success(id)
        } catch (e: Exception) {
            Timber.e(e, "Failed to log diet")
            Resource.Error(errorHandler.getErrorMessage(e), throwable = e)
        }
    }

    override suspend fun deleteDietLog(dietLog: DietLog) {
        try {
            dietLogDao.deleteDietLog(dietLog.toEntity(userId = 0)) // userId not needed for delete by PK
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete diet log")
        }
    }

    override fun getDailyNutritionSummary(
        userId: Long,
        date: String
    ): Flow<Resource<DailyNutritionSummary>> {
        return combine(
            dietLogDao.getTotalCaloriesForDate(userId, date),
            dietLogDao.getTotalProteinForDate(userId, date),
            dietLogDao.getTotalCarbsForDate(userId, date),
            dietLogDao.getTotalFatForDate(userId, date),
            dietLogDao.getTotalWaterForDate(userId, date)
        ) { calories, protein, carbs, fat, water ->
            Resource.Success(
                DailyNutritionSummary(
                    date = date,
                    totalCalories = calories ?: 0,
                    totalProtein = protein ?: 0f,
                    totalCarbs = carbs ?: 0f,
                    totalFat = fat ?: 0f,
                    totalWaterMl = water ?: 0
                )
            ) as Resource<DailyNutritionSummary>
        }
    }

    override suspend fun syncDietLogs(): Resource<Unit> {
        return try {
            val unsyncedLogs = dietLogDao.getUnsyncedLogs()
            if (unsyncedLogs.isNotEmpty()) {
                // TODO: Send to remote API
                // For now, just mark as synced
                dietLogDao.markAsSynced(unsyncedLogs.map { it.id })
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync diet logs")
            Resource.Error(errorHandler.getErrorMessage(e), throwable = e)
        }
    }
}
