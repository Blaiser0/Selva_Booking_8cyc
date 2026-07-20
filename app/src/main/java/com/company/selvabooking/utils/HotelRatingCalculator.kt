package com.company.selvabooking.utils

object HotelRatingCalculator {

    /**
     * Impacto de cada reseña sobre la calificación base del hotel:
     * - 1-2 estrellas: baja
     * - 3 estrellas: se mantiene
     * - 4-5 estrellas: sube
     */
    private val STAR_DELTA = mapOf(
        1 to -0.6,
        2 to -0.3,
        3 to 0.0,
        4 to 0.3,
        5 to 0.6
    )

    fun computeDisplayedRating(baseRating: Double, reviewStarRatings: List<Int>): Double {
        if (reviewStarRatings.isEmpty()) return baseRating.coerceIn(1.0, 10.0)

        val totalDelta = reviewStarRatings.sumOf { stars ->
            STAR_DELTA[stars.coerceIn(1, 5)] ?: 0.0
        }
        return (baseRating + totalDelta).coerceIn(1.0, 10.0)
    }
}
