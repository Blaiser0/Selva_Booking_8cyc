package com.company.selvabooking.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.company.selvabooking.SelvaBookingApplication
import com.company.selvabooking.data.firebase.FirestoreService
import com.company.selvabooking.domain.model.ReservationStatus
import com.company.selvabooking.repository.AuthRepository
import com.company.selvabooking.repository.HotelRepository
import com.company.selvabooking.repository.ReservationRepository
import com.company.selvabooking.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminDashboardUiState(
    val isLoading: Boolean = true,
    val totalHotels: Int = 0,
    val totalRooms: Int = 0,
    val totalReservations: Int = 0,
    val activeReservations: Int = 0,
    val totalUsers: Int = 0,
    val pendingAdminRequests: Int = 0
)

class AdminDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val hotelRepository: HotelRepository =
        (application as SelvaBookingApplication).hotelRepository
    private val roomRepository: RoomRepository =
        (application as SelvaBookingApplication).roomRepository
    private val reservationRepository: ReservationRepository =
        (application as SelvaBookingApplication).reservationRepository
    private val authRepository: AuthRepository =
        (application as SelvaBookingApplication).authRepository
    private val firestoreService = FirestoreService()

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            authRepository.seedSampleDataIfNeeded()
            combine(
                hotelRepository.getHotelsFlow(),
                reservationRepository.getAllReservationsFlow()
            ) { hotels, reservations ->
                Pair(hotels, reservations)
            }.collect { (hotels, reservations) ->
                val rooms = roomRepository.getAllRooms().getOrElse { emptyList() }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        totalHotels = hotels.size,
                        totalRooms = rooms.size,
                        totalReservations = reservations.size,
                        activeReservations = reservations.count { r ->
                            r.estado == ReservationStatus.CONFIRMADA ||
                                r.estado == ReservationStatus.PENDIENTE
                        }
                    )
                }
            }
        }
        viewModelScope.launch {
            firestoreService.getAllUsers().onSuccess { users ->
                _uiState.update { it.copy(totalUsers = users.size) }
            }
        }
        viewModelScope.launch {
            authRepository.getPendingAdminRequestsFlow().collect { requests ->
                _uiState.update { it.copy(pendingAdminRequests = requests.size) }
            }
        }
    }
}
