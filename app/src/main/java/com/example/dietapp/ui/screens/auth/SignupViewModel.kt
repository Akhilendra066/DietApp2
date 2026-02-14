package com.example.dietapp.ui.screens.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SignupUiState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSignupSuccess: Boolean = false
)

@HiltViewModel
class SignupViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) = _uiState.update { it.copy(fullName = name, error = null) }
    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email, error = null) }
    fun onPhoneChange(phone: String) = _uiState.update { it.copy(phone = phone, error = null) }
    fun onPasswordChange(pw: String) = _uiState.update { it.copy(password = pw, error = null) }
    fun onConfirmPasswordChange(pw: String) = _uiState.update { it.copy(confirmPassword = pw, error = null) }

    fun onSignup() {
        val s = _uiState.value
        when {
            s.fullName.isBlank() || s.email.isBlank() || s.password.isBlank() ->
                _uiState.update { it.copy(error = "Please fill all required fields") }
            s.password.length < 6 ->
                _uiState.update { it.copy(error = "Password must be at least 6 characters") }
            s.password != s.confirmPassword ->
                _uiState.update { it.copy(error = "Passwords do not match") }
            else -> {
                _uiState.update { it.copy(isLoading = true, error = null) }
                // TODO: Call auth repository
                _uiState.update { it.copy(isLoading = false, isSignupSuccess = true) }
            }
        }
    }
}
