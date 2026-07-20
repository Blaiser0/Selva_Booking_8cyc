package com.company.selvabooking.navigation

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
    const val ADMIN_HOTEL_FORM = "admin_hotel_form?hotelId={hotelId}"
    const val ADMIN_ROOMS = "admin_rooms/{hotelId}"
    const val ADMIN_ROOM_FORM = "admin_room_form/{hotelId}?roomId={roomId}"
    const val ADMIN_RESERVATIONS = "admin_reservations"
    const val ADMIN_REQUESTS = "admin_requests"
    const val ADMIN_PROFILE = "admin_profile"
    const val SUPPORT = "support"

    fun hotelDetail(hotelId: String) = "hotel_detail/$hotelId"
    fun booking(hotelId: String, roomId: String) = "booking/$hotelId/$roomId"
    fun payment(reservationId: String) = "payment/$reservationId"
    fun adminRooms(hotelId: String) = "admin_rooms/$hotelId"
    fun adminHotelForm(hotelId: String? = null) =
        if (hotelId != null) "admin_hotel_form?hotelId=$hotelId" else "admin_hotel_form"
    fun adminRoomForm(hotelId: String, roomId: String? = null) =
        if (roomId != null) "admin_room_form/$hotelId?roomId=$roomId"
        else "admin_room_form/$hotelId"
}
