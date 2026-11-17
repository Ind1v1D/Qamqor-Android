package com.example.qamqorapp.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qamqorapp.data.AuthClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SetupUiState(
    val username: String = "",
    val password: String = "",
    val usernameError: Boolean = false,
    val passwordError: Boolean = false,
    val isSaving: Boolean = false,
    val error: Boolean = false,
    val done: Boolean = false,
)

class SetupViewModel(
    private val auth: AuthClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) = _uiState.update {
        it.copy(username = value, usernameError = false, error = false)
    }

    fun onPasswordChange(value: String) = _uiState.update {
        it.copy(password = value, passwordError = false, error = false)
    }

    fun submit() {
        val state = _uiState.value
        val usernameOk = state.username.isNotBlank()
        val passwordOk = state.password.isNotBlank()

        if (!usernameOk || !passwordOk) {
            _uiState.update {
                it.copy(usernameError = !usernameOk, passwordError = !passwordOk)
            }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = false) }
        viewModelScope.launch {
            try {
                auth.login(state.username, state.password)
                _uiState.update { it.copy(isSaving = false, done = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = true) }
            }
        }
    }

    fun consumeDone() = _uiState.update { it.copy(done = false) }
}
