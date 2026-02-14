package com.example.dietapp.di

import android.content.Context
import androidx.room.Room
import com.example.dietapp.data.local.DietDatabase
import com.example.dietapp.data.local.dao.DietLogDao
import com.example.dietapp.data.local.dao.FoodItemDao
import com.example.dietapp.data.local.dao.MealPlanDao
import com.example.dietapp.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDietDatabase(
        @ApplicationContext context: Context
    ): DietDatabase {
        return Room.databaseBuilder(
            context,
            DietDatabase::class.java,
            DietDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: DietDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideMealPlanDao(database: DietDatabase): MealPlanDao {
        return database.mealPlanDao()
    }

    @Provides
    @Singleton
    fun provideFoodItemDao(database: DietDatabase): FoodItemDao {
        return database.foodItemDao()
    }

    @Provides
    @Singleton
    fun provideDietLogDao(database: DietDatabase): DietLogDao {
        return database.dietLogDao()
    }
}
