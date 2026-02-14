package com.example.dietapp.ui.screens.chat

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ChatMessage(val text: String, val isUser: Boolean, val timestamp: Long = System.currentTimeMillis())

data class ChatUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage("Hi! I'm your AI diet assistant 🥗. Ask me anything about nutrition, recipes, or diet tips!", isUser = false)
    ),
    val inputText: String = "",
    val isLoading: Boolean = false
)

@HiltViewModel
class AIChatViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun onInputChange(text: String) = _uiState.update { it.copy(inputText = text) }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return

        _uiState.update {
            it.copy(
                messages = it.messages + ChatMessage(text, isUser = true),
                inputText = "",
                isLoading = true
            )
        }

        // TODO: Call AI service
        val reply = "That's a great question! Based on your diet profile, I'd recommend focusing on protein-rich foods like paneer, dal, and eggs. Aim for 1.6g protein per kg of body weight daily. Would you like me to adjust your meal plan?"
        _uiState.update {
            it.copy(
                messages = it.messages + ChatMessage(reply, isUser = false),
                isLoading = false
            )
        }
    }
}
