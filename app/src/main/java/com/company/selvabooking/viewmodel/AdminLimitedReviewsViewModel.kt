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
import com.company.selvabooking.utils.AdminDataScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResenaWithHotel(
    val resena: Resena,
    val hotelName: String
)

data class AdminLimitedReviewsUiState(
    val isLoading: Boolean = true,
    val accessDenied: Boolean = false,
    val resenas: List<ResenaWithHotel> = emptyList(),
    val hotelCount: Int = 0
)

class AdminLimitedReviewsViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository: AuthRepository =
        (application as SelvaBookingApplication).authRepository
    private val hotelRepository: HotelRepository =
        (application as SelvaBookingApplication).hotelRepository
    private val resenaRepository: ResenaRepository =
        (application as SelvaBookingApplication).resenaRepository

    private val _uiState = MutableStateFlow(AdminLimitedReviewsUiState())
    val uiState: StateFlow<AdminLimitedReviewsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = authRepository.getCurrentUserData().getOrNull()
            if (user?.rol != UserRole.ADMINISTRADOR) {
                _uiState.update {
                    it.copy(isLoading = false, accessDenied = true)
                }
                return@launch
            }
            combine(
                authRepository.getGerentesHotelFlow(),
                hotelRepository.getHotelsFlow(),
                resenaRepository.getAllResenasFlow()
            ) { gerentes, hotels, allResenas ->
                val scopedHotels = AdminDataScope.hotelsForAdmin(user.id, gerentes, hotels)
                val hotelNames = scopedHotels.associate { it.id to it.nombre }
                val resenas = allResenas
                    .filter { it.hotelId in hotelNames.keys }
                    .sortedByDescending { it.createdAt }
                    .map { resena ->
                        ResenaWithHotel(
                            resena = resena,
                            hotelName = hotelNames[resena.hotelId].orEmpty()
                        )
                    }
                AdminLimitedReviewsUiState(
                    isLoading = false,
                    resenas = resenas,
                    hotelCount = scopedHotels.size
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
