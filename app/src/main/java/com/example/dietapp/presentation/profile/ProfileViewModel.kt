package com.example.dietapp.presentation.profile

import com.example.dietapp.domain.model.User
import com.example.dietapp.domain.usecase.GetUserProfileUseCase
import com.example.dietapp.domain.usecase.SaveUserProfileUseCase
import com.example.dietapp.domain.util.Resource
import com.example.dietapp.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false,
    val editName: String = "",
    val editAge: String = "",
    val editHeightCm: String = "",
    val editWeightKg: String = "",
    val editTargetCalories: String = "",
    val editGender: String = "male",
    val editActivityLevel: String = "sedentary",
    val editDietaryPreference: String = "vegetarian",
    val editGoalType: String = "maintain"
)

sealed class ProfileEvent {
    data class ShowError(val message: String) : ProfileEvent()
    data object ProfileSaved : ProfileEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val saveUserProfileUseCase: SaveUserProfileUseCase
) : BaseViewModel<ProfileUiState, ProfileEvent>(ProfileUiState()) {

    init { loadProfile() }

    private fun loadProfile() {
        launchSafe(
            onError = { updateState { copy(isLoading = false, error = it.message) } }
        ) {
            getUserProfileUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val u = result.data
                        updateState {
                            copy(
                                user = u, isLoading = false, error = null,
                                editName = u.name,
                                editAge = u.age.toString(),
                                editHeightCm = u.heightCm.toString(),
                                editWeightKg = u.weightKg.toString(),
                                editTargetCalories = u.targetCalories.toString(),
                                editGender = u.gender,
                                editActivityLevel = u.activityLevel,
                                editDietaryPreference = u.dietaryPreference,
                                editGoalType = u.goalType
                            )
                        }
                    }
                    is Resource.Error -> updateState { copy(isLoading = false, error = result.message) }
                    is Resource.Loading -> updateState { copy(isLoading = true) }
                }
            }
        }
    }

    fun saveProfile() {
        val user = currentState.user ?: return
        val updated = user.copy(
            name = currentState.editName,
            age = currentState.editAge.toIntOrNull() ?: user.age,
            heightCm = currentState.editHeightCm.toFloatOrNull() ?: user.heightCm,
            weightKg = currentState.editWeightKg.toFloatOrNull() ?: user.weightKg,
            targetCalories = currentState.editTargetCalories.toIntOrNull() ?: user.targetCalories,
            gender = currentState.editGender,
            activityLevel = currentState.editActivityLevel,
            dietaryPreference = currentState.editDietaryPreference,
            goalType = currentState.editGoalType
        )
        updateState { copy(isSaving = true) }
        launchSafe(
            onError = { updateState { copy(isSaving = false, error = it.message) } }
        ) {
            val result = saveUserProfileUseCase(updated)
            when (result) {
                is Resource.Success -> {
                    updateState { copy(isSaving = false, isEditing = false, user = updated) }
                    sendEvent(ProfileEvent.ProfileSaved)
                }
                is Resource.Error -> updateState { copy(isSaving = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun toggleEditing() = updateState { copy(isEditing = !isEditing) }
    fun onNameChanged(v: String) = updateState { copy(editName = v) }
    fun onAgeChanged(v: String) = updateState { copy(editAge = v) }
    fun onHeightChanged(v: String) = updateState { copy(editHeightCm = v) }
    fun onWeightChanged(v: String) = updateState { copy(editWeightKg = v) }
    fun onTargetCaloriesChanged(v: String) = updateState { copy(editTargetCalories = v) }
    fun onGenderChanged(v: String) = updateState { copy(editGender = v) }
    fun onActivityLevelChanged(v: String) = updateState { copy(editActivityLevel = v) }
    fun onDietaryPrefChanged(v: String) = updateState { copy(editDietaryPreference = v) }
    fun onGoalChanged(v: String) = updateState { copy(editGoalType = v) }
    fun dismissError() = updateState { copy(error = null) }
}
