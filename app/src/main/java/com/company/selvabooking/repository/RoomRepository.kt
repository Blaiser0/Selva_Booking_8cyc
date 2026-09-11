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

    suspend fun getRoom(roomId: String): Result<Room> = firestoreService.getRoom(roomId)

    suspend fun createRoom(room: Room): Result<String> {
        val cantidad = room.cantidad.coerceAtLeast(1)
        return firestoreService.createRoom(room.copy(cantidad = cantidad, stock = cantidad))
    }

    suspend fun updateRoom(room: Room): Result<Unit> = firestoreService.updateRoom(room)

    suspend fun deleteRoom(roomId: String): Result<Unit> = firestoreService.deleteRoom(roomId)

    suspend fun uploadRoomImage(uri: Uri): Result<String> = storageService.uploadRoomImage(uri)

    suspend fun migrateRoomStockFields(): Result<Unit> =
        firestoreService.migrateRoomStockFields()

    suspend fun decrementStock(roomId: String): Result<Unit> {
        val room = getRoom(roomId).getOrElse { return Result.failure(it) }
        if (room.stock <= 0) {
            return Result.failure(Exception("No hay habitaciones disponibles de este tipo"))
        }
        return updateRoom(room.copy(stock = room.stock - 1))
    }

    suspend fun incrementStock(roomId: String): Result<Unit> {
        val room = getRoom(roomId).getOrElse { return Result.failure(it) }
        if (room.stock >= room.cantidad) return Result.success(Unit)
        return updateRoom(room.copy(stock = room.stock + 1))
    }
}
