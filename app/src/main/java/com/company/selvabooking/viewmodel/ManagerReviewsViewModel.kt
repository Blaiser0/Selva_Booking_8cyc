package com.company.selvabooking.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.company.selvabooking.SelvaBookingApplication
import com.company.selvabooking.domain.model.Resena
import com.company.selvabooking.domain.model.UserRole
import com.company.selvabooking.repository.AuthRepository
import com.company.selvabooking.repository.HotelRepository
import com.company.selvabooking.repository.ResenaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HotelReviewsUiState(
    val isLoading: Boolean = true,
    val hotelName: String = "",
    val resenas: List<Resena> = emptyList(),
    val accessDenied: Boolean = false,
    val noHotel: Boolean = false
)

class ManagerReviewsViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository: AuthRepository =
        (application as SelvaBookingApplication).authRepository
    private val hotelRepository: HotelRepository =
        (application as SelvaBookingApplication).hotelRepository
    private val resenaRepository: ResenaRepository =
        (application as SelvaBookingApplication).resenaRepository

    private val _uiState = MutableStateFlow(HotelReviewsUiState())
    val uiState: StateFlow<HotelReviewsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = authRepository.getCurrentUserData().getOrNull()
            if (user?.rol != UserRole.GERENTE_HOTEL) {
                _uiState.update {
                    it.copy(isLoading = false, accessDenied = true)
                }
                return@launch
            }
            hotelRepository.getHotelsByOwnerFlow(user.id)
                .flatMapLatest { hotels ->
                    val hotel = hotels.firstOrNull()
                    when {
                        hotel == null -> flowOf(
                            HotelReviewsUiState(
                                isLoading = false,
                                noHotel = true
                            )
                        )
                        else -> resenaRepository.getResenasByHotelFlow(hotel.id)
                            .map { resenas ->
                                HotelReviewsUiState(
                                    isLoading = false,
                                    hotelName = hotel.nombre,
                                    resenas = resenas
                                )
                            }
                    }
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }
}
