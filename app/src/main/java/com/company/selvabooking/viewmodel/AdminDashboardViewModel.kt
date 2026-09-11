package com.company.selvabooking.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.company.selvabooking.SelvaBookingApplication
import com.company.selvabooking.domain.model.Hotel
import com.company.selvabooking.domain.model.Reservation
import com.company.selvabooking.domain.model.ReservationStatus
import com.company.selvabooking.domain.model.User
import com.company.selvabooking.domain.model.UserRole
import com.company.selvabooking.repository.AuthRepository
import com.company.selvabooking.repository.HotelRepository
import com.company.selvabooking.repository.ReservationRepository
import com.company.selvabooking.repository.RoomRepository
import com.company.selvabooking.utils.AdminDataScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminDashboardUiState(
    val isLoading: Boolean = true,
    val isSuperAdmin: Boolean = false,
    val isLimitedAdmin: Boolean = false,
    val totalHotels: Int = 0,
    val totalRooms: Int = 0,
    val totalReservations: Int = 0,
    val confirmedReservations: Int = 0,
    val terminatedReservations: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalUsuarios: Int = 0,
    val totalGerentes: Int = 0,
    val totalAdministradores: Int = 0
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

    private var isLimitedAdmin: Boolean = false
    private var currentAdminId: String = ""

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUserData().getOrNull()
            val isSuperAdmin = user?.rol == UserRole.SUPER_ADMIN
            isLimitedAdmin = user?.rol == UserRole.ADMINISTRADOR
            currentAdminId = user?.id.orEmpty()
            _uiState.update {
                it.copy(
                    isSuperAdmin = isSuperAdmin,
                    isLimitedAdmin = isLimitedAdmin
                )
            }

            authRepository.seedSampleDataIfNeeded()
            reservationRepository.expireFinishedReservations()

            combine(
                combine(
                    hotelRepository.getHotelsFlow(),
                    reservationRepository.getAllReservationsFlow(),
                    authRepository.getGerentesHotelFlow(),
                    authRepository.getAdministratorAccountsFlow()
                ) { hotels, reservations, gerentes, administradores ->
                    DashboardSnapshot(
                        hotels = hotels,
                        reservations = reservations,
                        gerentes = gerentes,
                        administradoresCount = administradores.size
                    )
                },
                authRepository.getAllUsersFlow()
            ) { snapshot, allUsers ->
                snapshot.copy(totalUsuarios = if (isSuperAdmin) allUsers.size else 0)
            }.collect { snapshot ->
                val scopedHotels = if (isLimitedAdmin) {
                    AdminDataScope.hotelsForAdmin(currentAdminId, snapshot.gerentes, snapshot.hotels)
                } else {
                    snapshot.hotels
                }
                val scopedGerentes = if (isLimitedAdmin) {
                    AdminDataScope.gerentesCreatedBy(currentAdminId, snapshot.gerentes)
                } else {
                    snapshot.gerentes
                }
                val hotelIds = scopedHotels.map { it.id }.toSet()
                val allRooms = roomRepository.getAllRooms().getOrElse { emptyList() }
                val scopedRooms = allRooms.filter { it.hotelId in hotelIds }
                val publicReservations = snapshot.reservations.filter { it.estado.isPublic }
                val scopedReservations = if (isLimitedAdmin) {
                    AdminDataScope.reservationsForAdmin(
                        currentAdminId,
                        snapshot.gerentes,
                        snapshot.hotels,
                        publicReservations
                    )
                } else {
                    publicReservations
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        totalHotels = scopedHotels.size,
                        totalRooms = scopedRooms.size,
                        totalReservations = scopedReservations.size,
                        confirmedReservations = scopedReservations.count { r ->
                            r.estado == ReservationStatus.CONFIRMADA
                        },
                        terminatedReservations = scopedReservations.count { r ->
                            r.estado == ReservationStatus.TERMINADA
                        },
                        totalRevenue = scopedReservations
                            .filter { r ->
                                r.estado == ReservationStatus.CONFIRMADA ||
                                    r.estado == ReservationStatus.TERMINADA
                            }
                            .sumOf { r -> r.precioTotal },
                        totalUsuarios = snapshot.totalUsuarios,
                        totalGerentes = scopedGerentes.size,
                        totalAdministradores = snapshot.administradoresCount
                    )
                }
            }
        }
    }

    private data class DashboardSnapshot(
        val hotels: List<Hotel>,
        val reservations: List<Reservation>,
        val gerentes: List<User>,
        val administradoresCount: Int,
        val totalUsuarios: Int = 0
    )
}
