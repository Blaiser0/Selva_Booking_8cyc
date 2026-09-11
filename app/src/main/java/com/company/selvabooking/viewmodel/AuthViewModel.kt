package com.company.selvabooking.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.company.selvabooking.SelvaBookingApplication
import com.company.selvabooking.domain.model.User
import com.company.selvabooking.domain.model.UserRole
import com.company.selvabooking.navigation.PendingAuthAction
import com.company.selvabooking.repository.AuthRepository
import com.company.selvabooking.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSessionReady: Boolean = false,
    val currentUser: User? = null,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nombreError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isResetLoading: Boolean = false,
    val isResetEmailSent: Boolean = false,
    val resetError: String? = null,
    val termsAccepted: Boolean = false,
    val termsViewed: Boolean = false,
    val termsError: String? = null,
    val showTermsDialog: Boolean = false,
    val telefono: String = "",
    val telefonoError: String? = null,
    val showGerenteProfileForm: Boolean = false,
    val isCompletingProfile: Boolean = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository: AuthRepository =
        (application as SelvaBookingApplication).authRepository

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var pendingAuthAction: PendingAuthAction? = null

    fun setPendingAuthAction(action: PendingAuthAction?) {
        pendingAuthAction = action
    }

    fun consumePendingAuthAction(): PendingAuthAction? =
        pendingAuthAction.also { pendingAuthAction = null }

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            if (authRepository.isLoggedIn) {
                authRepository.getCurrentUserData().fold(
                    onSuccess = { user ->
                        _uiState.update {
                            it.copy(
                                currentUser = user,
                                isAuthenticated = true,
                                isSessionReady = true
                            )
                        }
                        if (user.rol == UserRole.SUPER_ADMIN) {
                            authRepository.seedSampleDataIfNeeded()
                        }
                    },
                    onFailure = {
                        authRepository.logout()
                        _uiState.update {
                            it.copy(
                                currentUser = null,
                                isAuthenticated = false,
                                isSessionReady = true
                            )
                        }
                    }
                )
            } else {
                _uiState.update {
                    it.copy(
                        currentUser = null,
                        isAuthenticated = false,
                        isSessionReady = true
                    )
                }
            }
        }
    }

    fun updateNombre(value: String) = _uiState.update {
        it.copy(
            nombre = ValidationUtils.filterPersonNameInput(value),
            nombreError = null
        )
    }
    fun updateEmail(value: String) = _uiState.update { it.copy(email = value, emailError = null) }
    fun updatePassword(value: String) = _uiState.update { it.copy(password = value, passwordError = null) }
    fun updateConfirmPassword(value: String) = _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null) }
    fun updateTelefono(value: String) = _uiState.update {
        it.copy(
            telefono = ValidationUtils.filterPhoneInput(value),
            telefonoError = null
        )
    }
    fun showGerenteProfileForm() = _uiState.update { it.copy(showGerenteProfileForm = true) }
    fun hideGerenteProfileForm() = _uiState.update { it.copy(showGerenteProfileForm = false) }

    fun initGerenteProfileForm(user: User) {
        _uiState.update {
            it.copy(
                nombre = user.nombre,
                email = user.email,
                password = "",
                confirmPassword = "",
                termsAccepted = false,
                termsViewed = false,
                showGerenteProfileForm = false
            )
        }
    }
    fun updateTermsAccepted(value: Boolean) {
        _uiState.update {
            if (!it.termsViewed && value) it
            else it.copy(termsAccepted = value, termsError = null)
        }
    }

    fun openTermsDialog() = _uiState.update { it.copy(showTermsDialog = true) }

    fun dismissTermsDialog() = _uiState.update { it.copy(showTermsDialog = false) }

    fun acceptTermsFromDialog() = _uiState.update {
        it.copy(
            showTermsDialog = false,
            termsViewed = true,
            termsAccepted = true,
            termsError = null
        )
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun clearResetState() = _uiState.update {
        it.copy(isResetEmailSent = false, resetError = null, isResetLoading = false)
    }

    fun login() {
        val state = _uiState.value
        var hasError = false

        val emailError = if (!ValidationUtils.isValidEmail(state.email)) "Correo inválido" else null
        val passwordError = if (!ValidationUtils.isValidPassword(state.password)) "Mínimo 6 caracteres" else null

        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(emailError = emailError, passwordError = passwordError)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.login(
                email = state.email.trim(),
                password = state.password.trim()
            ).fold(
                onSuccess = { user ->
                    if (user.rol == UserRole.SUPER_ADMIN) {
                        authRepository.seedSampleDataIfNeeded()
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentUser = user,
                            isAuthenticated = true
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Error al iniciar sesión"
                        )
                    }
                }
            )
        }
    }

    fun register() {
        val state = _uiState.value
        val nombreError = when {
            state.nombre.trim().length < 3 -> "Mínimo 3 caracteres"
            !ValidationUtils.isValidName(state.nombre) -> "Solo letras y espacios"
            else -> null
        }
        val emailError = if (!ValidationUtils.isValidEmail(state.email)) "Correo inválido" else null
        val telefonoError = if (!ValidationUtils.isValidPhone(state.telefono)) {
            "Ingrese 9 dígitos numéricos"
        } else {
            null
        }
        val passwordError = if (!ValidationUtils.isValidPassword(state.password)) "Mínimo 6 caracteres" else null
        val confirmError = if (!ValidationUtils.passwordsMatch(state.password, state.confirmPassword))
            "Las contraseñas no coinciden" else null
        val termsError = when {
            !state.termsViewed -> "Debe leer los Términos y Condiciones antes de registrarse"
            !state.termsAccepted -> "Debe aceptar los Términos y Condiciones para crear su cuenta"
            else -> null
        }

        if (nombreError != null || emailError != null || telefonoError != null ||
            passwordError != null || confirmError != null || termsError != null
        ) {
            _uiState.update {
                it.copy(
                    nombreError = nombreError,
                    emailError = emailError,
                    telefonoError = telefonoError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmError,
                    termsError = termsError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.register(
                nombre = state.nombre.trim(),
                email = state.email.trim(),
                password = state.password.trim(),
                telefono = state.telefono.trim()
            ).fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentUser = user,
                            isAuthenticated = true
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Error al registrarse"
                        )
                    }
                }
            )
        }
    }

    fun sendPasswordResetEmail() {
        val state = _uiState.value
        val emailError = if (!ValidationUtils.isValidEmail(state.email)) "Correo inválido" else null

        if (emailError != null) {
            _uiState.update { it.copy(emailError = emailError) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isResetLoading = true,
                    resetError = null,
                    isResetEmailSent = false,
                    emailError = null
                )
            }
            authRepository.sendPasswordResetEmail(state.email.trim()).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isResetLoading = false,
                            isResetEmailSent = true
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isResetLoading = false,
                            resetError = e.message ?: "No se pudo enviar el correo de recuperación"
                        )
                    }
                }
            )
        }
    }

    fun completeGerenteProfile() {
        val state = _uiState.value
        val nombreError = if (!ValidationUtils.isValidName(state.nombre)) "Nombre muy corto" else null
        val passwordError = if (!ValidationUtils.isValidPassword(state.password)) "Mínimo 6 caracteres" else null
        val confirmError = if (!ValidationUtils.passwordsMatch(state.password, state.confirmPassword)) {
            "Las contraseñas no coinciden"
        } else {
            null
        }
        val termsError = when {
            !state.termsViewed -> "Debe leer los Términos y Condiciones"
            !state.termsAccepted -> "Debe aceptar los Términos y Condiciones"
            else -> null
        }

        if (nombreError != null || passwordError != null || confirmError != null || termsError != null) {
            _uiState.update {
                it.copy(
                    nombreError = nombreError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmError,
                    termsError = termsError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCompletingProfile = true, error = null) }
            authRepository.completeGerenteProfile(
                nombre = state.nombre.trim(),
                password = state.password.trim(),
                confirmPassword = state.confirmPassword.trim()
            ).fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isCompletingProfile = false,
                            currentUser = user,
                            isAuthenticated = true,
                            showGerenteProfileForm = false
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isCompletingProfile = false,
                            error = e.message ?: "No se pudo actualizar el perfil"
                        )
                    }
                }
            )
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.update {
            AuthUiState(isSessionReady = true)
        }
    }

    fun updateCurrentUser(user: User) {
        val wasNotSuperAdmin = _uiState.value.currentUser?.rol != UserRole.SUPER_ADMIN
        _uiState.update { it.copy(currentUser = user) }
        if (wasNotSuperAdmin && user.rol == UserRole.SUPER_ADMIN) {
            viewModelScope.launch {
                authRepository.seedSampleDataIfNeeded()
            }
        }
    }
}
