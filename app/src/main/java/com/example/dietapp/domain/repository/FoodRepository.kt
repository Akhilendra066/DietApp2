package com.example.dietapp.domain.repository

import com.example.dietapp.domain.model.FoodItem
import com.example.dietapp.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface FoodRepository {

    fun getFoodItems(category: String? = null): Flow<Resource<List<FoodItem>>>

    fun searchFoodItems(query: String): Flow<Resource<List<FoodItem>>>

    fun getIndianFoodItems(): Flow<Resource<List<FoodItem>>>

    fun getFoodItemById(id: Long): Flow<Resource<FoodItem>>

    fun getCategories(): Flow<Resource<List<String>>>
}
