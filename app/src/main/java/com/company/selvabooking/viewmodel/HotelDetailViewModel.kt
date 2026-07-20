package com.company.selvabooking.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.company.selvabooking.SelvaBookingApplication
import com.company.selvabooking.domain.model.Hotel
import com.company.selvabooking.domain.model.Room
import com.company.selvabooking.repository.HotelRepository
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
    val error: String? = null
)

class HotelDetailViewModel(
    application: Application,
    private val hotelId: String
) : AndroidViewModel(application) {

    private val hotelRepository: HotelRepository =
        (application as SelvaBookingApplication).hotelRepository
    private val roomRepository: RoomRepository =
        (application as SelvaBookingApplication).roomRepository

    private val _uiState = MutableStateFlow(HotelDetailUiState())
    val uiState: StateFlow<HotelDetailUiState> = _uiState.asStateFlow()

    init {
        loadHotel()
        loadRooms()
    }

    private fun loadHotel() {
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
                _uiState.update { it.copy(rooms = rooms.filter { r -> r.disponible }) }
            }
        }
    }

    fun selectImage(index: Int) {
        _uiState.update { it.copy(selectedImageIndex = index) }
    }
}
