package com.company.selvabooking.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.company.selvabooking.SelvaBookingApplication
import com.company.selvabooking.domain.model.Reservation
import com.company.selvabooking.domain.model.ReservationStatus
import com.company.selvabooking.repository.AuthRepository
import com.company.selvabooking.repository.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyReservationsUiState(
    val isLoading: Boolean = true,
    val reservations: List<Reservation> = emptyList(),
    val statusFilter: ReservationStatus? = null,
    val filteredReservations: List<Reservation> = emptyList(),
    val error: String? = null,
    val cancelMessage: String? = null
)

class MyReservationsViewModel(application: Application) : AndroidViewModel(application) {

    private val reservationRepository: ReservationRepository =
        (application as SelvaBookingApplication).reservationRepository
    private val authRepository: AuthRepository =
        (application as SelvaBookingApplication).authRepository

    private val _uiState = MutableStateFlow(MyReservationsUiState())
    val uiState: StateFlow<MyReservationsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.authStateFlow().collectLatest { firebaseUser ->
                val userId = firebaseUser?.uid
                if (userId.isNullOrBlank()) {
                    _uiState.update {
                        it.copy(isLoading = false, reservations = emptyList(), filteredReservations = emptyList())
                    }
                    return@collectLatest
                }
                reservationRepository.getUserReservationsFlow(userId).collect { reservations ->
                    _uiState.update {
                        it.copy(isLoading = false, reservations = reservations)
                    }
                    applyFilters()
                }
            }
        }
    }

    fun updateStatusFilter(status: ReservationStatus?) {
        _uiState.update { it.copy(statusFilter = status) }
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        val filtered = if (state.statusFilter == null) {
            state.reservations
        } else {
            state.reservations.filter { it.estado == state.statusFilter }
        }
        _uiState.update { it.copy(filteredReservations = filtered) }
    }

    fun cancelReservation(reservationId: String) {
        viewModelScope.launch {
            reservationRepository.cancelReservation(reservationId).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(cancelMessage = "Reserva cancelada exitosamente")
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
            )
        }
    }

    fun canCancel(reservation: Reservation): Boolean {
        return reservation.estado == ReservationStatus.PENDIENTE ||
            reservation.estado == ReservationStatus.CONFIRMADA
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, cancelMessage = null) }
    }
}
