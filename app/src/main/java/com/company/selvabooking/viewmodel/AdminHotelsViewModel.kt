package com.company.selvabooking.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.company.selvabooking.SelvaBookingApplication
import com.company.selvabooking.domain.model.Hotel
import com.company.selvabooking.domain.model.Room
import com.company.selvabooking.repository.HotelRepository
import com.company.selvabooking.repository.ResenaRepository
import com.company.selvabooking.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminHotelsUiState(
    val isLoading: Boolean = true,
    val hotels: List<Hotel> = emptyList(),
    val isSaving: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val editingHotelId: String? = null,
    val nombre: String = "",
    val ciudad: String = "",
    val direccion: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val estrellas: Int = 3,
    val precioMinimo: String = "",
    val calificacion: String = "4.0",
    val servicios: String = "",
    val ubicacion: String = "",
    val destacado: Boolean = false,
    val oferta: Boolean = false,
    val imagenes: List<String> = emptyList()
)

class AdminHotelsViewModel(application: Application) : AndroidViewModel(application) {

    private val hotelRepository: HotelRepository =
        (application as SelvaBookingApplication).hotelRepository
    private val roomRepository: RoomRepository =
        (application as SelvaBookingApplication).roomRepository
    private val resenaRepository: ResenaRepository =
        (application as SelvaBookingApplication).resenaRepository

    private val _uiState = MutableStateFlow(AdminHotelsUiState())
    val uiState: StateFlow<AdminHotelsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            hotelRepository.getHotelsFlow().collect { hotels ->
                _uiState.update { it.copy(isLoading = false, hotels = hotels) }
            }
        }
    }

    fun loadHotelForEdit(hotel: Hotel) {
        _uiState.update {
            it.copy(
                editingHotelId = hotel.id,
                nombre = hotel.nombre,
                ciudad = hotel.ciudad,
                direccion = hotel.direccion,
                descripcion = hotel.descripcion,
                categoria = hotel.categoria,
                estrellas = hotel.estrellas,
                precioMinimo = hotel.precioMinimo.toString(),
                calificacion = hotel.effectiveBaseRating().toString(),
                servicios = hotel.servicios.joinToString(", "),
                ubicacion = hotel.ubicacion,
                destacado = hotel.destacado,
                oferta = hotel.oferta,
                imagenes = hotel.imagenes
            )
        }
    }

    fun clearForm() {
        _uiState.update { AdminHotelsUiState(hotels = it.hotels, isLoading = false) }
    }

    fun updateNombre(v: String) = _uiState.update { it.copy(nombre = v) }
    fun updateCiudad(v: String) = _uiState.update { it.copy(ciudad = v) }
    fun updateDireccion(v: String) = _uiState.update { it.copy(direccion = v) }
    fun updateDescripcion(v: String) = _uiState.update { it.copy(descripcion = v) }
    fun updateCategoria(v: String) = _uiState.update { it.copy(categoria = v) }
    fun updateEstrellas(v: Int) = _uiState.update { it.copy(estrellas = v) }
    fun updatePrecioMinimo(v: String) = _uiState.update { it.copy(precioMinimo = v) }
    fun updateCalificacion(v: String) = _uiState.update { it.copy(calificacion = v) }
    fun updateServicios(v: String) = _uiState.update { it.copy(servicios = v) }
    fun updateUbicacion(v: String) = _uiState.update { it.copy(ubicacion = v) }
    fun updateDestacado(v: Boolean) = _uiState.update { it.copy(destacado = v) }
    fun updateOferta(v: Boolean) = _uiState.update { it.copy(oferta = v) }

    fun removeImage(index: Int) {
        _uiState.update { state ->
            state.copy(imagenes = state.imagenes.filterIndexed { i, _ -> i != index })
        }
    }

    fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            hotelRepository.uploadHotelImage(uri).fold(
                onSuccess = { url ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            imagenes = it.imagenes + url
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSaving = false, error = e.message) }
                }
            )
        }
    }

    fun saveHotel() {
        val state = _uiState.value
        if (state.nombre.isBlank() || state.ciudad.isBlank()) {
            _uiState.update { it.copy(error = "Nombre y ciudad son obligatorios") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val adminRating = state.calificacion.toDoubleOrNull() ?: 4.0
            val hotel = Hotel(
                id = state.editingHotelId ?: "",
                nombre = state.nombre,
                ciudad = state.ciudad,
                direccion = state.direccion,
                descripcion = state.descripcion,
                categoria = state.categoria,
                estrellas = state.estrellas,
                precioMinimo = state.precioMinimo.toDoubleOrNull() ?: 0.0,
                calificacion = adminRating,
                calificacionBase = adminRating,
                servicios = state.servicios.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                ubicacion = state.ubicacion,
                destacado = state.destacado,
                oferta = state.oferta,
                imagenes = state.imagenes
            )
            val result = if (state.editingHotelId != null) {
                hotelRepository.updateHotel(hotel).map { hotel.id }
            } else {
                hotelRepository.createHotel(hotel)
            }
            result.fold(
                onSuccess = { savedHotelId ->
                    resenaRepository.refreshHotelRating(savedHotelId)
                    _uiState.update {
                        it.copy(isSaving = false, message = "Hotel guardado", editingHotelId = null)
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSaving = false, error = e.message) }
                }
            )
        }
    }

    fun deleteHotel(hotelId: String) {
        viewModelScope.launch {
            hotelRepository.deleteHotel(hotelId).fold(
                onSuccess = {
                    _uiState.update { it.copy(message = "Hotel eliminado") }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
            )
        }
    }

    fun clearMessages() = _uiState.update { it.copy(message = null, error = null) }
}

data class AdminRoomsUiState(
    val isLoading: Boolean = true,
    val hotelId: String = "",
    val hotelName: String = "",
    val rooms: List<Room> = emptyList(),
    val isSaving: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val editingRoomId: String? = null,
    val nombre: String = "",
    val descripcion: String = "",
    val precio: String = "",
    val capacidad: String = "2",
    val disponible: Boolean = true,
    val imagenes: List<String> = emptyList()
)

class AdminRoomsViewModel(
    application: Application,
    private val hotelId: String
) : AndroidViewModel(application) {

    private val hotelRepository: HotelRepository =
        (application as SelvaBookingApplication).hotelRepository
    private val roomRepository: RoomRepository =
        (application as SelvaBookingApplication).roomRepository

    private val _uiState = MutableStateFlow(AdminRoomsUiState(hotelId = hotelId))
    val uiState: StateFlow<AdminRoomsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            hotelRepository.getHotel(hotelId).onSuccess { hotel ->
                _uiState.update { it.copy(hotelName = hotel.nombre) }
            }
            roomRepository.getRoomsByHotelFlow(hotelId).collect { rooms ->
                _uiState.update { it.copy(isLoading = false, rooms = rooms) }
            }
        }
    }

    fun loadRoomForEdit(room: Room) {
        _uiState.update {
            it.copy(
                editingRoomId = room.id,
                nombre = room.nombre,
                descripcion = room.descripcion,
                precio = room.precio.toString(),
                capacidad = room.capacidad.toString(),
                disponible = room.disponible,
                imagenes = room.imagenes
            )
        }
    }

    fun clearForm() {
        _uiState.update {
            AdminRoomsUiState(
                hotelId = hotelId,
                hotelName = it.hotelName,
                rooms = it.rooms,
                isLoading = false
            )
        }
    }

    fun updateNombre(v: String) = _uiState.update { it.copy(nombre = v) }
    fun updateDescripcion(v: String) = _uiState.update { it.copy(descripcion = v) }
    fun updatePrecio(v: String) = _uiState.update { it.copy(precio = v) }
    fun updateCapacidad(v: String) = _uiState.update { it.copy(capacidad = v) }
    fun updateDisponible(v: Boolean) = _uiState.update { it.copy(disponible = v) }

    fun removeImage(index: Int) {
        _uiState.update { state ->
            state.copy(imagenes = state.imagenes.filterIndexed { i, _ -> i != index })
        }
    }

    fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            roomRepository.uploadRoomImage(uri).fold(
                onSuccess = { url ->
                    _uiState.update { it.copy(imagenes = it.imagenes + url) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
            )
        }
    }

    fun saveRoom() {
        val state = _uiState.value
        if (state.nombre.isBlank()) {
            _uiState.update { it.copy(error = "El nombre es obligatorio") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val room = Room(
                id = state.editingRoomId ?: "",
                hotelId = hotelId,
                nombre = state.nombre,
                descripcion = state.descripcion,
                precio = state.precio.toDoubleOrNull() ?: 0.0,
                capacidad = state.capacidad.toIntOrNull() ?: 2,
                disponible = state.disponible,
                imagenes = state.imagenes
            )
            val result = if (state.editingRoomId != null) {
                roomRepository.updateRoom(room)
            } else {
                roomRepository.createRoom(room).map { }
            }
            result.fold(
                onSuccess = {
                    hotelRepository.syncPrecioMinimoFromRooms(hotelId)
                    _uiState.update { it.copy(isSaving = false, message = "Habitación guardada") }
                    clearForm()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSaving = false, error = e.message) }
                }
            )
        }
    }

    fun deleteRoom(roomId: String) {
        viewModelScope.launch {
            roomRepository.deleteRoom(roomId).fold(
                onSuccess = {
                    hotelRepository.syncPrecioMinimoFromRooms(hotelId)
                    _uiState.update { it.copy(message = "Habitación eliminada") }
                },
                onFailure = { e -> _uiState.update { it.copy(error = e.message) } }
            )
        }
    }

    fun clearMessages() = _uiState.update { it.copy(message = null, error = null) }
}
