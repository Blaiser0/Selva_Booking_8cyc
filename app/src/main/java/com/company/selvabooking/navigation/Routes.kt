package com.company.selvabooking.navigation

import android.net.Uri

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"

    const val CLIENT_HOME = "client_home"
    const val CLIENT_SEARCH = "client_search"
    const val CLIENT_RESERVATIONS = "client_reservations"
    const val CLIENT_PROFILE = "client_profile"
    const val HOTEL_DETAIL = "hotel_detail/{hotelId}"
    const val BOOKING = "booking/{hotelId}/{roomId}"
    const val PAYMENT = "payment/{reservationId}"

    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_HOTELS = "admin_hotels"

    object AdminHotelsFilter {
        const val ALL = "all"
        const val WITHOUT_OWNER = "sin_encargado"
        const val WITHOUT_ADMIN = "sin_admin"
    }
    const val ADMIN_HOTEL_FORM = "admin_hotel_form?hotelId={hotelId}"
    const val ADMIN_ROOMS = "admin_rooms/{hotelId}?hotelName={hotelName}"
    const val ADMIN_ROOM_FORM = "admin_room_form/{hotelId}?roomId={roomId}"
    const val ADMIN_RESERVATIONS = "admin_reservations"
    const val ADMIN_ADMINISTRADORES = "admin_administradores"
    const val ADMIN_GERENTES = "admin_gerentes"
    const val ADMIN_AUDIT = "admin_audit"
    const val ADMIN_USERS = "admin_users"
    const val ADMIN_REVIEWS = "admin_reviews"
    const val ADMIN_HOTEL_REVIEWS = "admin_hotel_reviews/{hotelId}?hotelName={hotelName}"
    const val ADMIN_PROFILE = "admin_profile"
    const val GERENTE_COMPLETE_PROFILE = "gerente_complete_profile"
    const val MANAGER_HOTELS = "manager_hotels"
    const val MANAGER_RESERVATIONS = "manager_reservations"
    const val MANAGER_REVIEWS = "manager_reviews"
    const val MANAGER_PROFILE = "manager_profile"
    const val SUPPORT = "support"

    fun hotelDetail(hotelId: String) = "hotel_detail/$hotelId"
    fun booking(hotelId: String, roomId: String) = "booking/$hotelId/$roomId"
    fun payment(reservationId: String) = "payment/$reservationId"
    fun adminRooms(hotelId: String, hotelName: String = "") =
        if (hotelName.isBlank()) {
            "admin_rooms/$hotelId"
        } else {
            "admin_rooms/$hotelId?hotelName=${Uri.encode(hotelName)}"
        }
    fun adminHotelReviews(hotelId: String, hotelName: String = "") =
        if (hotelName.isBlank()) {
            "admin_hotel_reviews/$hotelId"
        } else {
            "admin_hotel_reviews/$hotelId?hotelName=${Uri.encode(hotelName)}"
        }
    fun adminHotels(filter: String = AdminHotelsFilter.ALL): String =
        if (filter == AdminHotelsFilter.ALL) {
            ADMIN_HOTELS
        } else {
            "$ADMIN_HOTELS?hotelsFilter=$filter"
        }

    fun routeBase(route: String): String = route.substringBefore("?")

    fun matchesRoute(currentRoute: String, targetRoute: String): Boolean =
        routeBase(currentRoute) == routeBase(targetRoute)

    fun adminHotelForm(hotelId: String? = null) =
        if (hotelId != null) "admin_hotel_form?hotelId=$hotelId" else "admin_hotel_form"
    fun adminRoomForm(hotelId: String, roomId: String? = null) =
        if (roomId != null) "admin_room_form/$hotelId?roomId=$roomId"
        else "admin_room_form/$hotelId"
}
