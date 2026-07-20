package com.company.selvabooking.repository

import com.company.selvabooking.data.firebase.FirestoreService
import com.company.selvabooking.domain.model.Reservation
import com.company.selvabooking.domain.model.ReservationStatus
import kotlinx.coroutines.flow.Flow

class ReservationRepository(
    private val firestoreService: FirestoreService = FirestoreService()
) {
    fun getAllReservationsFlow(): Flow<List<Reservation>> =
        firestoreService.getReservationsFlow()

    fun getUserReservationsFlow(userId: String): Flow<List<Reservation>> =
        firestoreService.getUserReservationsFlow(userId)

    suspend fun createReservation(reservation: Reservation): Result<String> =
        firestoreService.createReservation(reservation)

    suspend fun confirmReservation(reservationId: String): Result<Unit> =
        firestoreService.updateReservationStatus(
            reservationId,
            ReservationStatus.CONFIRMADA.value
        )

    suspend fun cancelReservation(reservationId: String): Result<Unit> =
        firestoreService.updateReservationStatus(
            reservationId,
            ReservationStatus.CANCELADA.value
        )

    suspend fun updateReservationStatus(
        reservationId: String,
        status: ReservationStatus
    ): Result<Unit> =
        firestoreService.updateReservationStatus(reservationId, status.value)

    suspend fun updateReservation(reservation: Reservation): Result<Unit> =
        firestoreService.updateReservation(reservation)

    suspend fun deleteReservation(reservationId: String): Result<Unit> =
        firestoreService.deleteReservation(reservationId)
}
