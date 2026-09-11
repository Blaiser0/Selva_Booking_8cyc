package com.company.selvabooking.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.company.selvabooking.SelvaBookingApplication
import com.company.selvabooking.domain.model.AuditAction
import com.company.selvabooking.domain.model.AuditEntityType
import com.company.selvabooking.domain.model.AuditLog
import com.company.selvabooking.domain.model.UserRole
import com.company.selvabooking.repository.AuditRepository
import com.company.selvabooking.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminAuditUiState(
    val isLoading: Boolean = true,
    val logs: List<AuditLog> = emptyList(),
    val encargadoFilter: String = "",
    val actionFilter: AuditAction? = null,
    val entityFilter: AuditEntityType? = null,
    val showOnlyPending: Boolean = false,
    val revertingLogId: String? = null,
    val message: String? = null,
    val error: String? = null
) {
    val encargadoOptions: List<String>
        get() = logs.map { it.actorName.ifBlank { it.actorEmail } }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

    val filteredLogs: List<AuditLog>
        get() = logs.filter { log ->
            val matchesEncargado = encargadoFilter.isBlank() ||
                log.actorName == encargadoFilter ||
                log.actorEmail == encargadoFilter
            val matchesAction = actionFilter == null || log.action == actionFilter
            val matchesEntity = entityFilter == null || log.entityType == entityFilter
            val matchesPending = !showOnlyPending || !log.reverted
            matchesEncargado && matchesAction && matchesEntity && matchesPending
        }
}

class AdminAuditViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository: AuthRepository =
        (application as SelvaBookingApplication).authRepository
    private val auditRepository: AuditRepository =
        (application as SelvaBookingApplication).auditRepository

    private var adminUserId: String = ""

    private val _uiState = MutableStateFlow(AdminAuditUiState())
    val uiState: StateFlow<AdminAuditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = authRepository.getCurrentUserData().getOrNull()
            if (user?.rol != UserRole.SUPER_ADMIN) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Solo SuperAdmin puede ver el registro de auditoría")
                }
                return@launch
            }
            adminUserId = user.id
            auditRepository.getAuditLogsFlow().collect { logs ->
                _uiState.update { it.copy(isLoading = false, logs = logs) }
            }
        }
    }

    fun setEncargadoFilter(value: String) = _uiState.update { it.copy(encargadoFilter = value) }
    fun setActionFilter(value: AuditAction?) = _uiState.update { it.copy(actionFilter = value) }
    fun setEntityFilter(value: AuditEntityType?) = _uiState.update { it.copy(entityFilter = value) }
    fun setShowOnlyPending(value: Boolean) = _uiState.update { it.copy(showOnlyPending = value) }

    fun revertChange(logId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(revertingLogId = logId, error = null, message = null) }
            auditRepository.revertChange(logId, adminUserId).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            revertingLogId = null,
                            message = "Cambio revertido correctamente"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(revertingLogId = null, error = e.message) }
                }
            )
        }
    }

    fun clearMessages() = _uiState.update { it.copy(message = null, error = null) }
}
