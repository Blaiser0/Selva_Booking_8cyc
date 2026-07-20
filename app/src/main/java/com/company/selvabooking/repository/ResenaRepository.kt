package com.company.selvabooking.repository

import com.company.selvabooking.data.firebase.FirestoreService
import com.company.selvabooking.domain.model.Resena
import com.company.selvabooking.domain.model.Reservation
import com.company.selvabooking.utils.HotelRatingCalculator
import kotlinx.coroutines.flow.Flow

class ResenaRepository(
    private val firestoreService: FirestoreService = FirestoreService(),
    private val hotelRepository: HotelRepository = HotelRepository()
) {
    fun getResenasByHotelFlow(hotelId: String): Flow<List<Resena>> =
        firestoreService.getResenasByHotelFlow(hotelId)

    suspend fun createResena(resena: Resena): Result<String> {
        return firestoreService.createResena(resena).also { result ->
            if (result.isSuccess) {
                syncHotelRating(resena.hotelId)
            }
        }
    }

    suspend fun updateResena(resena: Resena): Result<Unit> {
        return firestoreService.updateResena(resena).also { result ->
            if (result.isSuccess) {
                syncHotelRating(resena.hotelId)
            }
        }
    }

    suspend fun deleteResena(resenaId: String, hotelId: String): Result<Unit> {
        return firestoreService.deleteResena(resenaId).also { result ->
            if (result.isSuccess) {
                syncHotelRating(hotelId)
            }
        }
    }

    suspend fun refreshHotelRating(hotelId: String) {
        syncHotelRating(hotelId)
    }

    fun userHasEligibleReservation(
        reservations: List<Reservation>,
        hotelId: String
    ): Boolean {
        return reservations.any { it.hotelId == hotelId }
    }

    fun findEligibleReservation(
        reservations: List<Reservation>,
        hotelId: String
    ): Reservation? {
        return reservations
            .filter { it.hotelId == hotelId }
            .maxByOrNull { it.createdAt }
    }

    private suspend fun syncHotelRating(hotelId: String) {
        val resenas = firestoreService.getResenasByHotel(hotelId).getOrElse { emptyList() }
        val hotel = hotelRepository.getHotel(hotelId).getOrNull() ?: return
        val baseRating = hotel.effectiveBaseRating()

        val newRating = if (resenas.isEmpty()) {
            baseRating
        } else {
            HotelRatingCalculator.computeDisplayedRating(
                baseRating = baseRating,
                reviewStarRatings = resenas.map { it.calificacion }
            )
        }

        hotelRepository.updateHotel(
            hotel.copy(
                calificacion = newRating,
                calificacionBase = baseRating
            )
        )
    }
}
