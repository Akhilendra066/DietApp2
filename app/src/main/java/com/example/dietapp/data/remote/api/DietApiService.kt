package com.example.dietapp.data.remote.api

import com.example.dietapp.data.remote.dto.FoodItemDto
import com.example.dietapp.data.remote.dto.MealPlanDto
import com.example.dietapp.data.remote.dto.MealPlanRequestDto
import com.example.dietapp.data.remote.dto.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DietApiService {

    @GET("api/v1/meal-plans")
    suspend fun getMealPlans(
        @Query("userId") userId: Long,
        @Query("date") date: String,
        @Query("dietaryPreference") dietaryPreference: String? = null
    ): Response<ApiResponse<List<MealPlanDto>>>

    @POST("api/v1/meal-plans/generate")
    suspend fun generateAiMealPlan(
        @Body request: MealPlanRequestDto
    ): Response<ApiResponse<List<MealPlanDto>>>

    @GET("api/v1/food-items")
    suspend fun getFoodItems(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null,
        @Query("indianOnly") indianOnly: Boolean = true,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<List<FoodItemDto>>>

    @GET("api/v1/food-items/{id}")
    suspend fun getFoodItemById(
        @Path("id") id: Long
    ): Response<ApiResponse<FoodItemDto>>

    @GET("api/v1/food-items/categories")
    suspend fun getFoodCategories(): Response<ApiResponse<List<String>>>

    @POST("api/v1/diet-logs/sync")
    suspend fun syncDietLogs(
        @Body logs: List<com.example.dietapp.data.local.entity.DietLogEntity>
    ): Response<ApiResponse<Unit>>
}
