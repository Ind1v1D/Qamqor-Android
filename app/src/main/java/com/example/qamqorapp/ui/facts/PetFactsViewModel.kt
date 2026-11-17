package com.example.qamqorapp.ui.facts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qamqorapp.data.SettingsRepository
import com.example.qamqorapp.domain.model.PetFact
import com.example.qamqorapp.domain.repository.PetFactsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
sealed interface PetFactsUiState {
    data object Loading : PetFactsUiState
    data class Success(val facts: List<PetFact>) : PetFactsUiState
    data class Error(val message: String) : PetFactsUiState
}

class PetFactsViewModel(
    private val repository: PetFactsRepository,
    private val settings: SettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<PetFactsUiState>(PetFactsUiState.Loading)
    val uiState: StateFlow<PetFactsUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            settings.language.collect { language ->
                loadFactsFor(language)
            }
        }
    }

    fun loadFacts() {
        loadFactsFor(settings.language.value)
    }

    private fun loadFactsFor(language: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = PetFactsUiState.Loading

            try {
                val facts = repository.getFacts(language)
                _uiState.value = PetFactsUiState.Success(facts)
            } catch (e: Exception) {
                _uiState.value = PetFactsUiState.Error(e.message ?: "Unknown error, No Internet Connection")
            }
        }
    }
}
