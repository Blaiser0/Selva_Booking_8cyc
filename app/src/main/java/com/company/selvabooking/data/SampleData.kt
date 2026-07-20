package com.company.selvabooking.data

import com.company.selvabooking.domain.model.Hotel
import com.company.selvabooking.domain.model.Room

object SampleData {

    val hotels = listOf(
        Hotel(
            nombre = "Eco Lodge Selva Verde",
            ciudad = "Puerto Maldonado",
            direccion = "Av. La Marina 245, Madre de Dios",
            descripcion = "Lodge ecológico en el corazón de la Amazonía peruana. " +
                "Disfruta de la naturaleza con todas las comodidades modernas.",
            categoria = "Ecológico",
            estrellas = 5,
            precioMinimo = 280.0,
            calificacion = 4.8,
            calificacionBase = 4.8,
            imagenes = listOf(
                "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800",
                "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800"
            ),
            servicios = listOf(
                "WiFi", "Restaurante", "Piscina natural", "Guía turístico",
                "Traslado aeropuerto", "Desayuno incluido"
            ),
            ubicacion = "-12.5933, -69.1891",
            destacado = true,
            oferta = true
        ),
        Hotel(
            nombre = "Amazonia Rainforest Resort",
            ciudad = "Tambopata",
            direccion = "Reserva Tambopata Km 45",
            descripcion = "Resort de lujo rodeado de selva virgen. Experiencia única " +
                "con avistamiento de fauna silvestre.",
            categoria = "Lujo",
            estrellas = 5,
            precioMinimo = 450.0,
            calificacion = 4.9,
            calificacionBase = 4.9,
            imagenes = listOf(
                "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800",
                "https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=800"
            ),
            servicios = listOf(
                "Spa", "Restaurante gourmet", "Kayak", "Observación de aves",
                "Bar", "Servicio de habitaciones 24h"
            ),
            ubicacion = "-13.1333, -69.6500",
            destacado = true,
            oferta = false
        ),
        Hotel(
            nombre = "Cabañas Madre de Dios",
            ciudad = "Puerto Maldonado",
            direccion = "Jr. Cusco 128",
            descripcion = "Cabañas rústicas y acogedoras ideales para viajeros " +
                "que buscan una experiencia auténtica en la selva.",
            categoria = "Económico",
            estrellas = 3,
            precioMinimo = 120.0,
            calificacion = 4.2,
            calificacionBase = 4.2,
            imagenes = listOf(
                "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800"
            ),
            servicios = listOf("WiFi", "Desayuno", "Jardín", "Estacionamiento"),
            ubicacion = "-12.6000, -69.1833",
            destacado = false,
            oferta = true
        ),
        Hotel(
            nombre = "Tambopata Jungle Lodge",
            ciudad = "Tambopata",
            direccion = "Río Tambopata s/n",
            descripcion = "Lodge accesible solo por río, perfecto para exploradores " +
                "y amantes de la biodiversidad amazónica.",
            categoria = "Aventura",
            estrellas = 4,
            precioMinimo = 200.0,
            calificacion = 4.5,
            calificacionBase = 4.5,
            imagenes = listOf(
                "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800"
            ),
            servicios = listOf(
                "Excursiones", "Canopy", "Pesca deportiva", "Comedor",
                "Energía solar"
            ),
            ubicacion = "-13.2000, -69.7000",
            destacado = true,
            oferta = false
        )
    )

    fun roomsForHotel(hotelId: String, hotelName: String): List<Room> = when {
        hotelName.contains("Eco Lodge") -> listOf(
            Room(hotelId = hotelId, nombre = "Suite Selva", descripcion = "Suite con vista al bosque", precio = 380.0, capacidad = 2),
            Room(hotelId = hotelId, nombre = "Cabaña Estándar", descripcion = "Cabaña cómoda con terraza", precio = 280.0, capacidad = 3),
            Room(hotelId = hotelId, nombre = "Habitación Familiar", descripcion = "Ideal para familias", precio = 450.0, capacidad = 5)
        )
        hotelName.contains("Amazonia") -> listOf(
            Room(hotelId = hotelId, nombre = "Villa Premium", descripcion = "Villa privada con piscina", precio = 650.0, capacidad = 2),
            Room(hotelId = hotelId, nombre = "Suite Deluxe", descripcion = "Suite de lujo con jacuzzi", precio = 550.0, capacidad = 2),
            Room(hotelId = hotelId, nombre = "Habitación Superior", descripcion = "Habitación con balcón", precio = 450.0, capacidad = 3)
        )
        hotelName.contains("Cabañas") -> listOf(
            Room(hotelId = hotelId, nombre = "Cabaña Simple", descripcion = "Cabaña básica y acogedora", precio = 120.0, capacidad = 2),
            Room(hotelId = hotelId, nombre = "Cabaña Doble", descripcion = "Cabaña para dos personas", precio = 180.0, capacidad = 2)
        )
        else -> listOf(
            Room(hotelId = hotelId, nombre = "Habitación Estándar", descripcion = "Habitación con ventilador", precio = 200.0, capacidad = 2),
            Room(hotelId = hotelId, nombre = "Bungalow", descripcion = "Bungalow independiente", precio = 320.0, capacidad = 4)
        )
    }
}
