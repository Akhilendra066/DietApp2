package com.example.dietapp.data.repository

import com.example.dietapp.data.local.dao.FoodItemDao
import com.example.dietapp.data.local.entity.toDomain
import com.example.dietapp.data.remote.api.DietApiService
import com.example.dietapp.data.remote.dto.toEntity
import com.example.dietapp.data.remote.dto.toDomain
import com.example.dietapp.domain.model.FoodItem
import com.example.dietapp.domain.repository.FoodRepository
import com.example.dietapp.domain.util.Resource
import com.example.dietapp.util.ErrorHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepositoryImpl @Inject constructor(
    private val foodItemDao: FoodItemDao,
    private val apiService: DietApiService,
    private val errorHandler: ErrorHandler
) : FoodRepository {

    override fun getFoodItems(category: String?): Flow<Resource<List<FoodItem>>> = flow {
        emit(Resource.Loading())

        try {
            val response = apiService.getFoodItems(category = category)
            if (response.isSuccessful && response.body()?.success == true) {
                val items = response.body()?.data ?: emptyList()
                foodItemDao.insertFoodItems(items.map { it.toEntity() })
                emit(Resource.Success(items.map { it.toDomain() }))
            } else {
                // Fallback to cache
                val cached = if (category != null) {
                    foodItemDao.getFoodItemsByCategory(category)
                } else {
                    foodItemDao.getCachedFoodItems()
                }
                cached.collect { entities ->
                    emit(Resource.Success(entities.map { it.toDomain() }))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch food items")
            val cached = if (category != null) {
                foodItemDao.getFoodItemsByCategory(category)
            } else {
                foodItemDao.getCachedFoodItems()
            }
            cached.collect { entities ->
                emit(Resource.Error(
                    message = errorHandler.getErrorMessage(e),
                    data = entities.map { it.toDomain() },
                    throwable = e
                ))
            }
        }
    }

    override fun searchFoodItems(query: String): Flow<Resource<List<FoodItem>>> {
        return foodItemDao.searchFoodItems(query).map { entities ->
            Resource.Success(entities.map { it.toDomain() })
        }
    }

    override fun getIndianFoodItems(): Flow<Resource<List<FoodItem>>> {
        return foodItemDao.getIndianFoodItems().map { entities ->
            Resource.Success(entities.map { it.toDomain() })
        }
    }

    override fun getFoodItemById(id: Long): Flow<Resource<FoodItem>> {
        return foodItemDao.getFoodItemById(id).map { entity ->
            if (entity != null) {
                Resource.Success(entity.toDomain())
            } else {
                Resource.Error("Food item not found")
            }
        }
    }

    override fun getCategories(): Flow<Resource<List<String>>> {
        return foodItemDao.getAllCategories().map { categories ->
            Resource.Success(categories)
        }
    }
}
