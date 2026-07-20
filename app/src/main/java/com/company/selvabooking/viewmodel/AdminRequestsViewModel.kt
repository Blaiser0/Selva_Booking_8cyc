package com.company.selvabooking.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.company.selvabooking.SelvaBookingApplication
import com.company.selvabooking.domain.model.User
import com.company.selvabooking.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminRequestsUiState(
    val isLoading: Boolean = true,
    val requests: List<User> = emptyList(),
    val processingUserId: String? = null,
    val message: String? = null,
    val error: String? = null
)

class AdminRequestsViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository: AuthRepository =
        (application as SelvaBookingApplication).authRepository

    private val _uiState = MutableStateFlow(AdminRequestsUiState())
    val uiState: StateFlow<AdminRequestsUiState> = _uiState.asStateFlow()

    init {
        loadRequests()
    }

    private fun loadRequests() {
        viewModelScope.launch {
            authRepository.getPendingAdminRequestsFlow().collect { requests ->
                _uiState.update {
                    it.copy(isLoading = false, requests = requests)
                }
            }
        }
    }

    fun approveRequest(userId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(processingUserId = userId, error = null, message = null)
            }
            authRepository.approveAdminRequest(userId).fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            processingUserId = null,
                            message = "Solicitud de ${user.nombre} aprobada"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            processingUserId = null,
                            error = e.message ?: "No se pudo aprobar la solicitud"
                        )
                    }
                }
            )
        }
    }

    fun rejectRequest(userId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(processingUserId = userId, error = null, message = null)
            }
            authRepository.rejectAdminRequest(userId).fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            processingUserId = null,
                            message = "Solicitud de ${user.nombre} rechazada"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            processingUserId = null,
                            error = e.message ?: "No se pudo rechazar la solicitud"
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}
