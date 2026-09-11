package com.company.selvabooking.data.firebase

import com.company.selvabooking.domain.model.AuditLog
import com.company.selvabooking.domain.model.Hotel
import com.company.selvabooking.domain.model.Resena
import com.company.selvabooking.domain.model.Reservation
import com.company.selvabooking.domain.model.Room
import com.company.selvabooking.domain.model.User
import com.company.selvabooking.domain.model.UserRole
import com.company.selvabooking.utils.Constants
import com.company.selvabooking.utils.HotelFormOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun createUser(user: User): Result<Unit> = try {
        firestore.collection(Constants.COLLECTION_USUARIOS)
            .document(user.id)
            .set(user.toMap())
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getUser(userId: String): Result<User> = try {
        val doc = firestore.collection(Constants.COLLECTION_USUARIOS)
            .document(userId)
            .get()
            .await()
        if (doc.exists()) {
            Result.success(User.fromMap(doc.id, doc.data ?: emptyMap()))
        } else {
            Result.failure(Exception("Usuario no encontrado"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateUser(user: User): Result<Unit> = try {
        firestore.collection(Constants.COLLECTION_USUARIOS)
            .document(user.id)
            .set(user.toMap())
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteUser(userId: String): Result<Unit> = try {
        firestore.collection(Constants.COLLECTION_USUARIOS)
            .document(userId)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getAllUsers(): Result<List<User>> = try {
        val snapshot = firestore.collection(Constants.COLLECTION_USUARIOS).get().await()
        val users = snapshot.documents.map { User.fromMap(it.id, it.data ?: emptyMap()) }
        Result.success(users)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getAllUsersFlow(): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_USUARIOS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.map {
                    User.fromMap(it.id, it.data ?: emptyMap())
                } ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    fun getPendingAdminRequestsFlow(): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_USUARIOS)
            .whereEqualTo("solicitudAdmin", Constants.ADMIN_REQUEST_PENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.map {
                    User.fromMap(it.id, it.data ?: emptyMap())
                } ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    fun getHotelsFlow(): Flow<List<Hotel>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_HOTELES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val hotels = snapshot?.documents?.map {
                    Hotel.fromMap(it.id, it.data ?: emptyMap())
                } ?: emptyList()
                trySend(hotels)
            }
        awaitClose { listener.remove() }
    }

    fun getHotelsByOwnerFlow(ownerId: String): Flow<List<Hotel>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_HOTELES)
            .whereEqualTo("propietarioId", ownerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val hotels = snapshot?.documents?.map {
                    Hotel.fromMap(it.id, it.data ?: emptyMap())
                } ?: emptyList()
                trySend(hotels)
            }
        awaitClose { listener.remove() }
    }

    fun getAdministradoresFlow(): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_USUARIOS)
            .whereEqualTo("rol", UserRole.ADMINISTRADOR.value)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.map {
                    User.fromMap(it.id, it.data ?: emptyMap())
                } ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    /** Administradores activos o en modo cliente con permiso de alternar rol. */
    fun getAdministratorAccountsFlow(): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_USUARIOS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.map {
                    User.fromMap(it.id, it.data ?: emptyMap())
                }?.filter { it.isAdministratorAccount }
                    ?.sortedBy { it.nombre.lowercase() } ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    fun getGerentesHotelFlow(): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_USUARIOS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.map {
                    User.fromMap(it.id, it.data ?: emptyMap())
                }?.filter { user ->
                    user.rol == UserRole.GERENTE_HOTEL ||
                        UserRole.matchesEncargadoHotel(user.rolAlternativo)
                }?.sortedBy { it.nombre.lowercase() } ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getHotelsByOwner(ownerId: String): Result<List<Hotel>> = try {
        val snapshot = firestore.collection(Constants.COLLECTION_HOTELES)
            .whereEqualTo("propietarioId", ownerId)
            .get()
            .await()
        val hotels = snapshot.documents.map { Hotel.fromMap(it.id, it.data ?: emptyMap()) }
        Result.success(hotels)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getAllHotels(): Result<List<Hotel>> = try {
        val snapshot = firestore.collection(Constants.COLLECTION_HOTELES).get().await()
        val hotels = snapshot.documents.map { Hotel.fromMap(it.id, it.data ?: emptyMap()) }
        Result.success(hotels)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getHotel(hotelId: String): Result<Hotel> = try {
        val doc = firestore.collection(Constants.COLLECTION_HOTELES)
            .document(hotelId)
            .get()
            .await()
        if (doc.exists()) {
            Result.success(Hotel.fromMap(doc.id, doc.data ?: emptyMap()))
        } else {
            Result.failure(Exception("Hotel no encontrado"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createHotel(hotel: Hotel): Result<String> = try {
        val ref = if (hotel.id.isNotEmpty()) {
            firestore.collection(Constants.COLLECTION_HOTELES).document(hotel.id)
        } else {
            firestore.collection(Constants.COLLECTION_HOTELES).document()
        }
        ref.set(hotel.copy(id = ref.id).toMap()).await()
        Result.success(ref.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateHotel(hotel: Hotel): Result<Unit> = try {
        firestore.collection(Constants.COLLECTION_HOTELES)
            .document(hotel.id)
            .set(hotel.toMap())
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteHotel(hotelId: String): Result<Unit> = try {
        val roomsSnapshot = firestore.collection(Constants.COLLECTION_HABITACIONES)
            .whereEqualTo("hotelId", hotelId)
            .get()
            .await()
        roomsSnapshot.documents.forEach { it.reference.delete().await() }
        val resenasSnapshot = firestore.collection(Constants.COLLECTION_RESENAS)
            .whereEqualTo("hotelId", hotelId)
            .get()
            .await()
        resenasSnapshot.documents.forEach { it.reference.delete().await() }
        firestore.collection(Constants.COLLECTION_HOTELES)
            .document(hotelId)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getRoomsByHotelFlow(hotelId: String): Flow<List<Room>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_HABITACIONES)
            .whereEqualTo("hotelId", hotelId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val rooms = snapshot?.documents?.map {
                    Room.fromMap(it.id, it.data ?: emptyMap())
                } ?: emptyList()
                trySend(rooms)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getAllRooms(): Result<List<Room>> = try {
        val snapshot = firestore.collection(Constants.COLLECTION_HABITACIONES).get().await()
        val rooms = snapshot.documents.map { Room.fromMap(it.id, it.data ?: emptyMap()) }
        Result.success(rooms)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createRoom(room: Room): Result<String> = try {
        val ref = if (room.id.isNotEmpty()) {
            firestore.collection(Constants.COLLECTION_HABITACIONES).document(room.id)
        } else {
            firestore.collection(Constants.COLLECTION_HABITACIONES).document()
        }
        ref.set(room.copy(id = ref.id).toMap()).await()
        Result.success(ref.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateRoom(room: Room): Result<Unit> = try {
        firestore.collection(Constants.COLLECTION_HABITACIONES)
            .document(room.id)
            .set(room.toMap())
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteRoom(roomId: String): Result<Unit> = try {
        firestore.collection(Constants.COLLECTION_HABITACIONES)
            .document(roomId)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getRoom(roomId: String): Result<Room> = try {
        val doc = firestore.collection(Constants.COLLECTION_HABITACIONES)
            .document(roomId)
            .get()
            .await()
        if (!doc.exists()) {
            Result.failure(Exception("Habitación no encontrada"))
        } else {
            Result.success(Room.fromMap(doc.id, doc.data ?: emptyMap()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun migrateOneHotelPerGerente(): Result<Unit> {
        return try {
            val hotels = getAllHotels().getOrElse { return Result.failure(it) }
            val byOwner = hotels
                .filter { it.propietarioId.isNotBlank() }
                .groupBy { it.propietarioId }
            for ((_, ownerHotels) in byOwner) {
                if (ownerHotels.size <= HotelFormOptions.MAX_HOTELS_PER_GERENTE) continue
                val keepHotel = ownerHotels.minWith(
                    compareBy<Hotel> { it.nombre.lowercase() }.thenBy { it.id }
                )
                ownerHotels
                    .filter { it.id != keepHotel.id }
                    .forEach { hotel ->
                        updateHotel(hotel.copy(propietarioId = "")).getOrElse {
                            return Result.failure(it)
                        }
                    }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun migrateGerenteCreatorAssignments(): Result<Unit> {
        return try {
            val users = getAllUsers().getOrElse { return Result.failure(it) }
            val remied = users.find { it.email.equals(Constants.ADMIN_REMED_EMAIL, ignoreCase = true) }
            val lizy = users.find { it.email.equals(Constants.ADMIN_LIZY_EMAIL, ignoreCase = true) }
            if (remied == null || lizy == null) {
                return Result.success(Unit)
            }
            val gerentes = users.filter { user ->
                user.rol == UserRole.GERENTE_HOTEL ||
                    UserRole.matchesEncargadoHotel(user.rolAlternativo)
            }.sortedBy { it.email.lowercase() }
            for ((index, gerente) in gerentes.withIndex()) {
                if (gerente.creadoPorAdminId.isNotBlank()) continue
                val adminId = if (index % 2 == 0) remied.id else lizy.id
                updateUser(gerente.copy(creadoPorAdminId = adminId)).getOrElse {
                    return Result.failure(it)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun migrateRoomStockFields(): Result<Unit> = try {
        val snapshot = firestore.collection(Constants.COLLECTION_HABITACIONES).get().await()
        snapshot.documents.forEach { doc ->
            val data = doc.data ?: return@forEach
            val updates = mutableMapOf<String, Any>()
            if (!data.containsKey("cantidad")) updates["cantidad"] = 1
            if (!data.containsKey("stock")) updates["stock"] = 1
            if (updates.isNotEmpty()) {
                doc.reference.update(updates).await()
            }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getAllReservations(): Result<List<Reservation>> = try {
        val snapshot = firestore.collection(Constants.COLLECTION_RESERVAS).get().await()
        val reservations = snapshot.documents.map {
            Reservation.fromMap(it.id, it.data ?: emptyMap())
        }
        Result.success(reservations)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getReservationsFlow(): Flow<List<Reservation>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_RESERVAS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val reservations = snapshot?.documents?.map {
                    Reservation.fromMap(it.id, it.data ?: emptyMap())
                } ?: emptyList()
                trySend(reservations)
            }
        awaitClose { listener.remove() }
    }

    fun getUserReservationsFlow(userId: String): Flow<List<Reservation>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_RESERVAS)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val reservations = snapshot?.documents?.map {
                    Reservation.fromMap(it.id, it.data ?: emptyMap())
                }?.sortedByDescending { it.createdAt } ?: emptyList()
                trySend(reservations)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createReservation(reservation: Reservation): Result<String> = try {
        val ref = firestore.collection(Constants.COLLECTION_RESERVAS).document()
        ref.set(reservation.copy(id = ref.id).toMap()).await()
        Result.success(ref.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateReservationStatus(
        reservationId: String,
        status: String
    ): Result<Unit> = try {
        firestore.collection(Constants.COLLECTION_RESERVAS)
            .document(reservationId)
            .update("estado", status)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateReservation(reservation: Reservation): Result<Unit> = try {
        firestore.collection(Constants.COLLECTION_RESERVAS)
            .document(reservation.id)
            .set(reservation.toMap())
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteReservation(reservationId: String): Result<Unit> = try {
        firestore.collection(Constants.COLLECTION_RESERVAS)
            .document(reservationId)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getReservation(reservationId: String): Result<Reservation> = try {
        val doc = firestore.collection(Constants.COLLECTION_RESERVAS)
            .document(reservationId)
            .get()
            .await()
        if (!doc.exists()) {
            Result.failure(Exception("Reserva no encontrada"))
        } else {
            Result.success(Reservation.fromMap(doc.id, doc.data ?: emptyMap()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getRoomsByHotel(hotelId: String): Result<List<Room>> = try {
        val snapshot = firestore.collection(Constants.COLLECTION_HABITACIONES)
            .whereEqualTo("hotelId", hotelId)
            .get()
            .await()
        val rooms = snapshot.documents.map { Room.fromMap(it.id, it.data ?: emptyMap()) }
        Result.success(rooms)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getResenasByHotelFlow(hotelId: String): Flow<List<Resena>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_RESENAS)
            .whereEqualTo("hotelId", hotelId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val resenas = snapshot?.documents?.map {
                    Resena.fromMap(it.id, it.data ?: emptyMap())
                }?.sortedByDescending { it.createdAt } ?: emptyList()
                trySend(resenas)
            }
        awaitClose { listener.remove() }
    }

    fun getAllResenasFlow(): Flow<List<Resena>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_RESENAS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val resenas = snapshot?.documents?.map {
                    Resena.fromMap(it.id, it.data ?: emptyMap())
                } ?: emptyList()
                trySend(resenas)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getResenasByHotel(hotelId: String): Result<List<Resena>> = try {
        val snapshot = firestore.collection(Constants.COLLECTION_RESENAS)
            .whereEqualTo("hotelId", hotelId)
            .get()
            .await()
        val resenas = snapshot.documents.map {
            Resena.fromMap(it.id, it.data ?: emptyMap())
        }.sortedByDescending { it.createdAt }
        Result.success(resenas)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createResena(resena: Resena): Result<String> = try {
        val ref = firestore.collection(Constants.COLLECTION_RESENAS).document()
        ref.set(resena.copy(id = ref.id).toMap()).await()
        Result.success(ref.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateResena(resena: Resena): Result<Unit> = try {
        firestore.collection(Constants.COLLECTION_RESENAS)
            .document(resena.id)
            .set(resena.toMap())
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteResena(resenaId: String): Result<Unit> = try {
        firestore.collection(Constants.COLLECTION_RESENAS)
            .document(resenaId)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getAuditLogsFlow(): Flow<List<AuditLog>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_AUDIT_LOGS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val logs = snapshot?.documents?.map {
                    AuditLog.fromMap(it.id, it.data ?: emptyMap())
                } ?: emptyList()
                trySend(logs)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createAuditLog(log: AuditLog): Result<String> = try {
        val ref = firestore.collection(Constants.COLLECTION_AUDIT_LOGS).document()
        ref.set(log.copy(id = ref.id).toMap()).await()
        Result.success(ref.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getAuditLog(logId: String): Result<AuditLog> = try {
        val doc = firestore.collection(Constants.COLLECTION_AUDIT_LOGS)
            .document(logId)
            .get()
            .await()
        if (doc.exists()) {
            Result.success(AuditLog.fromMap(doc.id, doc.data ?: emptyMap()))
        } else {
            Result.failure(Exception("Registro de auditoría no encontrado"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun markAuditLogReverted(logId: String, adminUserId: String): Result<Unit> = try {
        firestore.collection(Constants.COLLECTION_AUDIT_LOGS)
            .document(logId)
            .update(
                mapOf(
                    "reverted" to true,
                    "revertedAt" to System.currentTimeMillis(),
                    "revertedBy" to adminUserId
                )
            )
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
