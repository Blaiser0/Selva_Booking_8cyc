package com.company.selvabooking.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.company.selvabooking.SelvaBookingApplication
import com.company.selvabooking.domain.model.User
import com.company.selvabooking.domain.model.UserRole
import com.company.selvabooking.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserRoleFilterOption(
    val role: UserRole?,
    val label: String
)

data class AdminUsersUiState(
    val isLoading: Boolean = true,
    val accessDenied: Boolean = false,
    val allUsers: List<User> = emptyList(),
    val filteredUsers: List<User> = emptyList(),
    val searchQuery: String = "",
    val selectedRoleFilter: UserRole? = null,
    val selectedRoleFilterLabel: String = "Todos los roles",
    val roleFilterOptions: List<UserRoleFilterOption> = emptyList(),
    val error: String? = null
)

class AdminUsersViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository: AuthRepository =
        (application as SelvaBookingApplication).authRepository

    private var allUsers: List<User> = emptyList()

    private val _uiState = MutableStateFlow(AdminUsersUiState())
    val uiState: StateFlow<AdminUsersUiState> = _uiState.asStateFlow()

    init {
        val roleOptions = buildRoleFilterOptions()
        _uiState.update {
            it.copy(
                roleFilterOptions = roleOptions,
                selectedRoleFilterLabel = roleOptions.first().label
            )
        }
        viewModelScope.launch {
            val user = authRepository.getCurrentUserData().getOrNull()
            if (user?.rol != UserRole.SUPER_ADMIN) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        accessDenied = true,
                        error = "Solo SuperAdmin puede consultar usuarios"
                    )
                }
                return@launch
            }
            authRepository.getAllUsersFlow().collect { users ->
                allUsers = users.sortedWith(
                    compareBy({ it.rol.displayLabel.lowercase() }, { it.nombre.lowercase() }, { it.email.lowercase() })
                )
                applyFilters()
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun updateRoleFilter(label: String) {
        val option = _uiState.value.roleFilterOptions.find { it.label == label } ?: return
        _uiState.update {
            it.copy(
                selectedRoleFilter = option.role,
                selectedRoleFilterLabel = option.label
            )
        }
        applyFilters()
    }

    private fun applyFilters() {
        var filtered = allUsers
        val roleFilter = _uiState.value.selectedRoleFilter
        if (roleFilter != null) {
            filtered = filtered.filter { it.rol == roleFilter }
        }
        val query = _uiState.value.searchQuery.trim()
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.nombre.contains(query, ignoreCase = true) ||
                    it.email.contains(query, ignoreCase = true) ||
                    it.telefono.contains(query, ignoreCase = true)
            }
        }
        _uiState.update {
            it.copy(
                isLoading = false,
                allUsers = allUsers,
                filteredUsers = filtered
            )
        }
    }

    private fun buildRoleFilterOptions(): List<UserRoleFilterOption> = buildList {
        add(UserRoleFilterOption(null, "Todos los roles"))
        UserRole.entries.forEach { role ->
            add(UserRoleFilterOption(role, role.displayLabel))
        }
    }
}
