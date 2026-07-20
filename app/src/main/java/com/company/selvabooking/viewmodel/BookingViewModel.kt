package com.company.selvabooking.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.company.selvabooking.SelvaBookingApplication
import com.company.selvabooking.domain.model.Hotel
import com.company.selvabooking.domain.model.Reservation
import com.company.selvabooking.domain.model.ReservationStatus
import com.company.selvabooking.domain.model.Room
import com.company.selvabooking.domain.model.User
import com.company.selvabooking.repository.AuthRepository
import com.company.selvabooking.repository.HotelRepository
import com.company.selvabooking.repository.ReservationRepository
import com.company.selvabooking.repository.RoomRepository
import com.company.selvabooking.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class BookingUiState(
    val isLoading: Boolean = true,
    val hotel: Hotel? = null,
    val room: Room? = null,
    val user: User? = null,
    val fechaIngreso: String = "",
    val fechaSalida: String = "",
    val huespedes: Int = 1,
    val totalNights: Int = 0,
    val totalPrice: Double = 0.0,
    val isSubmitting: Boolean = false,
    val reservationId: String? = null,
    val error: String? = null,
    val fechaError: String? = null
)

class BookingViewModel(
    application: Application,
    private val hotelId: String,
    private val roomId: String
) : AndroidViewModel(application) {

    private val hotelRepository: HotelRepository =
        (application as SelvaBookingApplication).hotelRepository
    private val roomRepository: RoomRepository =
        (application as SelvaBookingApplication).roomRepository
    private val reservationRepository: ReservationRepository =
        (application as SelvaBookingApplication).reservationRepository
    private val authRepository: AuthRepository =
        (application as SelvaBookingApplication).authRepository

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            authRepository.getCurrentUserData().onSuccess { user ->
                _uiState.update { it.copy(user = user) }
            }
            hotelRepository.getHotel(hotelId).onSuccess { hotel ->
                _uiState.update { it.copy(hotel = hotel) }
            }
            roomRepository.getRoomsByHotelFlow(hotelId).collect { rooms ->
                val room = rooms.find { it.id == roomId }
                _uiState.update { it.copy(room = room, isLoading = false) }
                calculateTotal()
            }
        }
    }

    fun setFechaIngreso(calendar: Calendar) {
        _uiState.update {
            it.copy(fechaIngreso = DateUtils.formatStorage(calendar), fechaError = null)
        }
        calculateTotal()
    }

    fun setFechaSalida(calendar: Calendar) {
        _uiState.update {
            it.copy(fechaSalida = DateUtils.formatStorage(calendar), fechaError = null)
        }
        calculateTotal()
    }

    fun setHuespedes(count: Int) {
        _uiState.update { it.copy(huespedes = count.coerceAtLeast(1)) }
    }

    private fun calculateTotal() {
        val state = _uiState.value
        if (state.fechaIngreso.isNotEmpty() && state.fechaSalida.isNotEmpty() && state.room != null) {
            val nights = DateUtils.daysBetween(state.fechaIngreso, state.fechaSalida)
            val total = if (nights > 0) nights * state.room.precio else 0.0
            _uiState.update { it.copy(totalNights = nights, totalPrice = total) }
        }
    }

    fun confirmBooking() {
        val state = _uiState.value
        if (state.fechaIngreso.isEmpty() || state.fechaSalida.isEmpty()) {
            _uiState.update { it.copy(fechaError = "Seleccione las fechas de ingreso y salida") }
            return
        }
        if (state.totalNights <= 0) {
            _uiState.update { it.copy(fechaError = "La fecha de salida debe ser posterior al ingreso") }
            return
        }
        if (state.huespedes > (state.room?.capacidad ?: 1)) {
            _uiState.update { it.copy(fechaError = "Excede la capacidad de la habitación") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            val authUserId = authRepository.currentUser?.uid.orEmpty()
            val reservation = Reservation(
                userId = state.user?.id?.takeIf { it.isNotBlank() } ?: authUserId,
                hotelId = hotelId,
                roomId = roomId,
                hotelNombre = state.hotel?.nombre ?: "",
                roomNombre = state.room?.nombre ?: "",
                userNombre = state.user?.nombre ?: "",
                userEmail = state.user?.email ?: "",
                userTelefono = state.user?.telefono ?: "",
                fechaIngreso = state.fechaIngreso,
                fechaSalida = state.fechaSalida,
                huespedes = state.huespedes,
                precioTotal = state.totalPrice,
                estado = ReservationStatus.PENDIENTE
            )
            reservationRepository.createReservation(reservation).fold(
                onSuccess = { id ->
                    _uiState.update { it.copy(isSubmitting = false, reservationId = id) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isSubmitting = false, error = e.message)
                    }
                }
            )
        }
    }
}
