package com.example.qamqorapp.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qamqorapp.data.AuthClient
import com.example.qamqorapp.data.PushTokenRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SetupUiState(
    val email: String = "",
    val password: String = "",
    val emailError: Boolean = false,
    val passwordError: Boolean = false,
    val isSaving: Boolean = false,
    val error: AuthError? = null,
    val done: Boolean = false,
)

class SetupViewModel(
    private val auth: AuthClient,
    private val pushTokens: PushTokenRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update {
        it.copy(email = value, emailError = false, error = null)
    }

    fun onPasswordChange(value: String) = _uiState.update {
        it.copy(password = value, passwordError = false, error = null)
    }

    fun submit() = authenticate { email, password -> auth.login(email, password) }
    fun register() = authenticate { email, password -> auth.register(email, password) }

    private fun authenticate(action: suspend (email: String, password: String) -> Unit) {
        val state = _uiState.value
        val emailOk = state.email.isNotBlank() && '@' in state.email
        val passwordOk = state.password.isNotBlank()

        if (!emailOk || !passwordOk) {
            _uiState.update {
                it.copy(emailError = !emailOk, passwordError = !passwordOk)
            }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                action(state.email, state.password)
                pushTokens.syncToken()
                _uiState.update { it.copy(isSaving = false, done = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.toAuthError()) }
            }
        }
    }

    fun consumeDone() = _uiState.update { it.copy(done = false) }
}
