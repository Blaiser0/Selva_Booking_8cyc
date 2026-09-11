package com.company.selvabooking.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.company.selvabooking.SelvaBookingApplication
import com.company.selvabooking.domain.model.Hotel
import com.company.selvabooking.domain.model.Reservation
import com.company.selvabooking.domain.model.ReservationStatus
import com.company.selvabooking.domain.model.Room
import com.company.selvabooking.domain.model.UserRole
import com.company.selvabooking.repository.AuthRepository
import com.company.selvabooking.repository.HotelRepository
import com.company.selvabooking.repository.ReservationRepository
import com.company.selvabooking.repository.RoomRepository
import com.company.selvabooking.utils.AdminDataScope
import com.company.selvabooking.utils.DateUtils
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class AdminReservationsUiState(
    val isLoading: Boolean = true,
    val isRoleResolved: Boolean = false,
    val isGerenteMode: Boolean = false,
    val canManageReservations: Boolean = false,
    val hotelName: String = "",
    val totalCount: Int = 0,
    val confirmadasCount: Int = 0,
    val terminadasCount: Int = 0,
    val allReservations: List<Reservation> = emptyList(),
    val filteredReservations: List<Reservation> = emptyList(),
    val searchQuery: String = "",
    val statusFilter: ReservationStatus? = null,
    val selectedReservation: Reservation? = null,
    val showForm: Boolean = false,
    val editingReservationId: String? = null,
    val hotels: List<Hotel> = emptyList(),
    val rooms: List<Room> = emptyList(),
    val selectedHotelId: String = "",
    val selectedRoomId: String = "",
    val userNombre: String = "",
    val userEmail: String = "",
    val userTelefono: String = "",
    val fechaIngreso: String = "",
    val fechaSalida: String = "",
    val huespedes: String = "1",
    val estado: ReservationStatus = ReservationStatus.CONFIRMADA,
    val precioTotal: String = "",
    val isSaving: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class AdminReservationsViewModel(
    application: Application,
    private val forceGerenteMode: Boolean = false,
    initialCanManageReservations: Boolean = false,
    initialRoleResolved: Boolean = false
) : AndroidViewModel(application) {

    private val authRepository: AuthRepository =
        (application as SelvaBookingApplication).authRepository
    private val reservationRepository: ReservationRepository =
        (application as SelvaBookingApplication).reservationRepository
    private val hotelRepository: HotelRepository =
        (application as SelvaBookingApplication).hotelRepository
    private val roomRepository: RoomRepository =
        (application as SelvaBookingApplication).roomRepository

    private var isGerenteMode: Boolean = forceGerenteMode
    private var isLimitedAdminMode: Boolean = false
    private var currentAdminId: String = ""
    private var ownerHotelIds: Set<String> = emptySet()
    private var userResolved: Boolean = false
    private var hotelsResolved: Boolean = false
    private var reservationsLoaded: Boolean = false

    private val _uiState = MutableStateFlow(
        AdminReservationsUiState(
            isGerenteMode = forceGerenteMode,
            canManageReservations = initialCanManageReservations,
            isRoleResolved = initialRoleResolved || forceGerenteMode
        )
    )
    val uiState: StateFlow<AdminReservationsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            reservationRepository.expireFinishedReservations()
        }
        viewModelScope.launch {
            val user = authRepository.getCurrentUserData().getOrNull()
            isGerenteMode = forceGerenteMode || user?.rol == UserRole.GERENTE_HOTEL
            isLimitedAdminMode = !forceGerenteMode && user?.rol == UserRole.ADMINISTRADOR
            currentAdminId = user?.id.orEmpty()
            val isSuperAdmin = user?.rol == UserRole.SUPER_ADMIN
            _uiState.update {
                it.copy(
                    isGerenteMode = isGerenteMode,
                    canManageReservations = isSuperAdmin,
                    isRoleResolved = true
                )
            }
            userResolved = true
            when {
                isGerenteMode && user != null -> {
                    hotelRepository.getHotelsByOwnerFlow(user.id).collect { hotels ->
                        ownerHotelIds = hotels.map { it.id }.toSet()
                        hotelsResolved = true
                        _uiState.update {
                            it.copy(hotelName = hotels.firstOrNull()?.nombre.orEmpty())
                        }
                        applyFilters()
                    }
                }
                isLimitedAdminMode -> {
                    combine(
                        authRepository.getGerentesHotelFlow(),
                        hotelRepository.getHotelsFlow()
                    ) { gerentes, hotels ->
                        AdminDataScope.hotelsForAdmin(currentAdminId, gerentes, hotels)
                    }.collect { hotels ->
                        ownerHotelIds = hotels.map { it.id }.toSet()
                        hotelsResolved = true
                        _uiState.update { it.copy(hotels = hotels) }
                        applyFilters()
                    }
                }
                else -> {
                    hotelsResolved = true
                    applyFilters()
                    hotelRepository.getHotelsFlow().collect { hotels ->
                        _uiState.update { it.copy(hotels = hotels) }
                    }
                }
            }
        }
        viewModelScope.launch {
            reservationRepository.getAllReservationsFlow().collect { reservations ->
                _uiState.update { it.copy(allReservations = reservations) }
                reservationsLoaded = true
                applyFilters()
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun updateStatusFilter(status: ReservationStatus?) {
        _uiState.update { it.copy(statusFilter = status) }
        applyFilters()
    }

    fun selectReservation(reservation: Reservation?) {
        _uiState.update { it.copy(selectedReservation = reservation) }
    }

    fun openCreateForm() {
        _uiState.update {
            it.copy(
                showForm = true,
                editingReservationId = null,
                selectedHotelId = "",
                selectedRoomId = "",
                rooms = emptyList(),
                userNombre = "",
                userEmail = "",
                userTelefono = "",
                fechaIngreso = "",
                fechaSalida = "",
                huespedes = "1",
                estado = ReservationStatus.CONFIRMADA,
                precioTotal = "",
                error = null
            )
        }
    }

    fun openEditForm(reservation: Reservation) {
        _uiState.update {
            it.copy(
                showForm = true,
                editingReservationId = reservation.id,
                selectedHotelId = reservation.hotelId,
                selectedRoomId = reservation.roomId,
                userNombre = reservation.userNombre,
                userEmail = reservation.userEmail,
                userTelefono = reservation.userTelefono,
                fechaIngreso = reservation.fechaIngreso,
                fechaSalida = reservation.fechaSalida,
                huespedes = reservation.huespedes.toString(),
                estado = if (reservation.estado.isPublic) {
                    reservation.estado
                } else {
                    ReservationStatus.CONFIRMADA
                },
                precioTotal = reservation.precioTotal.toString(),
                error = null,
                selectedReservation = null
            )
        }
        loadRoomsForHotel(reservation.hotelId)
    }

    fun closeForm() {
        _uiState.update {
            it.copy(
                showForm = false,
                editingReservationId = null,
                error = null
            )
        }
    }

    fun updateUserNombre(v: String) = _uiState.update { it.copy(userNombre = v) }
    fun updateUserEmail(v: String) = _uiState.update { it.copy(userEmail = v) }
    fun updateUserTelefono(v: String) = _uiState.update { it.copy(userTelefono = v) }
    fun updateHuespedes(v: String) = _uiState.update { it.copy(huespedes = v) }
    fun updateEstado(status: ReservationStatus) = _uiState.update { it.copy(estado = status) }
    fun updatePrecioTotal(v: String) = _uiState.update { it.copy(precioTotal = v) }

    fun selectHotel(hotelId: String) {
        _uiState.update {
            it.copy(
                selectedHotelId = hotelId,
                selectedRoomId = "",
                rooms = emptyList()
            )
        }
        loadRoomsForHotel(hotelId)
        recalculatePrice()
    }

    fun selectRoom(roomId: String) {
        _uiState.update { it.copy(selectedRoomId = roomId) }
        recalculatePrice()
    }

    fun setFechaIngreso(calendar: Calendar) {
        _uiState.update { it.copy(fechaIngreso = DateUtils.formatStorage(calendar)) }
        recalculatePrice()
    }

    fun setFechaSalida(calendar: Calendar) {
        _uiState.update { it.copy(fechaSalida = DateUtils.formatStorage(calendar)) }
        recalculatePrice()
    }

    private fun loadRoomsForHotel(hotelId: String) {
        if (hotelId.isBlank()) return
        viewModelScope.launch {
            roomRepository.getRoomsByHotel(hotelId).fold(
                onSuccess = { rooms ->
                    _uiState.update { it.copy(rooms = rooms) }
                    recalculatePrice()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
            )
        }
    }

    private fun recalculatePrice() {
        val state = _uiState.value
        val room = state.rooms.find { it.id == state.selectedRoomId } ?: return
        if (state.fechaIngreso.isBlank() || state.fechaSalida.isBlank()) return
        val nights = DateUtils.daysBetween(state.fechaIngreso, state.fechaSalida).coerceAtLeast(1)
        val total = room.precio * nights
        _uiState.update { it.copy(precioTotal = total.toString()) }
    }

    fun saveReservation() {
        val state = _uiState.value
        if (state.selectedHotelId.isBlank() || state.selectedRoomId.isBlank()) {
            _uiState.update { it.copy(error = "Selecciona hotel y habitación") }
            return
        }
        if (state.userNombre.isBlank() || state.userEmail.isBlank()) {
            _uiState.update { it.copy(error = "Nombre y email del cliente son obligatorios") }
            return
        }
        if (state.fechaIngreso.isBlank() || state.fechaSalida.isBlank()) {
            _uiState.update { it.copy(error = "Selecciona fechas de ingreso y salida") }
            return
        }
        if (DateUtils.daysBetween(state.fechaIngreso, state.fechaSalida) < 1) {
            _uiState.update { it.copy(error = "La fecha de salida debe ser posterior a la de ingreso") }
            return
        }

        val hotel = state.hotels.find { it.id == state.selectedHotelId }
        val room = state.rooms.find { it.id == state.selectedRoomId }
        if (hotel == null || room == null) {
            _uiState.update { it.copy(error = "Hotel o habitación no encontrados") }
            return
        }
        if (state.editingReservationId == null && state.estado.holdsStock() && !room.isReservable) {
            _uiState.update { it.copy(error = "No hay habitaciones disponibles de este tipo") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val reservation = Reservation(
                id = state.editingReservationId ?: "",
                userId = "",
                hotelId = hotel.id,
                roomId = room.id,
                hotelNombre = hotel.nombre,
                roomNombre = room.nombre,
                userNombre = state.userNombre.trim(),
                userEmail = state.userEmail.trim(),
                userTelefono = state.userTelefono.trim(),
                fechaIngreso = state.fechaIngreso,
                fechaSalida = state.fechaSalida,
                huespedes = state.huespedes.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                precioTotal = state.precioTotal.toDoubleOrNull() ?: 0.0,
                estado = state.estado,
                createdAt = state.allReservations
                    .find { it.id == state.editingReservationId }
                    ?.createdAt ?: System.currentTimeMillis()
            )

            val result = if (state.editingReservationId != null) {
                val old = state.allReservations.find { it.id == state.editingReservationId }
                if (old != null && old.estado != reservation.estado) {
                    reservationRepository.updateReservationStatus(reservation.id, reservation.estado)
                        .fold(
                            onSuccess = { reservationRepository.updateReservation(reservation) },
                            onFailure = { Result.failure(it) }
                        )
                } else {
                    reservationRepository.updateReservation(reservation)
                }
            } else {
                reservationRepository.createReservation(reservation).map { }
            }

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            message = if (state.editingReservationId != null) {
                                "Reserva actualizada"
                            } else {
                                "Reserva creada"
                            },
                            showForm = false,
                            editingReservationId = null
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSaving = false, error = e.message) }
                }
            )
        }
    }

    fun deleteReservation(reservationId: String) {
        viewModelScope.launch {
            reservationRepository.deleteReservation(reservationId).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            message = "Reserva eliminada",
                            selectedReservation = null
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
            )
        }
    }

    private fun applyFilters() {
        if (!userResolved || !hotelsResolved || !reservationsLoaded) return
        val state = _uiState.value
        var base = state.allReservations.filter { it.estado.isPublic }
        if (isGerenteMode || isLimitedAdminMode) {
            base = base.filter { it.hotelId in ownerHotelIds }
        }
        val confirmadasCount = base.count { it.estado == ReservationStatus.CONFIRMADA }
        val terminadasCount = base.count { it.estado == ReservationStatus.TERMINADA }

        var filtered = base
        if (state.searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.hotelNombre.contains(state.searchQuery, ignoreCase = true) ||
                    it.roomNombre.contains(state.searchQuery, ignoreCase = true) ||
                    it.userNombre.contains(state.searchQuery, ignoreCase = true) ||
                    it.userEmail.contains(state.searchQuery, ignoreCase = true)
            }
        }
        if (state.statusFilter != null) {
            filtered = filtered.filter { it.estado == state.statusFilter }
        }
        _uiState.update {
            it.copy(
                isLoading = false,
                filteredReservations = filtered,
                totalCount = base.size,
                confirmadasCount = confirmadasCount,
                terminadasCount = terminadasCount
            )
        }
    }

    fun terminateReservation(id: String) = updateStatus(id, ReservationStatus.TERMINADA)

    private fun updateStatus(id: String, status: ReservationStatus) {
        viewModelScope.launch {
            reservationRepository.updateReservationStatus(id, status).fold(
                onSuccess = {
                    _uiState.update { it.copy(message = "Estado actualizado a ${status.value}") }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
            )
        }
    }

    fun clearMessages() = _uiState.update { it.copy(message = null, error = null) }

    fun onScreenVisible() {
        _uiState.update {
            it.copy(
                showForm = false,
                selectedReservation = null,
                editingReservationId = null,
                error = null
            )
        }
    }
}
