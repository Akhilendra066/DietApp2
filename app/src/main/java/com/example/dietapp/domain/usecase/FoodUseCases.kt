package com.example.dietapp.domain.usecase

import com.example.dietapp.domain.model.FoodItem
import com.example.dietapp.domain.repository.FoodRepository
import com.example.dietapp.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFoodItemsUseCase @Inject constructor(
    private val foodRepository: FoodRepository
) {
    operator fun invoke(category: String? = null): Flow<Resource<List<FoodItem>>> {
        return foodRepository.getFoodItems(category)
    }
}

class SearchFoodItemsUseCase @Inject constructor(
    private val foodRepository: FoodRepository
) {
    operator fun invoke(query: String): Flow<Resource<List<FoodItem>>> {
        if (query.isBlank()) return foodRepository.getFoodItems()
        return foodRepository.searchFoodItems(query)
    }
}

class GetFoodCategoriesUseCase @Inject constructor(
    private val foodRepository: FoodRepository
) {
    operator fun invoke(): Flow<Resource<List<String>>> {
        return foodRepository.getCategories()
    }
}
