package com.company.selvabooking.domain.model

data class Hotel(
    val id: String = "",
    val nombre: String = "",
    val ciudad: String = "",
    val direccion: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val estrellas: Int = 0,
    val precioMinimo: Double = 0.0,
    val calificacion: Double = 0.0,
    val calificacionBase: Double = 0.0,
    val imagenes: List<String> = emptyList(),
    val servicios: List<String> = emptyList(),
    val ubicacion: String = "",
    val destacado: Boolean = false,
    val oferta: Boolean = false
) {
    fun effectiveBaseRating(): Double =
        if (calificacionBase > 0.0) calificacionBase else calificacion

    fun toMap(): Map<String, Any> = mapOf(
        "nombre" to nombre,
        "ciudad" to ciudad,
        "direccion" to direccion,
        "descripcion" to descripcion,
        "categoria" to categoria,
        "estrellas" to estrellas,
        "precioMinimo" to precioMinimo,
        "calificacion" to calificacion,
        "calificacionBase" to effectiveBaseRating(),
        "imagenes" to imagenes,
        "servicios" to servicios,
        "ubicacion" to ubicacion,
        "destacado" to destacado,
        "oferta" to oferta
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(id: String, map: Map<String, Any?>): Hotel = Hotel(
            id = id,
            nombre = map["nombre"] as? String ?: "",
            ciudad = map["ciudad"] as? String ?: "",
            direccion = map["direccion"] as? String ?: "",
            descripcion = map["descripcion"] as? String ?: "",
            categoria = map["categoria"] as? String ?: "",
            estrellas = (map["estrellas"] as? Long)?.toInt() ?: 0,
            precioMinimo = (map["precioMinimo"] as? Number)?.toDouble() ?: 0.0,
            calificacion = (map["calificacion"] as? Number)?.toDouble() ?: 0.0,
            calificacionBase = (map["calificacionBase"] as? Number)?.toDouble()
                ?: (map["calificacion"] as? Number)?.toDouble()
                ?: 0.0,
            imagenes = (map["imagenes"] as? List<String>) ?: emptyList(),
            servicios = (map["servicios"] as? List<String>) ?: emptyList(),
            ubicacion = map["ubicacion"] as? String ?: "",
            destacado = map["destacado"] as? Boolean ?: false,
            oferta = map["oferta"] as? Boolean ?: false
        )
    }
}
