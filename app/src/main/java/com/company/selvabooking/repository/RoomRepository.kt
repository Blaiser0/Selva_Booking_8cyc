package com.company.selvabooking.repository

import android.net.Uri
import com.company.selvabooking.data.firebase.FirestoreService
import com.company.selvabooking.data.firebase.StorageService
import com.company.selvabooking.domain.model.Room
import kotlinx.coroutines.flow.Flow

class RoomRepository(
    private val firestoreService: FirestoreService = FirestoreService(),
    private val storageService: StorageService = StorageService()
) {
    fun getRoomsByHotelFlow(hotelId: String): Flow<List<Room>> =
        firestoreService.getRoomsByHotelFlow(hotelId)

    suspend fun getAllRooms(): Result<List<Room>> = firestoreService.getAllRooms()

    suspend fun getRoomsByHotel(hotelId: String): Result<List<Room>> =
        firestoreService.getRoomsByHotel(hotelId)

    suspend fun createRoom(room: Room): Result<String> = firestoreService.createRoom(room)

    suspend fun updateRoom(room: Room): Result<Unit> = firestoreService.updateRoom(room)

    suspend fun deleteRoom(roomId: String): Result<Unit> = firestoreService.deleteRoom(roomId)

    suspend fun uploadRoomImage(uri: Uri): Result<String> = storageService.uploadRoomImage(uri)
}
