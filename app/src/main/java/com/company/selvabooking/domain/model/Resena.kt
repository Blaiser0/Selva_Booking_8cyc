package com.company.selvabooking.domain.model

data class Resena(
    val id: String = "",
    val hotelId: String = "",
    val userId: String = "",
    val reservationId: String = "",
    val userNombre: String = "",
    val calificacion: Int = 5,
    val comentario: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "hotelId" to hotelId,
        "userId" to userId,
        "reservationId" to reservationId,
        "userNombre" to userNombre,
        "calificacion" to calificacion,
        "comentario" to comentario,
        "createdAt" to createdAt
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Resena = Resena(
            id = id,
            hotelId = map["hotelId"] as? String ?: "",
            userId = map["userId"] as? String ?: "",
            reservationId = map["reservationId"] as? String ?: "",
            userNombre = map["userNombre"] as? String ?: "",
            calificacion = when (val value = map["calificacion"]) {
                is Long -> value.toInt()
                is Int -> value
                is Double -> value.toInt()
                else -> 5
            }.coerceIn(1, 5),
            comentario = map["comentario"] as? String ?: "",
            createdAt = (map["createdAt"] as? Long) ?: 0L
        )
    }
}
