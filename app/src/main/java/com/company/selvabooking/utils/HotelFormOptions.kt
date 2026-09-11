package com.company.selvabooking.utils

import java.text.Collator
import java.util.Locale

object HotelFormOptions {

    private val spanishCollator = Collator.getInstance(Locale.forLanguageTag("es-PE"))

    private val cityNames = listOf(
        "Boca Colorado",
        "Fitzcarrald",
        "Huepetuhe",
        "Iberia",
        "Iñapari",
        "Mazuko",
        "Planchón",
        "Puerto Maldonado",
        "Puerto Rosario de Laberinto",
        "Salvación",
        "San Lorenzo"
    )

    val cities: List<String> = cityNames.sortedWith(compareBy(spanishCollator) { it })

    val categories: List<String> = listOf(
        "Boutique",
        "Ecológico",
        "Económico",
        "Estándar",
        "Lujo",
        "Resort"
    ).sortedWith(compareBy(spanishCollator) { it })

    val services: List<String> = listOf(
        "Acepta mascotas",
        "Aire acondicionado",
        "Bar",
        "Desayuno incluido",
        "Estacionamiento",
        "Gimnasio",
        "Lavandería",
        "Piscina",
        "Restaurante",
        "Servicio a la habitación",
        "Spa",
        "Tour guiado",
        "Traslado al aeropuerto",
        "WiFi"
    ).sortedWith(compareBy(spanishCollator) { it })

    fun isValidCity(city: String): Boolean = city in cities

    fun isValidCategory(category: String): Boolean = category in categories

    const val MAX_HOTELS_PER_GERENTE = 1
}
