package com.company.selvabooking.repository

import android.net.Uri
import com.company.selvabooking.data.firebase.FirestoreService
import com.company.selvabooking.data.firebase.StorageService
import com.company.selvabooking.domain.model.Hotel
import com.company.selvabooking.utils.HotelFormOptions
import kotlinx.coroutines.flow.Flow

class HotelRepository(
    private val firestoreService: FirestoreService = FirestoreService(),
    private val storageService: StorageService = StorageService()
) {
    fun getHotelsFlow(): Flow<List<Hotel>> = firestoreService.getHotelsFlow()

    fun getHotelsByOwnerFlow(ownerId: String): Flow<List<Hotel>> =
        firestoreService.getHotelsByOwnerFlow(ownerId)

    suspend fun getHotel(hotelId: String): Result<Hotel> = firestoreService.getHotel(hotelId)

    suspend fun getHotelsByOwner(ownerId: String): Result<List<Hotel>> =
        firestoreService.getHotelsByOwner(ownerId)

    suspend fun createHotel(hotel: Hotel): Result<String> {
        if (hotel.propietarioId.isNotBlank()) {
            val ownedHotels = getHotelsByOwner(hotel.propietarioId).getOrDefault(emptyList())
            val isNewAssignment = hotel.id.isBlank() ||
                ownedHotels.none { it.id == hotel.id }
            if (isNewAssignment && ownedHotels.size >= HotelFormOptions.MAX_HOTELS_PER_GERENTE) {
                return Result.failure(
                    Exception("Cada encargado solo puede tener un hotel registrado")
                )
            }
        }
        return firestoreService.createHotel(hotel)
    }

    suspend fun updateHotel(hotel: Hotel): Result<Unit> {
        if (hotel.propietarioId.isNotBlank()) {
            val ownedHotels = getHotelsByOwner(hotel.propietarioId).getOrDefault(emptyList())
            val otherHotels = ownedHotels.filter { it.id != hotel.id }
            if (otherHotels.isNotEmpty()) {
                return Result.failure(
                    Exception("Cada encargado solo puede tener un hotel registrado")
                )
            }
        }
        return firestoreService.updateHotel(hotel)
    }

    suspend fun deleteHotel(hotelId: String): Result<Unit> = firestoreService.deleteHotel(hotelId)

    suspend fun syncPrecioMinimoFromRooms(hotelId: String): Result<Unit> {
        val roomsResult = firestoreService.getRoomsByHotel(hotelId)
        val hotelResult = firestoreService.getHotel(hotelId)
        if (roomsResult.isFailure) return Result.failure(roomsResult.exceptionOrNull()!!)
        if (hotelResult.isFailure) return Result.failure(hotelResult.exceptionOrNull()!!)
        val rooms = roomsResult.getOrThrow()
        val hotel = hotelResult.getOrThrow()
        val minPrice = rooms.minOfOrNull { it.precio } ?: hotel.precioMinimo
        return firestoreService.updateHotel(hotel.copy(precioMinimo = minPrice))
    }

    suspend fun uploadHotelImage(uri: Uri): Result<String> = storageService.uploadHotelImage(uri)

    fun searchHotels(
        hotels: List<Hotel>,
        query: String = "",
        ciudad: String = "",
        precioMax: Double? = null,
        estrellasMin: Int = 0
    ): List<Hotel> {
        return hotels.filter { hotel ->
            val matchesQuery = query.isBlank() ||
                hotel.nombre.contains(query, ignoreCase = true) ||
                hotel.ciudad.contains(query, ignoreCase = true)
            val matchesCiudad = ciudad.isBlank() ||
                hotel.ciudad.contains(ciudad, ignoreCase = true)
            val matchesPrecio = precioMax == null || hotel.precioMinimo <= precioMax
            val matchesEstrellas = hotel.estrellas >= estrellasMin
            matchesQuery && matchesCiudad && matchesPrecio && matchesEstrellas
        }
    }
}
