package com.company.selvabooking.domain.model

data class Room(
    val id: String = "",
    val hotelId: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val capacidad: Int = 1,
    val cantidad: Int = 1,
    val stock: Int = 1,
    val disponible: Boolean = true,
    val imagenes: List<String> = emptyList()
) {
    val isReservable: Boolean get() = disponible && stock > 0

    fun toMap(): Map<String, Any> = mapOf(
        "hotelId" to hotelId,
        "nombre" to nombre,
        "descripcion" to descripcion,
        "precio" to precio,
        "capacidad" to capacidad,
        "cantidad" to cantidad,
        "stock" to stock,
        "disponible" to disponible,
        "imagenes" to imagenes
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(id: String, map: Map<String, Any?>): Room {
            val cantidad = (map["cantidad"] as? Number)?.toInt()?.coerceAtLeast(1) ?: 1
            val stock = (map["stock"] as? Number)?.toInt()?.coerceIn(0, cantidad)
                ?: cantidad
            return Room(
                id = id,
                hotelId = map["hotelId"] as? String ?: "",
                nombre = map["nombre"] as? String ?: "",
                descripcion = map["descripcion"] as? String ?: "",
                precio = (map["precio"] as? Number)?.toDouble() ?: 0.0,
                capacidad = (map["capacidad"] as? Long)?.toInt() ?: 1,
                cantidad = cantidad,
                stock = stock,
                disponible = map["disponible"] as? Boolean ?: true,
                imagenes = (map["imagenes"] as? List<String>) ?: emptyList()
            )
        }
    }
}
