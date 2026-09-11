package com.company.selvabooking.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.company.selvabooking.SelvaBookingApplication
import com.company.selvabooking.domain.model.UserRole
import com.company.selvabooking.repository.AuthRepository
import com.company.selvabooking.repository.HotelRepository
import com.company.selvabooking.repository.ResenaRepository
import com.company.selvabooking.utils.AdminDataScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminHotelReviewsViewModel(
    application: Application,
    private val hotelId: String,
    initialHotelName: String = ""
) : AndroidViewModel(application) {

    private val authRepository: AuthRepository =
        (application as SelvaBookingApplication).authRepository
    private val hotelRepository: HotelRepository =
        (application as SelvaBookingApplication).hotelRepository
    private val resenaRepository: ResenaRepository =
        (application as SelvaBookingApplication).resenaRepository

    private val _uiState = MutableStateFlow(
        HotelReviewsUiState(hotelName = initialHotelName)
    )
    val uiState: StateFlow<HotelReviewsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = authRepository.getCurrentUserData().getOrNull()
            val canAccess = when (user?.rol) {
                UserRole.SUPER_ADMIN -> true
                UserRole.ADMINISTRADOR -> {
                    val gerentes = authRepository.getGerentesHotelFlow().first()
                    val hotels = hotelRepository.getHotelsFlow().first()
                    AdminDataScope.hotelsForAdmin(user.id, gerentes, hotels)
                        .any { it.id == hotelId }
                }
                else -> false
            }
            if (!canAccess) {
                _uiState.update {
                    it.copy(isLoading = false, accessDenied = true)
                }
                return@launch
            }
            loadReviewsForHotel(hotelId)
        }
    }

    private suspend fun loadReviewsForHotel(targetHotelId: String) {
        val hotel = hotelRepository.getHotel(targetHotelId).getOrNull()
        if (hotel == null) {
            _uiState.update {
                it.copy(isLoading = false, noHotel = true)
            }
            return
        }
        _uiState.update { it.copy(hotelName = hotel.nombre) }
        resenaRepository.getResenasByHotelFlow(targetHotelId).collect { resenas ->
            _uiState.update {
                it.copy(isLoading = false, resenas = resenas)
            }
        }
    }
}
