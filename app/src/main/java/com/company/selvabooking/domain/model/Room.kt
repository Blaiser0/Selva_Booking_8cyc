package com.company.selvabooking.domain.model

data class Room(
    val id: String = "",
    val hotelId: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val capacidad: Int = 1,
    val disponible: Boolean = true,
    val imagenes: List<String> = emptyList()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "hotelId" to hotelId,
        "nombre" to nombre,
        "descripcion" to descripcion,
        "precio" to precio,
        "capacidad" to capacidad,
        "disponible" to disponible,
        "imagenes" to imagenes
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(id: String, map: Map<String, Any?>): Room = Room(
            id = id,
            hotelId = map["hotelId"] as? String ?: "",
            nombre = map["nombre"] as? String ?: "",
            descripcion = map["descripcion"] as? String ?: "",
            precio = (map["precio"] as? Number)?.toDouble() ?: 0.0,
            capacidad = (map["capacidad"] as? Long)?.toInt() ?: 1,
            disponible = map["disponible"] as? Boolean ?: true,
            imagenes = (map["imagenes"] as? List<String>) ?: emptyList()
        )
    }
}
