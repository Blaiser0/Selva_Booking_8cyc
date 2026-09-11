package com.company.selvabooking.viewmodel

import android.app.Application
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

data class AdminAdministradoresUiState(
    val isLoading: Boolean = true,
    val accessDenied: Boolean = false,
    val administradores: List<User> = emptyList(),
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isSaving: Boolean = false,
    val editingAdmin: User? = null,
    val editNombre: String = "",
    val editNombreError: String? = null,
    val isUpdatingAdmin: Boolean = false,
    val isSendingPasswordReset: Boolean = false,
    val deletingAdminId: String? = null,
    val message: String? = null,
    val error: String? = null
)

class AdminAdministradoresViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository: AuthRepository =
        (application as SelvaBookingApplication).authRepository

    private val _uiState = MutableStateFlow(AdminAdministradoresUiState())
    val uiState: StateFlow<AdminAdministradoresUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = authRepository.getCurrentUserData().getOrNull()
            if (user?.rol != UserRole.SUPER_ADMIN) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        accessDenied = true,
                        error = "Solo SuperAdmin puede gestionar administradores"
                    )
                }
                return@launch
            }
            authRepository.getAdministradoresFlow().collect { administradores ->
                _uiState.update { it.copy(isLoading = false, administradores = administradores) }
            }
        }
    }

    fun updateEmail(value: String) = _uiState.update { it.copy(email = value, emailError = null) }
    fun updatePassword(value: String) = _uiState.update { it.copy(password = value, passwordError = null) }
    fun updateConfirmPassword(value: String) =
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null) }

    fun openEditAdmin(admin: User) {
        _uiState.update {
            it.copy(
                editingAdmin = admin,
                editNombre = admin.nombre,
                editNombreError = null,
                error = null,
                message = null
            )
        }
    }

    fun closeEditAdmin() {
        _uiState.update {
            it.copy(
                editingAdmin = null,
                editNombre = "",
                editNombreError = null,
                isUpdatingAdmin = false,
                isSendingPasswordReset = false
            )
        }
    }

    fun updateEditNombre(value: String) =
        _uiState.update { it.copy(editNombre = value, editNombreError = null) }

    fun saveAdministradorChanges() {
        val admin = _uiState.value.editingAdmin ?: return
        val nombreError = if (!ValidationUtils.isValidName(_uiState.value.editNombre.trim())) {
            "Nombre muy corto"
        } else {
            null
        }
        if (nombreError != null) {
            _uiState.update { it.copy(editNombreError = nombreError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingAdmin = true, error = null, message = null) }
            authRepository.updateAdministrador(
                adminId = admin.id,
                nombre = _uiState.value.editNombre.trim()
            ).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isUpdatingAdmin = false,
                            editingAdmin = null,
                            editNombre = "",
                            message = "Administrador actualizado"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isUpdatingAdmin = false, error = e.message) }
                }
            )
        }
    }

    fun sendPasswordResetToEditingAdmin() {
        val admin = _uiState.value.editingAdmin ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSendingPasswordReset = true, error = null, message = null) }
            authRepository.sendAdministradorPasswordReset(admin.id).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSendingPasswordReset = false,
                            message = "Correo de restablecimiento enviado a ${admin.email}"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSendingPasswordReset = false, error = e.message) }
                }
            )
        }
    }

    fun createAdministrador() {
        val state = _uiState.value
        val emailError = if (!ValidationUtils.isValidEmail(state.email)) "Correo inválido" else null
        val passwordError = if (!ValidationUtils.isValidPassword(state.password)) "Mínimo 6 caracteres" else null
        val confirmError = if (!ValidationUtils.passwordsMatch(state.password, state.confirmPassword)) {
            "Las contraseñas no coinciden"
        } else {
            null
        }

        if (emailError != null || passwordError != null || confirmError != null) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, message = null) }
            authRepository.createAdministrador(
                email = state.email.trim(),
                password = state.password.trim()
            ).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            message = "Administrador creado",
                            email = "",
                            password = "",
                            confirmPassword = ""
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSaving = false, error = e.message) }
                }
            )
        }
    }

    fun deleteAdministrador(adminId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(deletingAdminId = adminId, error = null, message = null) }
            authRepository.deleteAdministrador(adminId).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            deletingAdminId = null,
                            message = "Administrador eliminado"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(deletingAdminId = null, error = e.message) }
                }
            )
        }
    }

    fun clearMessages() = _uiState.update { it.copy(message = null, error = null) }
}
