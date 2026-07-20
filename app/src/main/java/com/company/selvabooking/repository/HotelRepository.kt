package com.company.selvabooking.repository

import android.net.Uri
import com.company.selvabooking.data.firebase.FirestoreService
import com.company.selvabooking.data.firebase.StorageService
import com.company.selvabooking.domain.model.Hotel
import kotlinx.coroutines.flow.Flow

class HotelRepository(
    private val firestoreService: FirestoreService = FirestoreService(),
    private val storageService: StorageService = StorageService()
) {
    fun getHotelsFlow(): Flow<List<Hotel>> = firestoreService.getHotelsFlow()

    suspend fun getHotel(hotelId: String): Result<Hotel> = firestoreService.getHotel(hotelId)

    suspend fun createHotel(hotel: Hotel): Result<String> = firestoreService.createHotel(hotel)

    suspend fun updateHotel(hotel: Hotel): Result<Unit> = firestoreService.updateHotel(hotel)

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
