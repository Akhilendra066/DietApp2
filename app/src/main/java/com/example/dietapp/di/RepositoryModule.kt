package com.example.dietapp.di

import com.example.dietapp.data.repository.FoodRepositoryImpl
import com.example.dietapp.data.repository.MealPlanRepositoryImpl
import com.example.dietapp.data.repository.UserRepositoryImpl
import com.example.dietapp.domain.repository.FoodRepository
import com.example.dietapp.domain.repository.MealPlanRepository
import com.example.dietapp.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMealPlanRepository(
        mealPlanRepositoryImpl: MealPlanRepositoryImpl
    ): MealPlanRepository

    @Binds
    @Singleton
    abstract fun bindFoodRepository(
        foodRepositoryImpl: FoodRepositoryImpl
    ): FoodRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}
