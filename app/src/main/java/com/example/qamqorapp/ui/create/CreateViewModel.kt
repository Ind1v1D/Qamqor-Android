package com.example.qamqorapp.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qamqorapp.data.District
import com.example.qamqorapp.data.PostRepository
import com.example.qamqorapp.data.PostStatus
import com.example.qamqorapp.data.Species
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateUiState(
    val status: PostStatus = PostStatus.LOST,
    val species: Species = Species.DOG,
    val emoji: String = "🐕",
    val name: String = "",
    val breed: String = "",
    val district: District? = null,
    val address: String = "",
    val marks: String = "",
    val phone: String = "",
    val description: String = "",

    val nameError: Boolean = false,
    val phoneError: Boolean = false,
    val districtError: Boolean = false,

    val isSaving: Boolean = false,
    /** Set once the row is in Room; the screen pops itself off the back stack. */
    val published: Boolean = false,
)

/**
 * «Новое объявление» screen state.
 *
 * The concept's dashed «Добавить фото» box became an emoji picker (no photo
 * pipeline in this build), and the district is a real dropdown over [District]
 * instead of free text — which is what makes the feed's district chips possible.
 */
class CreateViewModel(
    private val repository: PostRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateUiState())
    val uiState: StateFlow<CreateUiState> = _uiState.asStateFlow()

    fun selectStatus(value: PostStatus) = _uiState.update { it.copy(status = value) }
    fun selectSpecies(value: Species) = _uiState.update { it.copy(species = value) }
    fun selectEmoji(value: String) = _uiState.update { it.copy(emoji = value) }
    fun selectDistrict(value: District) = _uiState.update {
        it.copy(district = value, districtError = false)
    }

    fun onNameChange(value: String) = _uiState.update {
        it.copy(name = value, nameError = false)
    }

    fun onBreedChange(value: String) = _uiState.update { it.copy(breed = value) }
    fun onAddressChange(value: String) = _uiState.update { it.copy(address = value) }
    fun onMarksChange(value: String) = _uiState.update { it.copy(marks = value) }

    fun onPhoneChange(value: String) = _uiState.update {
        it.copy(phone = value, phoneError = false)
    }

    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }

    fun submit() {
        val state = _uiState.value

        val nameOk = state.name.isNotBlank()
        val phoneOk = state.phone.filter { it.isDigit() }.length >= 10
        val districtOk = state.district != null

        if (!nameOk || !phoneOk || !districtOk) {
            _uiState.update {
                it.copy(nameError = !nameOk, phoneError = !phoneOk, districtError = !districtOk)
            }
            return
        }

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val author = repository.currentProfile()?.name.orEmpty()
            repository.publish(
                status = state.status,
                species = state.species,
                name = state.name.trim(),
                district = state.district ?: District.ALMALINSKY,
                address = state.address.trim(),
                marks = state.marks.trim(),
                description = state.description.trim(),
                emoji = state.emoji,
                phone = state.phone.trim(),
                breed = state.breed.trim(),
                author = author,
            )
            _uiState.update { it.copy(isSaving = false, published = true) }
        }
    }

    /** Called when the snackbar shown after publishing has been dismissed. */
    fun consumePublished() = _uiState.update { it.copy(published = false) }

    /** Prefills the author's own phone from the profile so it is not typed twice. */
    fun prefillFromProfile() {
        viewModelScope.launch {
            val profile = repository.currentProfile() ?: return@launch
            _uiState.update { state ->
                if (state.phone.isBlank()) state.copy(phone = profile.phone) else state
            }
        }
    }
}
