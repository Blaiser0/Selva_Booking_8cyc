package com.company.selvabooking.domain.model

data class Reservation(
    val id: String = "",
    val userId: String = "",
    val hotelId: String = "",
    val roomId: String = "",
    val hotelNombre: String = "",
    val roomNombre: String = "",
    val userNombre: String = "",
    val userEmail: String = "",
    val userTelefono: String = "",
    val fechaIngreso: String = "",
    val fechaSalida: String = "",
    val huespedes: Int = 1,
    val precioTotal: Double = 0.0,
    val estado: ReservationStatus = ReservationStatus.PENDIENTE,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "hotelId" to hotelId,
        "roomId" to roomId,
        "hotelNombre" to hotelNombre,
        "roomNombre" to roomNombre,
        "userNombre" to userNombre,
        "userEmail" to userEmail,
        "userTelefono" to userTelefono,
        "fechaIngreso" to fechaIngreso,
        "fechaSalida" to fechaSalida,
        "huespedes" to huespedes,
        "precioTotal" to precioTotal,
        "estado" to estado.value,
        "createdAt" to createdAt
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Reservation = Reservation(
            id = id,
            userId = map["userId"] as? String ?: "",
            hotelId = map["hotelId"] as? String ?: "",
            roomId = map["roomId"] as? String ?: "",
            hotelNombre = map["hotelNombre"] as? String ?: "",
            roomNombre = map["roomNombre"] as? String ?: "",
            userNombre = map["userNombre"] as? String ?: "",
            userEmail = map["userEmail"] as? String ?: "",
            userTelefono = map["userTelefono"] as? String ?: "",
            fechaIngreso = map["fechaIngreso"] as? String ?: "",
            fechaSalida = map["fechaSalida"] as? String ?: "",
            huespedes = (map["huespedes"] as? Long)?.toInt() ?: 1,
            precioTotal = (map["precioTotal"] as? Number)?.toDouble() ?: 0.0,
            estado = ReservationStatus.fromString(map["estado"] as? String ?: ""),
            createdAt = (map["createdAt"] as? Long) ?: System.currentTimeMillis()
        )
    }
}
