package com.company.selvabooking.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.company.selvabooking.SelvaBookingApplication
import com.company.selvabooking.domain.model.User
import com.company.selvabooking.domain.model.UserRole
import com.company.selvabooking.repository.AuthRepository
import com.company.selvabooking.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val nombre: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val showAdminRequestDialog: Boolean = false,
    val showSwitchToClientDialog: Boolean = false,
    val showSwitchToAdminDialog: Boolean = false,
    val nombreError: String? = null,
    val successMessage: String? = null,
    val error: String? = null
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository: AuthRepository =
        (application as SelvaBookingApplication).authRepository

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile(onSynced: ((User) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.getCurrentUserData().fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = user,
                            nombre = user.nombre
                        )
                    }
                    onSynced?.invoke(user)
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "No se pudo cargar el perfil"
                        )
                    }
                }
            )
        }
    }

    fun startEditing() {
        val user = _uiState.value.user ?: return
        _uiState.update {
            it.copy(
                isEditing = true,
                nombre = user.nombre,
                nombreError = null,
                successMessage = null,
                error = null
            )
        }
    }

    fun cancelEditing() {
        val user = _uiState.value.user ?: return
        _uiState.update {
            it.copy(
                isEditing = false,
                nombre = user.nombre,
                nombreError = null
            )
        }
    }

    fun updateNombre(value: String) {
        _uiState.update { it.copy(nombre = value, nombreError = null) }
    }

    fun saveProfile(onSaved: (User) -> Unit) {
        val state = _uiState.value
        val user = state.user ?: return
        val nombreError = if (!ValidationUtils.isValidName(state.nombre)) "Nombre muy corto" else null

        if (nombreError != null) {
            _uiState.update { it.copy(nombreError = nombreError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, successMessage = null) }
            val updatedUser = user.copy(nombre = state.nombre.trim())
            authRepository.updateUserProfile(updatedUser).fold(
                onSuccess = { savedUser ->
                    val resolvedUser = authRepository.getCurrentUserData().getOrElse { savedUser }
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isEditing = false,
                            user = resolvedUser,
                            nombre = resolvedUser.nombre,
                            successMessage = "Perfil actualizado"
                        )
                    }
                    onSaved(resolvedUser)
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = e.message ?: "No se pudo guardar el perfil"
                        )
                    }
                }
            )
        }
    }

    fun uploadProfilePhoto(uri: Uri, onSaved: (User) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true, error = null, successMessage = null) }
            authRepository.uploadProfilePhoto(uri).fold(
                onSuccess = { updatedUser ->
                    _uiState.update {
                        it.copy(
                            isUploadingPhoto = false,
                            user = updatedUser,
                            successMessage = "Foto de perfil actualizada"
                        )
                    }
                    onSaved(updatedUser)
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isUploadingPhoto = false,
                            error = e.message ?: "No se pudo subir la foto"
                        )
                    }
                }
            )
        }
    }

    fun onAccountTypeTripleTap() {
        val user = _uiState.value.user ?: return
        when {
            user.rol == UserRole.ADMINISTRADOR -> {
                _uiState.update { it.copy(showSwitchToClientDialog = true) }
            }
            user.puedeAlternarRol -> {
                _uiState.update { it.copy(showSwitchToAdminDialog = true) }
            }
            user.hasPendingAdminRequest -> {
                _uiState.update {
                    it.copy(successMessage = "Tu solicitud de administrador ya está pendiente")
                }
            }
            user.hasRejectedAdminRequest -> {
                _uiState.update {
                    it.copy(error = "Tu solicitud de administrador fue rechazada")
                }
            }
            else -> {
                _uiState.update { it.copy(showAdminRequestDialog = true) }
            }
        }
    }

    fun dismissAdminRequestDialog() {
        _uiState.update { it.copy(showAdminRequestDialog = false) }
    }

    fun dismissSwitchToClientDialog() {
        _uiState.update { it.copy(showSwitchToClientDialog = false) }
    }

    fun dismissSwitchToAdminDialog() {
        _uiState.update { it.copy(showSwitchToAdminDialog = false) }
    }

    fun confirmSwitchToClientRole(onSaved: (User) -> Unit) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showSwitchToClientDialog = false,
                    isSaving = true,
                    error = null,
                    successMessage = null
                )
            }
            authRepository.switchToClientRole().fold(
                onSuccess = { updatedUser ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            user = updatedUser,
                            successMessage = "Modo cliente activado"
                        )
                    }
                    onSaved(updatedUser)
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isSaving = false, error = e.message ?: "No se pudo cambiar el rol")
                    }
                }
            )
        }
    }

    fun confirmSwitchToAdminRole(onSaved: (User) -> Unit) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showSwitchToAdminDialog = false,
                    isSaving = true,
                    error = null,
                    successMessage = null
                )
            }
            authRepository.switchToAdminRole().fold(
                onSuccess = { updatedUser ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            user = updatedUser,
                            successMessage = "Modo administrador activado"
                        )
                    }
                    onSaved(updatedUser)
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isSaving = false, error = e.message ?: "No se pudo cambiar el rol")
                    }
                }
            )
        }
    }

    fun confirmAdminAccessRequest(onSaved: (User) -> Unit) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showAdminRequestDialog = false,
                    isSaving = true,
                    error = null,
                    successMessage = null
                )
            }
            authRepository.requestAdminAccess().fold(
                onSuccess = { updatedUser ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            user = updatedUser,
                            successMessage = "Solicitud enviada. Un administrador revisará tu petición."
                        )
                    }
                    onSaved(updatedUser)
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = e.message ?: "No se pudo enviar la solicitud"
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, error = null) }
    }
}
