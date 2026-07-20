package com.company.selvabooking.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.company.selvabooking.SelvaBookingApplication
import com.company.selvabooking.domain.model.Hotel
import com.company.selvabooking.repository.HotelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val featuredHotels: List<Hotel> = emptyList(),
    val offerHotels: List<Hotel> = emptyList(),
    val recommendedHotels: List<Hotel> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val hotelRepository: HotelRepository =
        (application as SelvaBookingApplication).hotelRepository

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHotels()
    }

    private fun loadHotels() {
        viewModelScope.launch {
            hotelRepository.getHotelsFlow().collect { hotels ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        featuredHotels = hotels.filter { h -> h.destacado }.take(5),
                        offerHotels = hotels.filter { h -> h.oferta }.take(5),
                        recommendedHotels = hotels.sortedByDescending { h -> h.calificacion }.take(5)
                    )
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
