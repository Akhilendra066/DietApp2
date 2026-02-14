package com.example.dietapp.data.repository

import com.example.dietapp.data.local.dao.MealPlanDao
import com.example.dietapp.data.local.entity.toDomain
import com.example.dietapp.data.remote.api.DietApiService
import com.example.dietapp.data.remote.dto.MealPlanRequestDto
import com.example.dietapp.data.remote.dto.toDomain
import com.example.dietapp.data.remote.dto.toEntity
import com.example.dietapp.domain.model.MealPlan
import com.example.dietapp.domain.repository.MealPlanRepository
import com.example.dietapp.domain.util.Resource
import com.example.dietapp.util.ErrorHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealPlanRepositoryImpl @Inject constructor(
    private val mealPlanDao: MealPlanDao,
    private val apiService: DietApiService,
    private val errorHandler: ErrorHandler
) : MealPlanRepository {

    override fun getMealPlansByDate(userId: Long, date: String): Flow<Resource<List<MealPlan>>> = flow {
        emit(Resource.Loading())

        // Emit cached data first (offline-first)
        val cachedPlans = mealPlanDao.getMealPlansByDate(userId, date)
        cachedPlans.collect { entities ->
            val domainPlans = entities.map { it.toDomain() }
            emit(Resource.Loading(domainPlans))
        }

        // Try to fetch from network
        try {
            val response = apiService.getMealPlans(userId, date)
            if (response.isSuccessful && response.body()?.success == true) {
                val remotePlans = response.body()?.data ?: emptyList()

                // Cache to database
                mealPlanDao.deleteMealPlansForDate(userId, date)
                mealPlanDao.insertMealPlans(remotePlans.map { it.toEntity(userId) })

                emit(Resource.Success(remotePlans.map { it.toDomain() }))
            } else {
                // Network failed, use cache
                mealPlanDao.getMealPlansByDate(userId, date).collect { entities ->
                    emit(Resource.Success(entities.map { it.toDomain() }))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch meal plans from network")
            // Fallback to cached data
            mealPlanDao.getMealPlansByDate(userId, date).collect { entities ->
                val cachedData = entities.map { it.toDomain() }
                emit(Resource.Error(
                    message = errorHandler.getErrorMessage(e),
                    data = cachedData,
                    throwable = e
                ))
            }
        }
    }

    override fun getMealPlansByDateRange(
        userId: Long,
        startDate: String,
        endDate: String
    ): Flow<Resource<List<MealPlan>>> {
        return mealPlanDao.getMealPlansByDateRange(userId, startDate, endDate).map { entities ->
            Resource.Success(entities.map { it.toDomain() })
        }
    }

    override fun getFavoriteMealPlans(userId: Long): Flow<Resource<List<MealPlan>>> {
        return mealPlanDao.getFavoriteMealPlans(userId).map { entities ->
            Resource.Success(entities.map { it.toDomain() })
        }
    }

    override suspend fun generateAiMealPlan(
        userId: Long,
        date: String,
        targetCalories: Int,
        dietaryPreference: String,
        goalType: String,
        activityLevel: String
    ): Resource<List<MealPlan>> {
        return try {
            val request = MealPlanRequestDto(
                userId = userId,
                date = date,
                targetCalories = targetCalories,
                dietaryPreference = dietaryPreference,
                goalType = goalType,
                activityLevel = activityLevel
            )
            val response = apiService.generateAiMealPlan(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val plans = response.body()?.data ?: emptyList()
                // Cache AI-generated plans
                mealPlanDao.insertMealPlans(plans.map { it.toEntity(userId) })
                Resource.Success(plans.map { it.toDomain() })
            } else {
                Resource.Error(response.body()?.error ?: "Failed to generate meal plan")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate AI meal plan")
            Resource.Error(errorHandler.getErrorMessage(e), throwable = e)
        }
    }

    override suspend fun toggleFavorite(mealPlanId: Long, isFavorite: Boolean) {
        mealPlanDao.updateFavoriteStatus(mealPlanId, isFavorite)
    }

    override fun getTotalCaloriesForDate(userId: Long, date: String): Flow<Int?> {
        return mealPlanDao.getTotalCaloriesForDate(userId, date)
    }
}
