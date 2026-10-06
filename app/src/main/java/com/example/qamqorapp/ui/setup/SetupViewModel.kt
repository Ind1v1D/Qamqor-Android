package com.example.qamqorapp.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qamqorapp.data.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SetupUiState(
    val name: String = "",
    val phone: String = "",
    val nameError: Boolean = false,
    val phoneError: Boolean = false,
    val isSaving: Boolean = false,
    /** Flips to true once the profile is stored; the shell reacts by moving to the feed. */
    val done: Boolean = false,
)

/**
 * First-run setup — the closest thing this app has to login.
 *
 * There is no backend, so "auth" is just writing one row to the `profile` table.
 * The row's existence is what the app shell checks to pick its start destination,
 * and clearing it («Выйти») is what signing out means here.
 */
class SetupViewModel(
    private val repository: PostRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update {
        it.copy(name = value, nameError = false)
    }

    fun onPhoneChange(value: String) = _uiState.update {
        it.copy(phone = value, phoneError = false)
    }

    /**
     * Deliberately permissive phone check: the mockups use `+7 707 000 00 00`, and
     * users paste numbers with dashes, spaces and parentheses. We only reject
     * strings that clearly are not phone numbers.
     */
    private fun String.looksLikePhone(): Boolean = this.filter { it.isDigit() }.length >= 10

    fun submit() {
        val state = _uiState.value
        val nameOk = state.name.isNotBlank()
        val phoneOk = state.phone.looksLikePhone()

        if (!nameOk || !phoneOk) {
            _uiState.update { it.copy(nameError = !nameOk, phoneError = !phoneOk) }
            return
        }

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            repository.saveProfile(state.name.trim(), state.phone.trim())
            _uiState.update { it.copy(isSaving = false, done = true) }
        }
    }

    /** Called by the shell when the `done` event has been consumed. */
    fun consumeDone() = _uiState.update { it.copy(done = false) }
}
