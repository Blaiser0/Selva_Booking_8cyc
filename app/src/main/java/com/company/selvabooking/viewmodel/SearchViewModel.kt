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

enum class HotelSortOption {
    RECOMMENDED,
    PRICE_ASC,
    RATING_DESC
}

data class SearchUiState(
    val isLoading: Boolean = true,
    val allHotels: List<Hotel> = emptyList(),
    val filteredHotels: List<Hotel> = emptyList(),
    val query: String = "",
    val ciudad: String = "",
    val precioMax: String = "",
    val estrellasMin: Int = 0,
    val sortBy: HotelSortOption = HotelSortOption.RECOMMENDED,
    val filtersExpanded: Boolean = false,
    val error: String? = null
)

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val hotelRepository: HotelRepository =
        (application as SelvaBookingApplication).hotelRepository

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            hotelRepository.getHotelsFlow().collect { hotels ->
                _uiState.update {
                    it.copy(isLoading = false, allHotels = hotels)
                }
                applyFilters()
            }
        }
    }

    fun updateQuery(value: String) {
        _uiState.update { it.copy(query = value) }
        applyFilters()
    }

    fun updateCiudad(value: String) {
        _uiState.update { it.copy(ciudad = value) }
        applyFilters()
    }

    fun updatePrecioMax(value: String) {
        _uiState.update { it.copy(precioMax = value) }
        applyFilters()
    }

    fun updateEstrellasMin(value: Int) {
        _uiState.update { it.copy(estrellasMin = value) }
        applyFilters()
    }

    fun updateSortBy(value: HotelSortOption) {
        _uiState.update { it.copy(sortBy = value) }
        applyFilters()
    }

    fun toggleFilters() {
        _uiState.update { it.copy(filtersExpanded = !it.filtersExpanded) }
    }

    private fun applyFilters() {
        val state = _uiState.value
        val precioMax = state.precioMax.toDoubleOrNull()
        val filtered = hotelRepository.searchHotels(
            hotels = state.allHotels,
            query = state.query,
            ciudad = state.ciudad,
            precioMax = precioMax,
            estrellasMin = state.estrellasMin
        )
        val sorted = when (state.sortBy) {
            HotelSortOption.PRICE_ASC -> filtered.sortedBy { it.precioMinimo }
            HotelSortOption.RATING_DESC -> filtered.sortedByDescending { it.calificacion }
            HotelSortOption.RECOMMENDED -> filtered.sortedWith(
                compareByDescending<Hotel> { it.destacado }
                    .thenByDescending { it.calificacion }
                    .thenBy { it.precioMinimo }
            )
        }
        _uiState.update { it.copy(filteredHotels = sorted) }
    }
}
