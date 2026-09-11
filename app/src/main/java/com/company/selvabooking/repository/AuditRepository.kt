package com.company.selvabooking.repository

import com.company.selvabooking.data.firebase.FirestoreService
import com.company.selvabooking.domain.model.AuditAction
import com.company.selvabooking.domain.model.AuditEntityType
import com.company.selvabooking.domain.model.AuditLog
import com.company.selvabooking.domain.model.Hotel
import com.company.selvabooking.domain.model.Room
import com.company.selvabooking.domain.model.User
import com.company.selvabooking.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

class AuditRepository(
    private val firestoreService: FirestoreService = FirestoreService(),
    private val hotelRepository: HotelRepository = HotelRepository(firestoreService),
    private val roomRepository: RoomRepository = RoomRepository(firestoreService)
) {
    fun getAuditLogsFlow(): Flow<List<AuditLog>> = firestoreService.getAuditLogsFlow()

    suspend fun logEncargadoChange(
        actor: User,
        action: AuditAction,
        entityType: AuditEntityType,
        entityId: String,
        entityLabel: String,
        hotelId: String = "",
        beforeData: Map<String, Any?> = emptyMap(),
        afterData: Map<String, Any?> = emptyMap(),
        relatedData: List<Map<String, Any?>> = emptyList()
    ): Result<String> {
        if (actor.rol != UserRole.GERENTE_HOTEL) return Result.success("")
        val log = AuditLog(
            actorUserId = actor.id,
            actorName = actor.nombre,
            actorEmail = actor.email,
            action = action,
            entityType = entityType,
            entityId = entityId,
            entityLabel = entityLabel,
            hotelId = hotelId.ifBlank { entityId.takeIf { entityType == AuditEntityType.HOTEL } ?: "" },
            beforeData = beforeData,
            afterData = afterData,
            relatedData = relatedData
        )
        return firestoreService.createAuditLog(log)
    }

    suspend fun revertChange(logId: String, adminUserId: String): Result<Unit> {
        val log = firestoreService.getAuditLog(logId).getOrElse { return Result.failure(it) }
        if (log.reverted) {
            return Result.failure(Exception("Este cambio ya fue revertido"))
        }

        val revertResult = when (log.action) {
            AuditAction.CREATE -> revertCreate(log)
            AuditAction.UPDATE -> revertUpdate(log)
            AuditAction.DELETE -> revertDelete(log)
        }
        if (revertResult.isFailure) return revertResult

        return firestoreService.markAuditLogReverted(logId, adminUserId)
    }

    private suspend fun revertCreate(log: AuditLog): Result<Unit> = when (log.entityType) {
        AuditEntityType.HOTEL -> hotelRepository.deleteHotel(log.entityId)
        AuditEntityType.ROOM -> {
            roomRepository.deleteRoom(log.entityId).also { result ->
                if (result.isSuccess && log.hotelId.isNotBlank()) {
                    hotelRepository.syncPrecioMinimoFromRooms(log.hotelId)
                }
            }
        }
    }

    private suspend fun revertUpdate(log: AuditLog): Result<Unit> {
        return when (log.entityType) {
            AuditEntityType.HOTEL -> {
                if (log.beforeData.isEmpty()) {
                    Result.failure(Exception("No hay respaldo del estado anterior"))
                } else {
                    hotelRepository.updateHotel(AuditLog.hotelFromSnapshot(log.beforeData))
                }
            }
            AuditEntityType.ROOM -> {
                if (log.beforeData.isEmpty()) {
                    Result.failure(Exception("No hay respaldo del estado anterior"))
                } else {
                    val room = AuditLog.roomFromSnapshot(log.beforeData)
                    roomRepository.updateRoom(room).also { result ->
                        if (result.isSuccess && room.hotelId.isNotBlank()) {
                            hotelRepository.syncPrecioMinimoFromRooms(room.hotelId)
                        }
                    }
                }
            }
        }
    }

    private suspend fun revertDelete(log: AuditLog): Result<Unit> {
        return when (log.entityType) {
            AuditEntityType.HOTEL -> {
                if (log.beforeData.isEmpty()) {
                    Result.failure(Exception("No hay respaldo del hotel eliminado"))
                } else {
                    val hotel = AuditLog.hotelFromSnapshot(log.beforeData)
                    hotelRepository.createHotel(hotel).getOrElse { return Result.failure(it) }
                    log.relatedData.forEach { roomMap ->
                        val room = AuditLog.roomFromSnapshot(roomMap)
                        roomRepository.createRoom(room).getOrElse { return Result.failure(it) }
                    }
                    Result.success(Unit)
                }
            }
            AuditEntityType.ROOM -> {
                if (log.beforeData.isEmpty()) {
                    Result.failure(Exception("No hay respaldo de la habitación eliminada"))
                } else {
                    val room = AuditLog.roomFromSnapshot(log.beforeData)
                    roomRepository.createRoom(room).getOrElse { return Result.failure(it) }
                    hotelRepository.syncPrecioMinimoFromRooms(room.hotelId)
                }
            }
        }
    }
}
