package com.company.selvabooking.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.company.selvabooking.SelvaBookingApplication
import com.company.selvabooking.domain.model.User
import com.company.selvabooking.domain.model.UserRole
import com.company.selvabooking.repository.AuthRepository
import com.company.selvabooking.repository.HotelRepository
import com.company.selvabooking.utils.AdminDataScope
import com.company.selvabooking.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GerenteListItem(
    val user: User,
    val hotelsCount: Int
)

data class AdminGerentesUiState(
    val isLoading: Boolean = true,
    val gerentes: List<GerenteListItem> = emptyList(),
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isSaving: Boolean = false,
    val deletingGerenteId: String? = null,
    val message: String? = null,
    val error: String? = null
)

class AdminGerentesViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository: AuthRepository =
        (application as SelvaBookingApplication).authRepository
    private val hotelRepository: HotelRepository =
        (application as SelvaBookingApplication).hotelRepository

    private var currentAdminId: String = ""
    private var isLimitedAdmin: Boolean = false

    private val _uiState = MutableStateFlow(AdminGerentesUiState())
    val uiState: StateFlow<AdminGerentesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = authRepository.getCurrentUserData().getOrNull()
            currentAdminId = user?.id.orEmpty()
            isLimitedAdmin = user?.rol == UserRole.ADMINISTRADOR
            combine(
                authRepository.getGerentesHotelFlow(),
                hotelRepository.getHotelsFlow()
            ) { gerentes, hotels ->
                val visibleGerentes = if (isLimitedAdmin) {
                    AdminDataScope.gerentesCreatedBy(currentAdminId, gerentes)
                } else {
                    gerentes
                }
                visibleGerentes.map { gerente ->
                    GerenteListItem(
                        user = gerente,
                        hotelsCount = hotels.count { it.propietarioId == gerente.id }
                    )
                }
            }.collect { gerentes ->
                _uiState.update { it.copy(isLoading = false, gerentes = gerentes) }
            }
        }
    }

    fun updateEmail(value: String) = _uiState.update { it.copy(email = value, emailError = null) }
    fun updatePassword(value: String) = _uiState.update { it.copy(password = value, passwordError = null) }
    fun updateConfirmPassword(value: String) =
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null) }

    fun createGerente() {
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
            authRepository.createGerenteHotel(
                email = state.email.trim(),
                password = state.password.trim()
            ).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            message = "Encargado del hotel creado",
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

    fun deleteGerente(gerenteId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(deletingGerenteId = gerenteId, error = null, message = null) }
            authRepository.deleteGerenteHotel(gerenteId).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            deletingGerenteId = null,
                            message = "Encargado eliminado junto con sus hoteles y habitaciones"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(deletingGerenteId = null, error = e.message) }
                }
            )
        }
    }

    fun clearMessages() = _uiState.update { it.copy(message = null, error = null) }
}
