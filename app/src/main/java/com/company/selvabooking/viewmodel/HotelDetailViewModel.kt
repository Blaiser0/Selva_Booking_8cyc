package com.company.selvabooking.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.company.selvabooking.SelvaBookingApplication
import com.company.selvabooking.domain.model.Hotel
import com.company.selvabooking.domain.model.Resena
import com.company.selvabooking.domain.model.Room
import com.company.selvabooking.repository.HotelRepository
import com.company.selvabooking.repository.ResenaRepository
import com.company.selvabooking.repository.ReservationRepository
import com.company.selvabooking.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HotelDetailUiState(
    val isLoading: Boolean = true,
    val hotel: Hotel? = null,
    val rooms: List<Room> = emptyList(),
    val selectedImageIndex: Int = 0,
    val resenas: List<Resena> = emptyList(),
    val canReview: Boolean = false,
    val eligibleReservationId: String = "",
    val userResena: Resena? = null,
    val showReviewDialog: Boolean = false,
    val reviewCalificacion: Int = 3,
    val reviewComentario: String = "",
    val reviewCalificacionError: String? = null,
    val reviewComentarioError: String? = null,
    val isSubmittingReview: Boolean = false,
    val showDeleteReviewDialog: Boolean = false,
    val reviewMessage: String? = null,
    val error: String? = null
)

class HotelDetailViewModel(
    application: Application,
    private val hotelId: String,
    private val userId: String?,
    private val userName: String?
) : AndroidViewModel(application) {

    private val hotelRepository: HotelRepository =
        (application as SelvaBookingApplication).hotelRepository
    private val roomRepository: RoomRepository =
        (application as SelvaBookingApplication).roomRepository
    private val reservationRepository: ReservationRepository =
        (application as SelvaBookingApplication).reservationRepository
    private val resenaRepository: ResenaRepository =
        (application as SelvaBookingApplication).resenaRepository

    private val _uiState = MutableStateFlow(HotelDetailUiState())
    val uiState: StateFlow<HotelDetailUiState> = _uiState.asStateFlow()

    init {
        loadHotel()
        loadRooms()
        loadResenas()
        observeReviewEligibility()
    }

    private fun loadHotel() {
        viewModelScope.launch {
            hotelRepository.getHotelsFlow().collect { hotels ->
                hotels.find { it.id == hotelId }?.let { hotel ->
                    _uiState.update { it.copy(hotel = hotel, isLoading = false) }
                }
            }
        }
        viewModelScope.launch {
            hotelRepository.getHotel(hotelId).fold(
                onSuccess = { hotel ->
                    _uiState.update { it.copy(hotel = hotel, isLoading = false) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message)
                    }
                }
            )
        }
    }

    private fun loadRooms() {
        viewModelScope.launch {
            roomRepository.getRoomsByHotelFlow(hotelId).collect { rooms ->
                _uiState.update { it.copy(rooms = rooms.filter { room -> room.disponible }) }
            }
        }
    }

    private fun loadResenas() {
        viewModelScope.launch {
            resenaRepository.getResenasByHotelFlow(hotelId).collect { resenas ->
                val userResena = userId?.let { id -> resenas.find { it.userId == id } }
                _uiState.update {
                    it.copy(
                        resenas = resenas,
                        userResena = userResena
                    )
                }
            }
        }
    }

    private fun observeReviewEligibility() {
        if (userId.isNullOrBlank()) return
        viewModelScope.launch {
            reservationRepository.getUserReservationsFlow(userId).collect { reservations ->
                val eligible = resenaRepository.findEligibleReservation(reservations, hotelId)
                _uiState.update {
                    it.copy(
                        canReview = eligible != null,
                        eligibleReservationId = eligible?.id.orEmpty()
                    )
                }
            }
        }
    }

    fun selectImage(index: Int) {
        _uiState.update { it.copy(selectedImageIndex = index) }
    }

    fun openReviewDialog() {
        val userResena = _uiState.value.userResena
        _uiState.update {
            it.copy(
                showReviewDialog = true,
                reviewCalificacion = userResena?.calificacion?.coerceIn(1, 5) ?: 3,
                reviewComentario = userResena?.comentario.orEmpty(),
                reviewCalificacionError = null,
                reviewComentarioError = null,
                reviewMessage = null,
                error = null
            )
        }
    }

    fun dismissReviewDialog() {
        _uiState.update {
            it.copy(
                showReviewDialog = false,
                reviewCalificacionError = null,
                reviewComentarioError = null
            )
        }
    }

    fun updateReviewCalificacion(value: Int) {
        _uiState.update {
            it.copy(reviewCalificacion = value.coerceIn(1, 5), reviewCalificacionError = null)
        }
    }

    fun updateReviewComentario(value: String) {
        _uiState.update {
            it.copy(reviewComentario = value, reviewComentarioError = null)
        }
    }

    fun submitReview() {
        val state = _uiState.value
        if (userId.isNullOrBlank() || userName.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Debe iniciar sesión para comentar") }
            return
        }
        if (!state.canReview) {
            _uiState.update {
                it.copy(error = "Solo pueden comentar quienes han reservado al menos una vez en este hotel")
            }
            return
        }

        val calificacionError = if (state.reviewCalificacion !in 1..5) {
            "Seleccione una calificación"
        } else null
        val comentarioError = when {
            state.reviewComentario.isBlank() -> "Escriba un comentario"
            state.reviewComentario.trim().length < 10 -> "El comentario debe tener al menos 10 caracteres"
            else -> null
        }
        if (calificacionError != null || comentarioError != null) {
            _uiState.update {
                it.copy(
                    reviewCalificacionError = calificacionError,
                    reviewComentarioError = comentarioError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingReview = true, error = null) }

            val existing = state.userResena
            val result = if (existing != null) {
                resenaRepository.updateResena(
                    existing.copy(
                        calificacion = state.reviewCalificacion,
                        comentario = state.reviewComentario.trim()
                    )
                )
            } else {
                resenaRepository.createResena(
                    Resena(
                        hotelId = hotelId,
                        userId = userId,
                        reservationId = state.eligibleReservationId,
                        userNombre = userName,
                        calificacion = state.reviewCalificacion,
                        comentario = state.reviewComentario.trim()
                    )
                ).map { Unit }
            }

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSubmittingReview = false,
                            showReviewDialog = false,
                            reviewMessage = if (existing != null) {
                                "Reseña actualizada correctamente"
                            } else {
                                "Gracias por tu comentario"
                            }
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isSubmittingReview = false,
                            error = e.message ?: "No se pudo guardar la reseña"
                        )
                    }
                }
            )
        }
    }

    fun requestDeleteReview() {
        _uiState.update { it.copy(showDeleteReviewDialog = true) }
    }

    fun dismissDeleteReviewDialog() {
        _uiState.update { it.copy(showDeleteReviewDialog = false) }
    }

    fun confirmDeleteReview() {
        val resena = _uiState.value.userResena ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingReview = true, showDeleteReviewDialog = false) }
            resenaRepository.deleteResena(resena.id, hotelId).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSubmittingReview = false,
                            reviewMessage = "Reseña eliminada"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isSubmittingReview = false,
                            error = e.message ?: "No se pudo eliminar la reseña"
                        )
                    }
                }
            )
        }
    }

    fun clearReviewMessage() {
        _uiState.update { it.copy(reviewMessage = null) }
    }
}
