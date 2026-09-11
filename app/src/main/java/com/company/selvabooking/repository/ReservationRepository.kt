package com.company.selvabooking.repository

import com.company.selvabooking.data.firebase.FirestoreService
import com.company.selvabooking.domain.model.Reservation
import com.company.selvabooking.domain.model.ReservationStatus
import com.company.selvabooking.utils.DateUtils
import kotlinx.coroutines.flow.Flow

class ReservationRepository(
    private val firestoreService: FirestoreService = FirestoreService(),
    private val roomRepository: RoomRepository = RoomRepository()
) {
    fun getAllReservationsFlow(): Flow<List<Reservation>> =
        firestoreService.getReservationsFlow()

    fun getUserReservationsFlow(userId: String): Flow<List<Reservation>> =
        firestoreService.getUserReservationsFlow(userId)

    suspend fun expireFinishedReservations() {
        val reservations = firestoreService.getAllReservations().getOrElse { emptyList() }
        reservations
            .filter {
                it.estado == ReservationStatus.CONFIRMADA &&
                    DateUtils.isCheckoutPast(it.fechaSalida)
            }
            .forEach { reservation ->
                updateReservationStatus(reservation.id, ReservationStatus.TERMINADA)
            }
    }

    suspend fun createReservation(reservation: Reservation): Result<String> {
        if (reservation.estado.holdsStock()) {
            val room = roomRepository.getRoom(reservation.roomId).getOrElse { return Result.failure(it) }
            if (!room.isReservable) {
                return Result.failure(Exception("No hay habitaciones disponibles de este tipo"))
            }
        }
        return firestoreService.createReservation(reservation).fold(
            onSuccess = { id ->
                if (reservation.estado.holdsStock()) {
                    roomRepository.decrementStock(reservation.roomId).getOrElse { error ->
                        firestoreService.deleteReservation(id)
                        return Result.failure(error)
                    }
                }
                Result.success(id)
            },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun confirmReservation(reservationId: String): Result<Unit> =
        updateReservationStatus(reservationId, ReservationStatus.CONFIRMADA)

    suspend fun terminateReservation(reservationId: String): Result<Unit> =
        updateReservationStatus(reservationId, ReservationStatus.TERMINADA)

    suspend fun updateReservationStatus(
        reservationId: String,
        status: ReservationStatus
    ): Result<Unit> {
        val reservation = firestoreService.getReservation(reservationId).getOrElse {
            return Result.failure(it)
        }
        val oldStatus = reservation.estado
        if (oldStatus == status) return Result.success(Unit)

        if (!oldStatus.holdsStock() && status.holdsStock()) {
            val room = roomRepository.getRoom(reservation.roomId).getOrElse { return Result.failure(it) }
            if (!room.isReservable) {
                return Result.failure(Exception("No hay habitaciones disponibles de este tipo"))
            }
        }

        return firestoreService.updateReservationStatus(reservationId, status.value).fold(
            onSuccess = {
                when {
                    oldStatus.holdsStock() && !status.holdsStock() ->
                        roomRepository.incrementStock(reservation.roomId)
                    !oldStatus.holdsStock() && status.holdsStock() ->
                        roomRepository.decrementStock(reservation.roomId)
                }
                Result.success(Unit)
            },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun updateReservation(reservation: Reservation): Result<Unit> =
        firestoreService.updateReservation(reservation)

    suspend fun deleteReservation(reservationId: String): Result<Unit> {
        val reservation = firestoreService.getReservation(reservationId).getOrElse {
            return Result.failure(it)
        }
        return firestoreService.deleteReservation(reservationId).fold(
            onSuccess = {
                if (reservation.estado.holdsStock()) {
                    roomRepository.incrementStock(reservation.roomId)
                }
                Result.success(Unit)
            },
            onFailure = { Result.failure(it) }
        )
    }
}
