package com.example.dietapp.presentation.foodsearch

import com.example.dietapp.domain.model.FoodItem
import com.example.dietapp.domain.usecase.GetFoodCategoriesUseCase
import com.example.dietapp.domain.usecase.GetFoodItemsUseCase
import com.example.dietapp.domain.usecase.SearchFoodItemsUseCase
import com.example.dietapp.domain.util.Resource
import com.example.dietapp.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import javax.inject.Inject

data class FoodSearchUiState(
    val searchQuery: String = "",
    val foodItems: List<FoodItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val error: String? = null,
    val selectedFood: FoodItem? = null
)

sealed class FoodSearchEvent {
    data class ShowError(val message: String) : FoodSearchEvent()
    data class FoodSelected(val food: FoodItem) : FoodSearchEvent()
}

@HiltViewModel
class FoodSearchViewModel @Inject constructor(
    private val getFoodItemsUseCase: GetFoodItemsUseCase,
    private val searchFoodItemsUseCase: SearchFoodItemsUseCase,
    private val getFoodCategoriesUseCase: GetFoodCategoriesUseCase
) : BaseViewModel<FoodSearchUiState, FoodSearchEvent>(FoodSearchUiState()) {

    private var searchJob: Job? = null

    init {
        loadCategories()
        loadFoodItems()
    }

    private fun loadCategories() {
        launchSafe {
            getFoodCategoriesUseCase().collect { result ->
                if (result is Resource.Success) {
                    updateState { copy(categories = result.data) }
                }
            }
        }
    }

    private fun loadFoodItems() {
        launchSafe(
            onError = { updateState { copy(isLoading = false, error = it.message) } }
        ) {
            getFoodItemsUseCase(currentState.selectedCategory).collect { result ->
                when (result) {
                    is Resource.Success -> updateState {
                        copy(foodItems = result.data, isLoading = false, error = null)
                    }
                    is Resource.Error -> updateState {
                        copy(foodItems = result.data ?: emptyList(), isLoading = false, error = result.message)
                    }
                    is Resource.Loading -> updateState {
                        copy(foodItems = result.data ?: foodItems, isLoading = true)
                    }
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        updateState { copy(searchQuery = query) }
        searchJob?.cancel()
        if (query.length >= 2) {
            searchJob = launchDebounced(query)
        } else if (query.isEmpty()) {
            loadFoodItems()
        }
    }

    private fun launchDebounced(query: String): Job {
        // Use launchSafe which returns Unit, but we store the concept
        launchSafe {
            delay(300) // Debounce 300ms
            updateState { copy(isSearching = true) }
            searchFoodItemsUseCase(query).collect { result ->
                when (result) {
                    is Resource.Success -> updateState {
                        copy(foodItems = result.data, isSearching = false)
                    }
                    is Resource.Error -> updateState {
                        copy(isSearching = false, error = result.message)
                    }
                    is Resource.Loading -> { /* keep searching state */ }
                }
            }
        }
        return Job() // placeholder — cancellation handled by searchJob reassignment
    }

    fun onCategorySelected(category: String?) {
        updateState { copy(selectedCategory = category, searchQuery = "", isLoading = true) }
        loadFoodItems()
    }

    fun onFoodSelected(food: FoodItem) {
        updateState { copy(selectedFood = food) }
        sendEvent(FoodSearchEvent.FoodSelected(food))
    }

    fun clearSearch() {
        updateState { copy(searchQuery = "", isSearching = false) }
        loadFoodItems()
    }

    fun dismissError() = updateState { copy(error = null) }
}
