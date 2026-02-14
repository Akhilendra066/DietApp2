package com.example.dietapp.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Base ViewModel with common state management patterns.
 * Uses StateFlow for UI state and Channel for one-time events.
 */
abstract class BaseViewModel<State, Event>(initialState: State) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    private val _events = Channel<Event>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    protected val currentState: State get() = _uiState.value

    protected fun updateState(reducer: State.() -> State) {
        _uiState.value = currentState.reducer()
    }

    protected fun sendEvent(event: Event) {
        viewModelScope.launch {
            _events.send(event)
        }
    }

    protected fun launchSafe(
        onError: ((Throwable) -> Unit)? = null,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                Timber.e(e, "Error in ${this@BaseViewModel::class.simpleName}")
                onError?.invoke(e)
            }
        }
    }
}
